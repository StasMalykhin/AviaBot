package com.github.StasMalykhin.aviabot.handler.Impl;

import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.entity.enums.Command;
import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import com.github.StasMalykhin.aviabot.handler.Handler;
import com.github.StasMalykhin.aviabot.service.*;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import com.github.StasMalykhin.aviabot.util.KeyboardMarkup;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.List;

/**
 * Обрабатывает запросы, которые связаны с поиском билетов и подпиской на них.
 *
 * @author Stanislav Malykhin
 */
@Component
@Log4j
@RequiredArgsConstructor
public class SearchAirTicketsHandler implements Handler {
    private final UserService userService;
    private final AirTicketSearchService airTicketSearchService;

    private final SearchService searchService;
    private final SubscriptionToAirTicketService subscriptionToAirTicketService;
    private final StartService startService;
    private final MessageService messageService;
    private final KeyboardMarkup keyboardMarkup;
    private final CreatorMessages creatorMsg;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(AppUser user, Update update, String message) {
        if (message.equals("Вернуться в основное меню")) {
            return returnMainMenu(user);
        } else if (message.equals("Задать новые критерии поиска")) {
            return returnToBeginningOfSearch(user);
        } else if (message.equals(Command.SUBSCRIBE_TO_TICKET.toString())) {
            return subscriptionToAirTicketService.formSubscriptionForTicket(user, update);
        } else if (message.equals(Command.DEPARTURE_CITY_WAS_FOUND_CORRECTLY.toString())) {
            return confirmDepartureCity(user);
        } else if (message.equals(Command.DESTINATION_CITY_WAS_FOUND_CORRECTLY.toString())) {
            return confirmDestinationCity(user);
        } else if (user.getState().equals(UserState.ENTER_DESTINATION_CITY_NAME_WHEN_SEARCHING_FOR_AIR_TICKETS)) {
            return handleEnteredDestinationCityName(user, message);
        } else if (user.getState().equals(UserState.ENTER_DEPARTURE_DATE_WHEN_SEARCHING_FOR_AIR_TICKETS)) {
            return handleEnteredDepartureDate(user, message);
        } else if (message.equals("Поиск билетов")) {
            return startSearch(user);
        } else {
            return handleEnteredDepartureCityName(user, message);
        }
    }

    private List<PartialBotApiMethod<? extends Serializable>> returnMainMenu(AppUser user) {
        userService.updateStatus(user, UserState.START);
        airTicketSearchService.removeAirTicketSearch(user);
        return startService.showMenu(user);
    }

    private List<PartialBotApiMethod<? extends Serializable>> returnToBeginningOfSearch(AppUser user) {
        userService.updateStatus(user, UserState.SEARCH_AIR_TICKETS);
        airTicketSearchService.removeAirTicketSearch(user);
        String text = messageService.getMessage("searchAirTickets.enterDepartureCity");
        return List.of(creatorMsg
                .createSendMessageWithKeyboard(text, user,
                        keyboardMarkup.createReplyKeyboardWithButtonBack()));
    }

    private List<PartialBotApiMethod<? extends Serializable>> confirmDepartureCity(AppUser user) {
        log.info("Пользователь " + user.getUsername() +
                " подтвердил название города отправления для поиска авиабилетов.");

        userService.updateStatus(user, UserState.ENTER_DESTINATION_CITY_NAME_WHEN_SEARCHING_FOR_AIR_TICKETS);
        String text = messageService.getMessage("searchAirTickets.enterDestinationCity");
        return List.of(creatorMsg.createSendMessage(text, user));
    }

    private List<PartialBotApiMethod<? extends Serializable>> confirmDestinationCity(AppUser user) {
        log.info("Пользователь " + user.getUsername() +
                " подтвердил название города назначения для поиска авиабилетов.");

        userService.updateStatus(user, UserState.ENTER_DEPARTURE_DATE_WHEN_SEARCHING_FOR_AIR_TICKETS);
        String text = messageService.getMessage("searchAirTickets.departureDateFormat");
        return List.of(creatorMsg.createSendMessage(text, user));
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    handleEnteredDestinationCityName(AppUser user, String message) {
        log.info("Пользователь " + user.getUsername() +
                " ввел целиком или частично название города назначения \"" + message +
                "\" для поиска авиабилетов.");

        return searchService.selectionCity(message, user,
                keyboardMarkup.createKeyboardWithButtonConfirmationOfDestinationCitySelection());
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    handleEnteredDepartureDate(AppUser user, String message) {
        log.info("Пользователь " + user.getUsername() +
                " ввел дату отправления \"" + message + "\" для поиска авиабилетов.");

        return searchService.selectionDateAndSearchTickets(message, user,
                keyboardMarkup.createReplyKeyboardWithButtonsAgainAndBack());
    }

    private List<PartialBotApiMethod<? extends Serializable>> startSearch(AppUser user) {
        String firstText = messageService.getMessage("searchAirTickets.manual");
        String secondText = messageService.getMessage("searchAirTickets.enterDepartureCity");
        return List.of(
                creatorMsg.createSendMessage(firstText, user),
                creatorMsg.createSendMessageWithKeyboard(secondText, user,
                        keyboardMarkup.createReplyKeyboardWithButtonBack()));
    }

    private List<PartialBotApiMethod<? extends Serializable>>
    handleEnteredDepartureCityName(AppUser user, String message) {
        log.info("Пользователь " + user.getUsername() +
                " ввел целиком или частично название города отправления \"" + message +
                "\" для поиска авиабилетов.");

        return searchService.selectionCity(message, user,
                keyboardMarkup.createKeyboardWithButtonConfirmationOfDepartureCitySelection());
    }

    @Override
    public List<UserState> operatedBotState() {
        return List.of(UserState.SEARCH_AIR_TICKETS,
                UserState.ENTER_DESTINATION_CITY_NAME_WHEN_SEARCHING_FOR_AIR_TICKETS,
                UserState.ENTER_DEPARTURE_DATE_WHEN_SEARCHING_FOR_AIR_TICKETS);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(Command.DEPARTURE_CITY_WAS_FOUND_CORRECTLY.toString(),
                Command.DESTINATION_CITY_WAS_FOUND_CORRECTLY.toString(),
                Command.SUBSCRIBE_TO_TICKET.toString());
    }
}
