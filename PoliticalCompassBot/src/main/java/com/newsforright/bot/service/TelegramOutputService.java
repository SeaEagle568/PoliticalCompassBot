package com.newsforright.bot.service;

import com.newsforright.bot.Bot;
import com.newsforright.bot.enums.Answer;
import com.newsforright.bot.enums.Util;
import com.newsforright.bot.util.CommonUtils;
import com.newsforright.bot.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramOutputService {

    private Bot bot;
    private CommonUtils utils;

    @Autowired
    public void setUtils(CommonUtils utils) {
        this.utils = utils;
    }
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
            greeting = Files.readString(Path.of(utils.getGreetingFile()));
        } catch (IOException e) {
            String error = "Cannot read greeting file!";
            e.printStackTrace();
            utils.printErrorToDev(error);
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

    private ReplyKeyboardMarkup startQuizMarkup() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(
                List.of(
                        oneButtonRow(Util.LETSGO.getText())
                )
        );
        return result;
    }

    private List<KeyboardRow> quizBasicButtonList(){
        return List.of(
                oneButtonRow(Answer.STRONG_AGREE.getText()),
                oneButtonRow(Answer.WEAK_AGREE.getText()),
                oneButtonRow(Answer.DONT_KNOW.getText()),
                oneButtonRow(Answer.WEAK_DISAGREE.getText()),
                oneButtonRow(Answer.STRONG_DISAGREE.getText())
        );
    }


    private ReplyKeyboardMarkup resultsKeyboard() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(
                List.of(
                    oneButtonRow(Util.RESTART.getText())
                )
        );
        return result;
    }

    private ReplyKeyboardMarkup quizFirstKeyboard() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(quizBasicButtonList());
        return result;
    }

    private ReplyKeyboardMarkup quizKeyboardMarkup() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        List<KeyboardRow> buttonList = new ArrayList<KeyboardRow>(quizBasicButtonList());
        buttonList.add(oneButtonRow(Util.BACK.getText()));
        result.setKeyboard(buttonList);
        return result;
    }




    private KeyboardRow oneButtonRow(String text) {
        KeyboardRow result = new KeyboardRow();
        result.add(text);
        return result;
    }

    public void askQuestion(String text, String chatId, Boolean isFirst){
        if (isFirst) printWithMarkup(text,chatId, quizFirstKeyboard());
        else printWithMarkup(text, chatId, quizKeyboardMarkup());
    }


    public void sendResults(String chatId, Pair<Double, Double> results) {

        File image = utils.getCompassWithDot(results);
        SendPhoto photo = new SendPhoto();
        photo.setPhoto(new InputFile(image));
        photo.setChatId(chatId);
        photo.setReplyMarkup(resultsKeyboard());
        try {
            bot.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            utils.printErrorToDev("Error uploading file");
        }
    }

}
