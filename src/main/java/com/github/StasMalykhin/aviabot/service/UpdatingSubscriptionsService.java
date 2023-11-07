package com.github.StasMalykhin.aviabot.service;

import com.github.StasMalykhin.aviabot.bot.Bot;
import com.github.StasMalykhin.aviabot.entity.AirTicket;
import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import com.github.StasMalykhin.aviabot.util.DateConversion;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Проверяет и по необходимости обновляет или удаляет билеты в подписке раз в сутки.
 *
 * @author Stanislav Malykhin
 */
@Service
@Transactional
@Log4j
@RequiredArgsConstructor
public class UpdatingSubscriptionsService {
    private final Bot bot;
    private final AirTicketService airTicketService;
    private final SearchService searchService;
    private final SubscriptionToAirTicketService subscriptionToAirTicketService;
    private final DateConversion dateConversion;
    private final CreatorMessages creatorMsg;
    private final MessageService messageService;

    @Scheduled(fixedRateString = "${subscriptions.updatePeriod}")
    public void regularCheckOfSubscriptions() {
        log.info("Выполняю обработку подписок пользователей.");
        airTicketService.findAllTickets().stream().forEach(this::checkOfSubscription);
        log.info("Завершил обработку подписок пользователей.");
    }

    private void checkOfSubscription(AirTicket airTicket) {
        AppUser user = airTicket.getAppUser();
        String departureAt = dateConversion.fromDateToString("yyyy-MM-dd 'в' HH:mm", airTicket.getDepartureAt());
        boolean isPastDepartureDate = Instant.now().isAfter(airTicket.getDepartureAt().toInstant());
        if (isPastDepartureDate) {
            handleResultDateInTicketIsOverdue(airTicket, user, departureAt);
            return;
        }
        Optional<AirTicket> actualTicket = Optional.ofNullable(getActualTicketByDate(airTicket));
        boolean ticketWasNotFoundAccordingToSpecifiedParameters = actualTicket.isEmpty();
        boolean ticketFoundIsDifferentFromTicketInSubscription =
                !ticketWasNotFoundAccordingToSpecifiedParameters &&
                        !actualTicket.get().getFlightNumber().equals(airTicket.getFlightNumber());
        if (ticketWasNotFoundAccordingToSpecifiedParameters) {
            handleResultTicketNotFound(airTicket, user, departureAt);
            return;
        } else if (ticketFoundIsDifferentFromTicketInSubscription) {
            String text = subscriptionToAirTicketService
                    .replaceTicketInSubscription(airTicket, user, actualTicket.get(), departureAt);
            bot.sendMessage(creatorMsg.createSendMessage(text, user));
            return;
        }
        checkMinPrice(airTicket, user, departureAt, actualTicket.get());
    }

    private void handleResultDateInTicketIsOverdue(AirTicket airTicket, AppUser user, String departureAt) {
        airTicketService.removeTicket(user, airTicket);
        String text = messageService
                .getMessage("updatingSubscriptions.dateOfTicketInSubscriptionIsNotRelevant",
                        airTicket.getNameCityDeparture(), airTicket.getNameCityDestination(),
                        airTicket.getFlightNumber(), departureAt);
        bot.sendMessage(creatorMsg.createSendMessage(text, user));
    }

    private void handleResultTicketNotFound(AirTicket airTicket, AppUser user, String departureAt) {
        airTicketService.removeTicket(user, airTicket);
        String text = messageService.getMessage("updatingSubscriptions.ticketsForThisDateHaveEnded",
                airTicket.getNameCityDeparture(), airTicket.getNameCityDestination(),
                airTicket.getFlightNumber(), departureAt);
        bot.sendMessage(creatorMsg.createSendMessage(text, user));
    }

