package com.newsforright.bot.control;

import com.newsforright.bot.entities.BotState;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.enums.Phase;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
import com.newsforright.bot.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommandHandler {

    @Value("${bot.name}")
    private String botUsername;

    private DBManager dbManager;
    private TelegramOutputService output;

    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }
    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
    }

    public void parseMessage(String message, TelegramUser currentUser) {
        if (isStartCommand(message)){
            startGreeting(currentUser, currentUser.getBotState());
        }
        else if (message.equals("/test")){
            //TODO: Delete this after test
            output.sendResults(currentUser.getChatId(), new Pair<Double, Double>(75d, 75d));
        }
    }

    private void startGreeting(TelegramUser currentUser, BotState state) {
        if (state.getPhase() != Phase.PRESTART) return;
        output.printGreeting(currentUser.getChatId());
        dbManager.nextPhase(currentUser.getBotState());
    }

    private boolean isStartCommand(String message) {
        return  (message.equalsIgnoreCase("/start")
                || message.equalsIgnoreCase("/start@" + botUsername));
    }
}
