package com.newsforright.bot.service;

import com.newsforright.bot.Bot;
import com.newsforright.bot.enums.Answer;
import com.newsforright.bot.enums.Button;
import com.newsforright.bot.enums.Util;
import com.newsforright.bot.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class somewhere near 'View' layer
 * Responsible for printing everything to end User in Telegram
 *
 * @author seaeagle
 */
@Service
public class TelegramOutputService {
    //dependencies
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

    /**
     * Method that prints greeting to user
     * Greeting is loaded from file
     * @param chatId End user chat id
     */
    public void printGreeting(String chatId) {
        String greeting = "Ой-йой сталася дурня, зачекайте трошки...";
        try {
            greeting = Files.readString(
                    Path.of(utils.getGreetingFile())
            );
        } catch (IOException e) {
            String error = "Cannot read greeting file!";
            e.printStackTrace();
            utils.printErrorToDev(error); //TODO: delete on release
        }
        printWithMarkup(greeting, chatId, startQuizMarkup());

    }

    /**
     * A method that prints some text that is supposed to be Quiz question to user
     * Uses answer keyboard markup
     * @param text String message text
     * @param chatId End user chat id
     * @param isFirst Boolean if it is the first question (No need to make BACK button)
     */
    public void askQuestion(String text, String chatId, Boolean isFirst){
        if (isFirst) printWithMarkup(text,chatId, quizFirstKeyboard());
        else printWithMarkup(text, chatId, quizKeyboardMarkup());
    }

    /**
     * A method that sends results (Image + Text) to user
     *
     * TODO: implement text result
     * @param chatId End user chat id
     * @param image Image(compass with dot) file from CommonUtils
     */
    public void sendResults(String chatId, File image) {

        SendPhoto photo = new SendPhoto();
        photo.setPhoto(new InputFile(image));
        photo.setChatId(chatId);
        photo.setReplyMarkup(resultsKeyboard());
        try {
            bot.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            utils.printErrorToDev("Error sending file"); //TODO: delete on release
        }
    }

    /**
     * Simple method to print some text with buttons to user
     * @param text String with message to send
     * @param chatId End user chat id
     * @param markup Keyboard (or Remove (or Inline)) markup
     */
    private void printWithMarkup(String text, String chatId, ReplyKeyboard markup) {
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

    /**
     * Simple method that creates one button markup
     * Used with greeting
     * @return KeyboardMarkup with one Util.LETSGO button
     */
    private ReplyKeyboardMarkup startQuizMarkup() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(
                List.of(
                        oneButtonRow(Util.LETSGO)
                )
        );
        return result;
    }

    /**
     * Simple method that returns IMMUTABLE List of buttons for quiz
     * @return List<KeyboardRow> with 5 buttons, one button per row all Answer type
     */
    private List<KeyboardRow> quizBasicButtonList(){
        return List.of(
                oneButtonRow(Answer.STRONG_AGREE),
                oneButtonRow(Answer.WEAK_AGREE),
                oneButtonRow(Answer.DONT_KNOW),
                oneButtonRow(Answer.WEAK_DISAGREE),
                oneButtonRow(Answer.STRONG_DISAGREE)
        );
    }

    /**
     * Simple method that generates one button Keyboard for RESULTS phase
     * @return keyboard markup with one Utils.RESTART button
     */
    private ReplyKeyboardMarkup resultsKeyboard() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(
                List.of(
                    oneButtonRow(Util.RESTART)
                )
        );
        return result;
    }

    /**
     * Simple method that converts list of standard quiz buttons to KeyboardMarkup
     * @return KeyboardMarkup with 5 buttons
     */
    private ReplyKeyboardMarkup quizFirstKeyboard() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        result.setKeyboard(quizBasicButtonList());
        return result;
    }

    /**
     * Simple method that converts list of standard quiz buttons + BACK button to KeyboardMarkup
     * @return KeyboardMarkup with 6 buttons
     */
    private ReplyKeyboardMarkup quizKeyboardMarkup() {
        ReplyKeyboardMarkup result = new ReplyKeyboardMarkup();
        List<KeyboardRow> buttonList = new ArrayList<>(quizBasicButtonList());
        buttonList.add(oneButtonRow(Util.BACK));
        result.setKeyboard(buttonList);
        return result;
    }

    /**
     * Method that creates a KeyboardRow object with 1 button
     * @param button Object that implements Button interface
     * @return       KeyboardRow
     */
    private KeyboardRow oneButtonRow(Button button) {
        KeyboardRow result = new KeyboardRow();
        result.add(button.getText());
        return result;
    }



}
