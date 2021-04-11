package com.newsforright.bot.control;

import com.newsforright.bot.entities.BotState;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.enums.Phase;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Little handler that deals with telegram commands
 *
 * @author seaeagle
 */
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

    /**
     * Method called by MainController
     * Now only checks /start
     * @param message String input message
     * @param currentUser Telegram user who send message
     */
    public void parseMessage(String message, TelegramUser currentUser) {
        if (isStartCommand(message)){
            startGreeting(currentUser, currentUser.getBotState());
        }
    }

    /**
     * Only if user on PRESTART sends greetings
     * @param currentUser Telegram user who send message
     * @param state IoC user.getBotState()
     */
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
