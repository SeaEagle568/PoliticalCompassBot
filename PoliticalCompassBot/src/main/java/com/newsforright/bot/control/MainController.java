package com.newsforright.bot.control;

import com.newsforright.bot.entities.BotState;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
import com.newsforright.bot.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

@Service
public class MainController {


    private AnswerHandler answerHandler;
    private CommandHandler commandHandler;
    private TelegramOutputService output;
    private DBManager dbManager;
    private CommonUtils utils;

    @Autowired
    public void setUtils(CommonUtils utils) {
        this.utils = utils;
    }

    @Value("${bot.resources.questions}")
    private String questionsFile;


    @Autowired
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
            commandHandler.parseMessage(message.getText(), currentUser);
        }
        else {
            answerHandler.parseMessage(message.getText(), currentUser);
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
            assert utils.questionList.get(0) != null;

            state = new BotState(message, null, utils.questionList.get(0));
            dbManager.saveState(state);

            user = new TelegramUser(chat.getFirstName() + " " + chat.getLastName(),
                    chat.getUserName(),
                    chatId,
                    null,
                    "0,0",
                    null,
                    state,
                    new ArrayList<Integer>(utils.getEmptyList())
            );

            dbManager.saveUser(user);

            state.setUser(user);

        }
        dbManager.saveState(state);
        return user;

    }

}
