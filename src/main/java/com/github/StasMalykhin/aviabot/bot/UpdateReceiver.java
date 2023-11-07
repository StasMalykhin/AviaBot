package com.github.StasMalykhin.aviabot.bot;

import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import com.github.StasMalykhin.aviabot.handler.Handler;
import com.github.StasMalykhin.aviabot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разбирает входящие Update и находит нужный обработчик по UserState или CallbackQuery
 *
 * @author Stanislav Malykhin
 */
@Component
@Log4j
@RequiredArgsConstructor
public class UpdateReceiver {
    private final List<Handler> handlers;
    private final UserService userService;
    private static final String CODE_CITY_FROM_INLINE_KEYBOARD_REGEX = "/([\\wа-яёА-ЯЁ]{3})$";

    public List<PartialBotApiMethod<? extends Serializable>> handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            return handleCallbackQuery(update);
        }
        return Collections.emptyList();
    }

    private List<PartialBotApiMethod<? extends Serializable>> handleMessage(Update update) {
        final User telegramUser = update.getMessage().getFrom();
        final Message message = update.getMessage();
        final Long chatId = message.getChatId();
        final AppUser user = userService.findUserByTelegramUserId(chatId).orElseGet(() ->
                userService.save(AppUser.builder()
                        .telegramUserId(telegramUser.getId())
                        .username(telegramUser.getUserName())
                        .firstName(telegramUser.getFirstName())
                        .lastName(telegramUser.getLastName())
                        .state(UserState.START)
                        .build()));

        UserState userState = getStateAfterCheckingToGetMainMenuCommands(message, user);

        Handler handler = getHandlerByState(userState);
        return handler.handle(user, update, message.getText());
    }

    private UserState getStateAfterCheckingToGetMainMenuCommands(Message message, AppUser user) {
        UserState userState = user.getState();
        if (userState.equals(UserState.START)) {
            switch (message.getText()) {
                case "Справочник городов" -> userState = UserState.SEARCH_CITIES_WITH_AIRPORT;
                case "Поиск билетов" -> userState = UserState.SEARCH_AIR_TICKETS;
                case "Мои подписки" -> userState = UserState.SHOW_SUBSCRIPTIONS;
            }
            userService.updateStatus(user, userState);
        }
        return userState;
    }

    private List<PartialBotApiMethod<? extends Serializable>> handleCallbackQuery(Update update) {
        final User telegramUser = update.getCallbackQuery().getFrom();
        final CallbackQuery callbackQuery = update.getCallbackQuery();
        final Long chatId = callbackQuery.getMessage().getChatId();
        final AppUser user = userService.findUserByTelegramUserId(chatId)
                .orElseGet(() -> userService.save(
                        AppUser.builder()
                                .telegramUserId(telegramUser.getId())
                                .username(telegramUser.getUserName())
                                .firstName(telegramUser.getFirstName())
                                .lastName(telegramUser.getLastName())
                                .state(UserState.START)
                                .build()));

        Handler handler = getHandlerAfterCheckingToGetCityCodeInCallbackQuery(user, callbackQuery);
        return handler.handle(user, update, callbackQuery.getData());
    }

    private Handler getHandlerAfterCheckingToGetCityCodeInCallbackQuery(AppUser user, CallbackQuery callbackQuery) {
        Pattern pattern = Pattern.compile(CODE_CITY_FROM_INLINE_KEYBOARD_REGEX);
        Matcher matcher = pattern.matcher(callbackQuery.getData());
        boolean callbackFromButtonOfCity = matcher.find();
        UserState userState = user.getState();
        boolean cityCodeInCallbackQueryWasObtainedDuringTicketSearch
                = (userState.equals(UserState.ENTER_DESTINATION_CITY_NAME_WHEN_SEARCHING_FOR_AIR_TICKETS) ||
                userState.equals(UserState.SEARCH_AIR_TICKETS)) && callbackFromButtonOfCity;
        if (cityCodeInCallbackQueryWasObtainedDuringTicketSearch) {
            return getHandlerByState(userState);
        }
        return getHandlerByCallBackQuery(callbackQuery.getData());
    }

    private Handler getHandlerByState(UserState userState) {
        return handlers.stream()
                .filter(h -> h.operatedBotState() != null)
                .filter(h -> h.operatedBotState().stream().anyMatch(userState::equals))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private Handler getHandlerByCallBackQuery(String query) {
        return handlers.stream()
                .filter(h -> h.operatedCallBackQuery().stream()
                        .anyMatch(query::startsWith))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }
}
