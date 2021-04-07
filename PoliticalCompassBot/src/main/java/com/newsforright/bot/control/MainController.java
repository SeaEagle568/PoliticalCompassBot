package com.newsforright.bot.control;

import com.newsforright.bot.entities.BotState;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MainController {


    private AnswerHandler answerHandler;
    private CommandHandler commandHandler;
    private TelegramOutputService output;
    private DBManager dbManager;

    public TelegramOutputService getOutput() {
        return output;
    }

    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
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

    public void razgrebiUpdate(Update update) {
        Chat chat = update.getMessage().getChat();
        Message message = update.getMessage();
        if (!isText(message)) return;

        TelegramUser currentUser = getUser(chat, message.getText());
        BotState state = currentUser.getBotState();
        if (message.isCommand()) {
            commandHandler.parseMessage(message.getText(), currentUser, state);
        }
        else {
            answerHandler.parseMessage(message.getText(), currentUser, state);
        }




    }

    private boolean isText(Message message) {
        return message.hasText();
    }

    private TelegramUser getUser(Chat chat, String message){
        String chatId = chat.getId().toString();
        TelegramUser user;
        BotState state;
        if (dbManager.userExists(chatId)){
            user = dbManager.userByChatId(chatId);
            user.getBotState().setLastAnswer(message);
            state = user.getBotState();
        }
        else {
            state = new BotState(message, null);
            dbManager.saveState(state);

            user = new TelegramUser(chat.getFirstName() + " " + chat.getLastName(),
                    chat.getUserName(),
                    chatId,
                    null,
                    null,
                    null,
                    state,
                    null);

            dbManager.saveUser(user);

            state.setUser(user);

        }
        dbManager.saveState(state);
        return user;

    }
}
