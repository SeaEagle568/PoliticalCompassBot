package com.newsforright.bot.service;

import com.newsforright.bot.Bot;
import com.newsforright.bot.enums.Answer;
import com.newsforright.bot.enums.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class TelegramOutputService {

    private Bot bot;

    @Value("${bot.resources.greeting}")
    private String greetingFile;

    @Value("${bot.devChatId}")
    private String devChatId;

    @Autowired
    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public void printSimpleMessage(String text, String chatId) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        ReplyKeyboardRemove remove = new ReplyKeyboardRemove();
        remove.setRemoveKeyboard(true);
        message.setReplyMarkup(remove);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void printGreeting(String chatId) {
        String greeting = "Ой-йой сталася дурня, зачекайте трошки...";
        try {
            greeting = Files.readString(Path.of(greetingFile));
        } catch (IOException e) {
            String error = "Cannot read greeting file!";
            e.printStackTrace();
            printErrorToDev(error);
        }
        printWithMarkup(greeting, chatId, startQuizMarkup());

    }
    public void printWithMarkup(String text, String chatId, ReplyKeyboard markup) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        message.setReplyMarkup(markup);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private ReplyKeyboardMarkup quizKeyboardMarkup() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(
                List.of(
                        oneButtonRow(Answer.STRONG_AGREE.getText()),
                        oneButtonRow(Answer.WEAK_AGREE.getText()),
                        oneButtonRow(Answer.DONT_KNOW.getText()),
                        oneButtonRow(Answer.WEAK_DISAGREE.getText()),
                        oneButtonRow(Answer.STRONG_DISAGREE.getText()),
                        oneButtonRow(Util.BACK.getText())
                )
        );
        return result;
    }

    private ReplyKeyboardMarkup startQuizMarkup() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(
                List.of(
                        oneButtonRow(Util.LETSGO.getText())
                )
        );
        return result;
    }


    private KeyboardRow oneButtonRow(String text) {
        KeyboardRow result = new KeyboardRow();
        result.add(text);
        return result;
    }


    public void printErrorToDev(String error) {
        printSimpleMessage(error, devChatId);
    }


}
