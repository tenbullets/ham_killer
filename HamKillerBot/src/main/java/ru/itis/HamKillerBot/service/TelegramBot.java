package ru.itis.HamKillerBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.itis.HamKillerBot.config.BotConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    static final String HELP_TEXT = "Welcome \n" + "It's Hamster Kombat Bot killer";

    @Autowired
    Tapper tapper;

    public TelegramBot(BotConfig config) {
        this.config = config;

        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "getting started"));
        commands.add(new BotCommand("/help", HELP_TEXT));
        commands.add(new BotCommand("/launch", "launch bot"));
        commands.add(new BotCommand("/balance", "get balance"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (msg) {
                case "/start":
                    startCmdReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                case "/help":
                    sendMsg(chatId, HELP_TEXT);
                    break;

                case "/launch":
                    try {
                        sendMsg(chatId, "Launched");
                        tapper.launch(this, chatId);
                    } catch (IOException | InterruptedException e) {
                        log.error("Launch error: " + e.getMessage());
                    }
                    break;

                case "/balance":
                    try {
                        int balance = tapper.getData().get(3);
                        sendMsg(chatId, "Your Balance: " + balance);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                default:
                    sendMsg(chatId, "Not Found");
            }
        }
    }

    private void startCmdReceived(long chatId, String name) {
        String answer = "Hi, " + name;
        sendMsg(chatId, answer);
        log.info("Replied to user: " + name);
    }

    public void sendMsg(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setParseMode(ParseMode.HTML);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("The message has not been sent: " + e.getMessage());
        }
    }

}
