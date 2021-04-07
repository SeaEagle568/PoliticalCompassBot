package com.newsforright.bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsforright.bot.control.MainController;
import com.newsforright.bot.entities.Question;
import com.newsforright.bot.persistence.DBManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.resources.questions}")
    private String questionsFile;

    private MainController controller;
    private ObjectMapper objectMapper;
    private DBManager dbManager;

    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Autowired
    public void setController(MainController controller) {
        this.controller = controller;
    }


    @PostConstruct
    private void onStartup(){
        ArrayList<Question> questionList = null;
        try {
            questionList = objectMapper.readValue(new FileReader(questionsFile), new TypeReference<ArrayList<Question>>(){});
        } catch (IOException e) {
            String error = "Error updating questions";
            System.err.println(error);
            controller.getOutput().printErrorToDev(error);
            e.printStackTrace();
        }
        dbManager.saveQuestions(questionList);
        System.out.println("[" + java.time.LocalDateTime.now() + "]"
            + " Bot successfully started"
        );
    }
    @Override
    public void onUpdateReceived(Update update) {
        controller.razgrebiUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

}
