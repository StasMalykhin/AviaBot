package com.github.StasMalykhin.aviabot.handler;

import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.List;

/**
 * @author Stanislav Malykhin
 */
public interface Handler {
    List<PartialBotApiMethod<? extends Serializable>> handle(AppUser user, Update update, String message);

    List<UserState> operatedBotState();

    List<String> operatedCallBackQuery();
}
