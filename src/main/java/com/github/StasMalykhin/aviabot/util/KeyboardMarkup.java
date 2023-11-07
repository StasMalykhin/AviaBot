package com.github.StasMalykhin.aviabot.util;

import com.github.StasMalykhin.aviabot.entity.enums.Command;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Создает inline и reply keyboards.
 *
 * @author Stanislav Malykhin
 */
@Component
public class KeyboardMarkup {
    public InlineKeyboardMarkup createInlineKeyboardWithOneRow(Map<String, String> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRow = new ArrayList<>();
        for (var entry : buttons.entrySet()) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(entry.getValue())
                    .callbackData(entry.getKey())
                    .build();
            inlineKeyboardButtonsRow.add(button);
        }
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRow));
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createInlineKeyboardWithOneColumn(Map<String, String> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (var entry : buttons.entrySet()) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(entry.getValue())
                    .callbackData(entry.getKey())
                    .build();
            List<InlineKeyboardButton> inlineKeyboardButtonsRow = new ArrayList<>();
            inlineKeyboardButtonsRow.add(button);
            keyboard.add(inlineKeyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup createReplyKeyboardWithOneRow(List<String> buttons) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow row = new KeyboardRow();
        for (String button : buttons) {
            row.add(button);
        }
        replyKeyboardMarkup.setKeyboard(List.of(row));
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup createInlineKeyboardWithButtonsBuyAndSubscribe(Map<String, String> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtonsRow = new ArrayList<>();
        List<Map.Entry<String, String>> listWithButtons = new ArrayList<>(buttons.entrySet());
        InlineKeyboardButton buttonWithBuy = InlineKeyboardButton.builder()
                .text(listWithButtons.get(0).getValue())
                .url(listWithButtons.get(0).getKey())
                .build();
        InlineKeyboardButton buttonWithSubscribe = InlineKeyboardButton.builder()
                .text(listWithButtons.get(1).getValue())
                .callbackData(listWithButtons.get(1).getKey())
                .build();
        inlineKeyboardButtonsRow.add(buttonWithBuy);
        inlineKeyboardButtonsRow.add(buttonWithSubscribe);
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRow));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboardMarkup createReplyKeyboardWithMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow firstRow = new KeyboardRow();
        KeyboardRow secondRow = new KeyboardRow();
        firstRow.add("Справочник городов");
        firstRow.add("Поиск билетов");
        secondRow.add("Мои подписки");
        replyKeyboardMarkup.setKeyboard(List.of(firstRow, secondRow));
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup createReplyKeyboardWithButtonBack() {
        ReplyKeyboardMarkup replyKeyboardMarkup = createReplyKeyboardWithOneRow(
                Collections.singletonList("Вернуться в основное меню"));
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup createKeyboardWithButtonSubscriptionIsIssued() {
        return createInlineKeyboardWithOneRow(Map.of(
                Command.SUBSCRIPTION_IS_ISSUED.toString(),
                "Подписался"));
    }

    public InlineKeyboardMarkup createKeyboardWithButtonConfirmationOfDepartureCitySelection() {
        return createInlineKeyboardWithOneRow(Map.of(
                Command.DEPARTURE_CITY_WAS_FOUND_CORRECTLY.toString(),
                "Да"));
    }

    public InlineKeyboardMarkup createKeyboardWithButtonConfirmationOfDestinationCitySelection() {
        return createInlineKeyboardWithOneRow(Map.of(
                Command.DESTINATION_CITY_WAS_FOUND_CORRECTLY.toString(),
                "Да"));
    }

    public ReplyKeyboardMarkup createReplyKeyboardWithButtonsAgainAndBack() {
        return createReplyKeyboardWithOneRow(List.of("Задать новые критерии поиска",
                "Вернуться в основное меню"));
    }

    public InlineKeyboardMarkup keyboardWithButtonUnsubscribe() {
        return createInlineKeyboardWithOneRow(Map.of(
                Command.UNSUBSCRIBE_FROM_TICKET.toString(),
                "Отписаться"));
    }

    public InlineKeyboardMarkup keyboardWithButtonSubscriptionCancelled() {
        return createInlineKeyboardWithOneRow(Map.of(
                Command.SUBSCRIPTION_CANCELLED.toString(),
                "Отписался"));
    }
}
