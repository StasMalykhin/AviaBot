package com.github.StasMalykhin.aviabot.handler.Impl;

import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.entity.enums.Command;
import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import com.github.StasMalykhin.aviabot.handler.Handler;
import com.github.StasMalykhin.aviabot.service.MessageService;
import com.github.StasMalykhin.aviabot.service.StartService;
import com.github.StasMalykhin.aviabot.service.SubscriptionToAirTicketService;
import com.github.StasMalykhin.aviabot.service.UserService;
import com.github.StasMalykhin.aviabot.util.CreatorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.List;

/**
 * Обрабатывает запросы, которые связаны с показом списка подписок и с функцией отписки.
 *
 * @author Stanislav Malykhin
 */
@Component
@Log4j
@RequiredArgsConstructor
public class ViewSubscriptionHandler implements Handler {
    private final UserService userService;
    private final SubscriptionToAirTicketService subscriptionToAirTicketService;
    private final StartService startService;
    private final MessageService messageService;
    private final CreatorMessages creatorMsg;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(AppUser user, Update update, String message) {
        if (message.equals("Вернуться в основное меню")) {
            userService.updateStatus(user, UserState.START);
            return startService.showMenu(user);
        } else if (message.equals("Мои подписки")) {
            return subscriptionToAirTicketService.informAboutActualUserSubscriptions(user);
        } else if (message.equals(Command.UNSUBSCRIBE_FROM_TICKET.toString())) {
            return subscriptionToAirTicketService.unsubscribeFromTicket(user, update);
        } else {
            String text = messageService.getMessage("subscription.pointerToButton");
            return List.of(creatorMsg.createSendMessage(text, user));
        }
    }

    @Override
    public List<UserState> operatedBotState() {
        return List.of(UserState.SHOW_SUBSCRIPTIONS);
    }


    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(Command.UNSUBSCRIBE_FROM_TICKET.toString());
    }
}
