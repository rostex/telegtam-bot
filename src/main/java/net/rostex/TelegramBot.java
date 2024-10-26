package net.rostex;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.validation.constraints.NotNull;
import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {

    private static final String ADD_EXPENSE_BTN = "Добавить трату";
    private static final String SHOW_CATEGORIES_BTN = "Показать категорию";
    private static final String SHOW_EXPENSES_BTN = "Показать все траты";

    private static final String IDLE_STATE = "IDLE";
    private static final String AWAITS_CATEGORY_STATE = "AWAITS_CATEGORY";
    private static final String AWAITS_EXPENSE_STATE = "AWAITS_EXPENSE";

    private static final Map<Long, ChatState> CHATS = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "railway_switch_calculate_bot";
    }

    public String getBotToken() {
        return "7323902783:AAH_9dCpstDnLg51NL3BH6m9OHDk8lfql_o";
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            System.out.println("Unsupported update");
            return;
        }

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        CHATS.putIfAbsent(chatId, new ChatState(IDLE_STATE));

        ChatState currentChat = CHATS.get(chatId);
        switch (currentChat.state) {
            case IDLE_STATE -> handelIdle(message, currentChat);
            case AWAITS_CATEGORY_STATE -> handleAwaitsCategory(message, currentChat);
            case AWAITS_EXPENSE_STATE -> handleAwaitsExpense(message, currentChat);
        }

    }

    private void handelIdle(Message incomingMessage, ChatState currentChat) {
        String incomingText = incomingMessage.getText();
        Long chatId = incomingMessage.getChatId();

        List<String> defaultButtons = List.of(
                ADD_EXPENSE_BTN,
                SHOW_CATEGORIES_BTN,
                SHOW_EXPENSES_BTN);

        switch (incomingText) {
            case SHOW_CATEGORIES_BTN -> changeState(
                    IDLE_STATE,
                    chatId,
                    currentChat,
                    currentChat.getFormattedCategories(),
                    defaultButtons
            );

            case SHOW_EXPENSES_BTN -> changeState(
                    IDLE_STATE,
                    chatId,
                    currentChat,
                    currentChat.getFormattedExpenses(),
                    defaultButtons
            );

            case ADD_EXPENSE_BTN -> changeState(
                    AWAITS_CATEGORY_STATE,
                    chatId,
                    currentChat,
                    "Укажите категорию",
                    null
            );
            default -> changeState(
                    IDLE_STATE,
                    chatId,
                    currentChat,
                    "Не известная команда",
                    defaultButtons
            );
        }
    }

    private void handleAwaitsCategory(Message incomingMessage, ChatState currentChat) {
        String incomingText = capitalize(incomingMessage.getText());
        Long chatId = incomingMessage.getChatId();
        currentChat.expenses.putIfAbsent(incomingText, new ArrayList<>());
        currentChat.data = incomingText;

        changeState(
                AWAITS_EXPENSE_STATE,
                chatId,
                currentChat,
                "Введите трату",
                null
        );
    }

    private void handleAwaitsExpense(Message incomingMessage, ChatState currentChat) {
        Long chatId = incomingMessage.getChatId();

        List<String> defaultButtons = List.of(
                ADD_EXPENSE_BTN,
                SHOW_CATEGORIES_BTN,
                SHOW_EXPENSES_BTN);


        if (currentChat.data == null) {
            changeState(
                    IDLE_STATE,
                    chatId,
                    currentChat,
                    "Попробуйте сначала",
                    defaultButtons
            );
            return;
        }

        String incomingText = incomingMessage.getText();
        Integer expense = Integer.parseInt(incomingText);
        currentChat.expenses.get(currentChat.data).add(expense);
        changeState(
                IDLE_STATE,
                chatId,
                currentChat,
                "Трата добавлена",
                defaultButtons
        );
    }

    private void changeState(String newState,
                             Long chatId,
                             ChatState currentChat,
                             String messageText,
                             List<String> buttonNames) {
        currentChat.state = newState;

        ReplyKeyboard keyboard = buildKeyboard(buttonNames);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(keyboard);


        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("!!!ERROR!!!");
            System.out.println(e);
        }
    }

    private ReplyKeyboard buildKeyboard(List<String> buttonNames) {
        if (buttonNames == null || buttonNames.isEmpty()) return new ReplyKeyboardRemove(true);
        List<KeyboardRow> rows = new ArrayList<>();
        for (var buttonName : buttonNames) {
            final KeyboardRow row = new KeyboardRow();
            row.add(buttonName);
            rows.add(row);
        }
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setKeyboard(rows);
        return replyKeyboard;
    }

    private String capitalize(String text) {
        return text
                .substring(0, 1)
                .toUpperCase()
                + text
                .substring(1).toLowerCase();
    }

}