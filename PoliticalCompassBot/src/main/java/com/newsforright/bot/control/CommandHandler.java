package com.newsforright.bot.control;

import com.newsforright.bot.entities.BotState;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.enums.Phase;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
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

    public void parseMessage(String message, TelegramUser currentUser, BotState state) {
        if (isStartCommand(message)){
            startGreeting(currentUser, state);
        }
    }

    private void startGreeting(TelegramUser currentUser, BotState state) {
        if (state.getPhase() != Phase.PRESTART) return;

        output.printGreeting(currentUser.getChatId());
        state.setPhase(Phase.GREETING);
        dbManager.saveState(state);
    }

    private boolean isStartCommand(String message) {
        return  (message.equalsIgnoreCase("/start")
                || message.equalsIgnoreCase("/start@" + botUsername));
    }
}
