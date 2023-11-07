package com.github.StasMalykhin.aviabot.handler.Impl;

import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import com.github.StasMalykhin.aviabot.handler.Handler;
import com.github.StasMalykhin.aviabot.service.MessageService;
import com.github.StasMalykhin.aviabot.service.StartService;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import com.github.StasMalykhin.aviabot.util.KeyboardMarkup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Обрабатывает запросы, которые связаны с основным меню.
 *
 * @author Stanislav Malykhin
 */
@Component
@RequiredArgsConstructor
public class StartHandler implements Handler {
    private final StartService startService;
    private final MessageService messageService;
    private final CreatorMessages creatorMsg;
    private final KeyboardMarkup keyboardMarkup;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(AppUser user, Update update, String message) {
        if (message.equals("/start")) {
            return startService.showMenu(user);
        } else {
            String text = messageService.getMessage("start.pointerToMenu");
            return List.of(creatorMsg.createSendMessageWithKeyboard(text, user,
                    keyboardMarkup.createReplyKeyboardWithMenu()));
        }
    }

    @Override
    public List<UserState> operatedBotState() {
        return List.of(UserState.START);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
