package com.github.StasMalykhin.aviabot.controller;

import com.github.StasMalykhin.aviabot.bot.Bot;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author Stanislav Malykhin
 */
@RestController
@RequiredArgsConstructor
public class WebHookController {
    private final Bot bot;

    @PostMapping("/callback/update")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return bot.onWebhookUpdateReceived(update);
    }
}
