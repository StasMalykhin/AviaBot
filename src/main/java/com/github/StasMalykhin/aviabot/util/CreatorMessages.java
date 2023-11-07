package com.github.StasMalykhin.aviabot.util;

import com.github.StasMalykhin.aviabot.entity.AirTicket;
import com.github.StasMalykhin.aviabot.entity.AppUser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Optional;

/**
 * Создает SendMessage и EditMessage c разными конфигурациями.
 *
 * @author Stanislav Malykhin
 */
@Component
public class CreatorMessages {
    public SendMessage createMessageWithAirTicket(AppUser user, InlineKeyboardMarkup keyboardMarkup,
                                                  AirTicket airTicket, String firstPrice,
                                                  String departureDate, String arrivalDate,
                                                  Optional<Integer> actualPrice) {
        StringBuilder message = new StringBuilder();
        message.append("Номер рейса: ").append(airTicket.getFlightNumber()).append("\n")
                .append("Город отправления: ").append(airTicket.getNameCityDeparture()).append("\n")
                .append("Код города отправления: ").append(airTicket.getCodeCityDeparture()).append("\n")
                .append("Аэропорт отправления: ").append(airTicket.getDepartureAirport()).append("\n")
                .append("Город назначения: ").append(airTicket.getNameCityDestination()).append("\n")
                .append("Код города назначения: ").append(airTicket.getCodeCityDestination()).append("\n")
                .append("Аэропорт назначения: ").append(airTicket.getDestinationAirport()).append("\n");
        boolean priceHasNotChangedBefore = actualPrice.isEmpty();
        if (priceHasNotChangedBefore) {
            message.append("Стоимость: ").append(firstPrice).append("\n");
        } else {
            message.append("Первоначальная стоимость: ").append(firstPrice).append("\n")
                    .append("Актуальная стоимость: ").append(actualPrice.get()).append(" руб.").append("\n")
                    .append(airTicket.getPercentageDeviation()).append("\n");
        }
        message.append("Авиакомпания: ").append(airTicket.getNameAirline()).append("\n")
                .append("Отбытие в: ").append(departureDate).append("\n")
                .append("Прибытие в: ").append(arrivalDate).append("\n")
                .append("Время в пути: ").append(airTicket.getFlightTime() / 60).append(" ч. ")
                .append(airTicket.getFlightTime() % 60).append(" мин.");
        return createSendMessageWithKeyboard(message.toString(), user, keyboardMarkup);
    }

    public SendMessage createMessageWithAirTicket(AppUser user, InlineKeyboardMarkup keyboardMarkup,
                                                  AirTicket airTicket, String firstPrice,
                                                  String departureDate, String arrivalDate) {
        StringBuilder message = new StringBuilder();
        message.append("Номер рейса: ").append(airTicket.getFlightNumber()).append("\n")
                .append("Город отправления: ").append(airTicket.getNameCityDeparture()).append("\n")
                .append("Код города отправления: ").append(airTicket.getCodeCityDeparture()).append("\n")
                .append("Аэропорт отправления: ").append(airTicket.getDepartureAirport()).append("\n")
                .append("Город назначения: ").append(airTicket.getNameCityDestination()).append("\n")
                .append("Код города назначения: ").append(airTicket.getCodeCityDestination()).append("\n")
                .append("Аэропорт назначения: ").append(airTicket.getDestinationAirport()).append("\n")
                .append("Стоимость: ").append(firstPrice).append("\n")
                .append("Авиакомпания: ").append(airTicket.getNameAirline()).append("\n")
                .append("Отбытие в: ").append(departureDate).append("\n")
                .append("Прибытие в: ").append(arrivalDate).append("\n")
                .append("Время в пути: ").append(airTicket.getFlightTime() / 60).append(" ч. ")
                .append(airTicket.getFlightTime() % 60).append(" мин.");
        return createSendMessageWithKeyboard(message.toString(), user, keyboardMarkup);
    }

    public SendMessage createSendMessageWithKeyboard(String message, AppUser user,
                                                     InlineKeyboardMarkup keyboardMarkup) {
        return SendMessage.builder()
                .text(message)
                .chatId(user.getTelegramUserId().toString())
                .replyMarkup(keyboardMarkup)
                .build();
    }

    public SendMessage createSendMessageWithKeyboardAndParseMode(String message, AppUser user, String parseMode,
                                                                 ReplyKeyboardMarkup keyboardMarkup) {
        return SendMessage.builder()
                .text(message)
                .chatId(user.getTelegramUserId().toString())
                .replyMarkup(keyboardMarkup)
                .parseMode(parseMode)
                .build();
    }

    public SendMessage createSendMessageWithKeyboard(String message, AppUser user,
                                                     ReplyKeyboardMarkup keyboardMarkup) {
        return SendMessage.builder()
                .text(message)
                .chatId(user.getTelegramUserId().toString())
                .replyMarkup(keyboardMarkup)
                .build();
    }

    public SendMessage createSendMessage(String message, AppUser user) {
        return SendMessage.builder()
                .text(message)
                .chatId(user.getTelegramUserId().toString())
                .build();
    }

    public EditMessageReplyMarkup createEditMessageWithKeyboard(Update update,
                                                                InlineKeyboardMarkup keyboardMarkup) {
        return EditMessageReplyMarkup.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .replyMarkup(keyboardMarkup)
                .build();
    }
}
