package com.newsforright.bot.control;

import com.newsforright.bot.entities.BotState;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

/**
 * This is main controller class that partially parses message
 * Delegates further parsing to handlers
 * Gets TelegramUser also
 *
 * @author seaeagle
 */
@Service
public class MainController {

    private AnswerHandler answerHandler;
    private CommandHandler commandHandler;
    private DBManager dbManager;
    private CommonUtils utils;

    @Autowired
    public void setUtils(CommonUtils utils) {
        this.utils = utils;
    }
    @Autowired
    public void setAnswerHandler(AnswerHandler answerHandler) {
        this.answerHandler = answerHandler;
    }
    @Autowired
    public void setCommandHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }
    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * A method that razgrebae update
     * If it has no text - exit
     * If it is a command - CommandHandler is to help
     * Else it is answer and we have AnswerHandler for that
     * @param update Update from Bot class
     */
    public void razgrebiUpdate(Update update) {
        Chat chat = update.getMessage().getChat();
        Message message = update.getMessage();

        if (!message.hasText()) return;
        TelegramUser currentUser = getUser(
                chat.getFirstName() + chat.getLastName(),
                chat.getUserName(),
                chat.getId().toString(),
                message.getText()
        );

        if (message.isCommand())
            commandHandler.parseMessage(message.getText(), currentUser);
        else
            answerHandler.parseMessage(message.getText(), currentUser);
    }

    /**
     * Method that searches user in DB and if there is no such user creates one
     * @param name String Firstname + Lastname
     * @param username String Username
     * @param chatId End user chat id
     * @param message String message
     * @return        Returns TelegramUser entity
     */
    private TelegramUser getUser(String name, String username, String chatId, String message){

        TelegramUser user;
        if (dbManager.userExists(chatId)) {
            user = dbManager.userByChatId(chatId);
            user.getBotState().setLastAnswer(message);
            dbManager.saveState(user.getBotState());
            return user;
        }
        BotState state = new BotState(message, null, utils.questionList.get(0));
        user = new TelegramUser(
                name,
                username,
                chatId,
                null,
                "0,0",
                null,
                state,
                new ArrayList<>(utils.getEmptyList())
        );
        state.setUser(user);
        dbManager.saveUser(user);
        return user;
    }

}
