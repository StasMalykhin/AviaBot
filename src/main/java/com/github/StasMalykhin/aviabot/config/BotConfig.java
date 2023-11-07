package com.github.StasMalykhin.aviabot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Stanislav Malykhin
 */
@Component
@ConfigurationProperties(prefix = "telegrambot")
@Getter
@Setter
public class BotConfig {
    private String userName;
    private String botToken;
    private String webHookPath;
}