    private void checkMinPrice(AirTicket airTicket, AppUser user, String departureAt,
                               AirTicket actualTicket) {
        int currentMinPrice = actualTicket.getFirstMinPrice();
        int savedFirstMinPrice = airTicket.getFirstMinPrice();
        Optional<Integer> savedActualMinPrice = Optional.ofNullable(airTicket.getActualMinPrice());
        boolean minPriceFoundDiffersFromFirstMinPriceInSubscriptions = currentMinPrice != savedFirstMinPrice;
        if (minPriceFoundDiffersFromFirstMinPriceInSubscriptions) {
            boolean priceHasNotChangedBeforeOrPriceChangeHasNotBeenSavedToSubscriptionsBefore =
                    savedActualMinPrice.isEmpty() || savedActualMinPrice.get() != currentMinPrice;
            if (priceHasNotChangedBeforeOrPriceChangeHasNotBeenSavedToSubscriptionsBefore) {
                String firstPriceChangeAsPercentage =
                        calculatePercentageOfDeviationAfterPriceUpdate(savedFirstMinPrice, currentMinPrice);
                airTicket.setPercentageDeviation(firstPriceChangeAsPercentage);
                airTicket.setActualMinPrice(currentMinPrice);
                airTicketService.save(airTicket);
                informOfUserAboutMinPriceChange(airTicket, user, departureAt,
                        currentMinPrice, savedActualMinPrice, firstPriceChangeAsPercentage);
            }
        }
    }

    private void informOfUserAboutMinPriceChange(AirTicket airTicket, AppUser user,
                                                 String departureAt, int currentMinPrice,
                                                 Optional<Integer> savedActualMinPrice,
                                                 String firstPriceChangeAsPercentage) {
        boolean priceHasNotChangedBefore = savedActualMinPrice.isEmpty();
        if (priceHasNotChangedBefore) {
            String text = messageService
                    .getMessage("updatingSubscriptions.subscriptionTicketHasBeenUpdated",
                            airTicket.getNameCityDeparture(), airTicket.getNameCityDestination(),
                            airTicket.getFlightNumber(), departureAt, firstPriceChangeAsPercentage);
            bot.sendMessage(creatorMsg.createSendMessage(text, user));
        } else {
            String actualPriceChangeAsPercentage =
                    calculatePercentageOfDeviationAfterPriceUpdate(
                            savedActualMinPrice.get(), currentMinPrice);
            String text = messageService
                    .getMessage("updatingSubscriptions.subscriptionTicketHasBeenUpdated",
                            airTicket.getNameCityDeparture(), airTicket.getNameCityDestination(),
                            airTicket.getFlightNumber(), departureAt, actualPriceChangeAsPercentage);
            bot.sendMessage(creatorMsg.createSendMessage(text, user));
        }
    }

    private String calculatePercentageOfDeviationAfterPriceUpdate(int priceAfterUpdate, int priceBeforeUpdate) {
        double percentageDeviationFromPrice = (double) (priceBeforeUpdate - priceAfterUpdate) *
                100 / priceAfterUpdate;
        int deviationInIntegers = 0;
        if (percentageDeviationFromPrice >= 0.5) {
            deviationInIntegers = (int) (percentageDeviationFromPrice + 0.5);
        } else if (percentageDeviationFromPrice <= -0.5) {
            deviationInIntegers = (int) (percentageDeviationFromPrice - 0.5);
        }
        if (deviationInIntegers >= 1) {
            return "Билет подорожал на " + deviationInIntegers + "% !";
        } else if (deviationInIntegers <= -1) {
            return "Билет подешевел на " + -deviationInIntegers + "% !!!";
        } else if (percentageDeviationFromPrice > 0) {
            return "Билет подорожал на " + (priceBeforeUpdate - priceAfterUpdate) + " руб.";
        } else {
            return "Билет подешевел на " + (priceAfterUpdate - priceBeforeUpdate) + " руб.";
        }
    }

    private AirTicket getActualTicketByDate(AirTicket airTicket) {
        String departureAt = dateConversion.fromDateToString("yyyy-MM-dd", airTicket.getDepartureAt());
        return searchService.findAirTicket(airTicket.getCodeCityDeparture(),
                airTicket.getCodeCityDestination(), departureAt);
    }
}
