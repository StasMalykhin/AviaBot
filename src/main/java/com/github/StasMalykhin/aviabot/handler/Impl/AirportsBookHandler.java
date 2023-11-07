package com.github.StasMalykhin.aviabot.handler.Impl;

import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import com.github.StasMalykhin.aviabot.handler.Handler;
import com.github.StasMalykhin.aviabot.service.AirportsBookService;
import com.github.StasMalykhin.aviabot.service.MessageService;
import com.github.StasMalykhin.aviabot.service.StartService;
import com.github.StasMalykhin.aviabot.service.UserService;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import com.github.StasMalykhin.aviabot.util.KeyboardMarkup;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Обрабатывает запросы, которые связаны со справочником городов с аэропортами.
 *
 * @author Stanislav Malykhin
 */
@Component
@Log4j
@RequiredArgsConstructor
public class AirportsBookHandler implements Handler {
    private final UserService userService;
    private final AirportsBookService airportsBookService;
    private final StartService startService;
    private final MessageService messageService;
    private final KeyboardMarkup keyboardMarkup;
    private final CreatorMessages creatorMsg;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(AppUser user, Update update, String message) {
        if (message.equals("Вернуться в основное меню")) {
            userService.updateStatus(user, UserState.START);
            return startService.showMenu(user);
        } else if (message.equals("Справочник городов")) {
            String text = messageService.getMessage("airportsBook.manual");
            return List.of(creatorMsg.createSendMessageWithKeyboard(text, user,
                    keyboardMarkup.createReplyKeyboardWithButtonBack()));
        } else {
            log.info("Пользователь " + user.getUsername() + " ввел целиком или частично название страны \"" +
                    message + "\" для поиска городов.");

            return airportsBookService.findCitiesWithAirportByNameCountry(message, user);
        }
    }

    @Override
    public List<UserState> operatedBotState() {
        return List.of(UserState.SEARCH_CITIES_WITH_AIRPORT);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
