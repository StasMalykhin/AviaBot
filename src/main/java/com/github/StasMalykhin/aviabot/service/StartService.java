package com.github.StasMalykhin.aviabot.service;

import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import com.github.StasMalykhin.aviabot.util.KeyboardMarkup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.List;

/**
 * Выводит пользователю основное меню.
 *
 * @author Stanislav Malykhin
 */
@Service
@RequiredArgsConstructor
public class StartService {
    private final MessageService messageService;
    private final CreatorMessages creatorMsg;
    private final KeyboardMarkup keyboardMarkup;

    public List<PartialBotApiMethod<? extends Serializable>> showMenu(AppUser user) {
        String text = messageService.getMessage("start.menu");
        return List.of(creatorMsg
                .createSendMessageWithKeyboardAndParseMode(text, user,
                        "Markdown", keyboardMarkup.createReplyKeyboardWithMenu()));
    }
}
