package com.github.StasMalykhin.aviabot.bot;

import com.github.StasMalykhin.aviabot.config.BotConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.List;

/**
 * @author Stanislav Malykhin
 */
@Component
@Log4j
@Getter
@Setter
public class Bot extends TelegramWebhookBot {
    private String userName;
    private String botToken;
    private final BotConfig botConfig;
    private final UpdateReceiver updateReceiver;

    public Bot(SetWebhook setWebhook,
               BotConfig botConfig, UpdateReceiver updateReceiver) throws TelegramApiException {
        this.botConfig = botConfig;
        this.botToken = botConfig.getBotToken();
        this.userName = botConfig.getUserName();
        this.updateReceiver = updateReceiver;

        this.setWebhook(setWebhook);
    }

    @Override
    public String getBotUsername() {
        return userName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        List<PartialBotApiMethod<? extends Serializable>> messagesToSend = updateReceiver.handleUpdate(update);

        if (messagesToSend != null && !messagesToSend.isEmpty()) {
            messagesToSend.forEach(response -> {
                try {
                    if (response instanceof EditMessageReplyMarkup editMessage) {
                        execute(editMessage);
                    } else if (response instanceof SendMessage message) {
                        execute(message);
                    }
                } catch (TelegramApiException e) {
                    log.error(e);
                }
            });
        }
        return null;
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    @Override
    public String getBotPath() {
        return "/update";
    }
}
