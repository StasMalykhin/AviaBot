package com.github.StasMalykhin.aviabot.service;

import com.github.StasMalykhin.aviabot.entity.AirTicket;
import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import com.github.StasMalykhin.aviabot.util.DateConversion;
import com.github.StasMalykhin.aviabot.util.KeyboardMarkup;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сохраняет, удаляет подписки. Информирует пользователя об актуальных подписках.
 *
 * @author Stanislav Malykhin
 */
@Service
@Log4j
@RequiredArgsConstructor
public class SubscriptionToAirTicketService {
    private final AirTicketService airTicketService;
    private final ConverterToAirTicket converterToAirTicket;
    private final MessageService messageService;
    private final CreatorMessages creatorMsg;
    private final KeyboardMarkup keyboardMarkup;
    private final DateConversion dateConversion;

    public List<PartialBotApiMethod<? extends Serializable>>
    informAboutActualUserSubscriptions(AppUser user) {
        List<AirTicket> tickets = airTicketService.findAllSubscriptionsByUser(user);
        String firstText = messageService.getMessage("subscription.titleOfListOfSubscriptions");
        SendMessage firstMessage = creatorMsg.createSendMessageWithKeyboard(firstText, user,
                keyboardMarkup.createReplyKeyboardWithButtonBack());
        boolean userHasNoSubscriptions = tickets.isEmpty();
        if (userHasNoSubscriptions) {
            String secondText = messageService.getMessage("subscription.noSubscriptionsFound");
            return List.of(firstMessage,
                    creatorMsg.createSendMessage(secondText, user));
        } else {
            return outputActualSubscriptions(user, tickets, firstMessage);
        }
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    outputActualSubscriptions(AppUser user, List<AirTicket> tickets, SendMessage firstMessage) {
        log.info("Пользователь " + user.getUsername() +
                " выгрузил свои подписки на билеты: " + tickets.size() + " шт.");

        List<PartialBotApiMethod<? extends Serializable>> subscriptions = new ArrayList<>();
        subscriptions.add(firstMessage);
        tickets.forEach(airTicket -> {
            String firstPrice = airTicket.getFirstMinPrice() + " руб.";
            Optional<Integer> actualPrice = Optional.ofNullable(airTicket.getActualMinPrice());
            String departureDate =
                    dateConversion.fromDateToString("yyyy-MM-dd HH:mm", airTicket.getDepartureAt());
            String arrivalDate =
                    dateConversion.fromDateToString("yyyy-MM-dd HH:mm", airTicket.getArrivalAt());
            subscriptions.add(creatorMsg.createMessageWithAirTicket(
                    user, keyboardMarkup.keyboardWithButtonUnsubscribe(),
                    airTicket, firstPrice, departureDate, arrivalDate, actualPrice));
        });
        String text = messageService.getMessage("subscription.endOfListOfSubscriptions");
        subscriptions.add(creatorMsg.createSendMessage(text, user));
        return subscriptions;
    }

    public List<PartialBotApiMethod<? extends Serializable>>
    unsubscribeFromTicket(AppUser user, Update update) {
        String ticket = update.getCallbackQuery().getMessage().getText();
        AirTicket ticketFromMessage = converterToAirTicket.conversionToAirTicket(ticket);

        log.info("Пользователь " + user.getUsername() +
                " отменил подписку на билет рейса №" + ticketFromMessage.getFlightNumber() +
                " " + ticketFromMessage.getNameCityDeparture() + "-" +
                ticketFromMessage.getNameCityDestination() + " c отправлением " +
                ticketFromMessage.getDepartureAt());

        AirTicket findTicket = airTicketService.findTicketInSubscriptions(ticketFromMessage, user).get();
        airTicketService.removeTicket(user, findTicket);
        String text = messageService.getMessage("subscription.subscriptionCancelled",
                ticketFromMessage.getFlightNumber());
        return List.of(EditMessageReplyMarkup.builder()
                        .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                        .messageId(update.getCallbackQuery().getMessage().getMessageId())
                        .replyMarkup(keyboardMarkup.keyboardWithButtonSubscriptionCancelled())
                        .build(),
                creatorMsg.createSendMessage(text, user));
    }

    public List<PartialBotApiMethod<? extends Serializable>>
    formSubscriptionForTicket(AppUser user, Update update) {
        String ticket = update.getCallbackQuery().getMessage().getText();
        AirTicket airTicket = converterToAirTicket.conversionToAirTicket(ticket);
        Optional<AirTicket> ticketFromSubscriptionWithSameParameters =
                airTicketService.findTicketWithSameParametersInSubscriptions(airTicket, user);
        boolean wasTicketWithTheseSearchParameters = ticketFromSubscriptionWithSameParameters.isPresent();
        if (wasTicketWithTheseSearchParameters) {
            return checkTicketInSubscriptionsAndUpdateIfNecessary(user, airTicket,
                    ticketFromSubscriptionWithSameParameters.get());
        } else {
            return addTicketToSubscriptionsAndInformUser(user, update, airTicket);
        }
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    checkTicketInSubscriptionsAndUpdateIfNecessary(AppUser user, AirTicket airTicket,
                                                   AirTicket ticketWithSameParameters) {
        Optional<AirTicket> sameTicket = airTicketService.findTicketInSubscriptions(airTicket, user);
        boolean isSameTicketFromSubscription = sameTicket.isPresent();
        if (isSameTicketFromSubscription) {
            String text = messageService.getMessage("searchAirTickets.ticketIsAlreadyStored");
            return List.of(creatorMsg.createSendMessage(text, user));
        } else {
            String departureAt = dateConversion.fromDateToString("yyyy-MM-dd 'в' HH:mm",
                    ticketWithSameParameters.getDepartureAt());
            String text = replaceTicketInSubscription(ticketWithSameParameters, user, airTicket, departureAt);
            return List.of(creatorMsg.createSendMessage(text, user));
        }
    }

    public String replaceTicketInSubscription(AirTicket airTicket, AppUser user,
                                              AirTicket realTicket, String departureAt) {
        airTicketService.removeTicket(user, airTicket);
        String departureDay =
                dateConversion.fromDateToString("yyyy-MM-dd", airTicket.getDepartureAt());
        String departureTime =
                dateConversion.fromDateToString("HH:mm", realTicket.getDepartureAt());
        airTicketService.addTicket(user, realTicket);
        return messageService
                .getMessage("updatingSubscriptions.ticketInSubscriptionHasBeenReplaced",
                        airTicket.getFlightNumber(), airTicket.getNameCityDeparture(),
                        airTicket.getNameCityDestination(), departureAt, departureDay,
                        realTicket.getFlightNumber(), departureTime, realTicket.getFirstMinPrice());
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    addTicketToSubscriptionsAndInformUser(AppUser user, Update update, AirTicket airTicket) {
        airTicketService.addTicket(user, airTicket);

        log.info("Пользователь " + user.getUsername() +
                " оформил подписку на билет рейса №" + airTicket.getFlightNumber() +
                " " + airTicket.getNameCityDeparture() + "-" +
                airTicket.getNameCityDestination() + " c отправлением " +
                airTicket.getDepartureAt());

        String text = messageService
                .getMessage("searchAirTickets.subscriptionIsIssued", airTicket.getFlightNumber());
        return List.of(creatorMsg.createEditMessageWithKeyboard(update,
                        keyboardMarkup.createKeyboardWithButtonSubscriptionIsIssued()),
                creatorMsg.createSendMessage(text, user));
    }
}
