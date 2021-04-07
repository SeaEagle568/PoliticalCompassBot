package com.newsforright.bot.control;

import com.newsforright.bot.entities.BotState;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.enums.Util;
import com.newsforright.bot.service.TelegramOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerHandler {

    private TelegramOutputService output;

    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
    }

    public void parseMessage(String message, TelegramUser currentUser, BotState state) {
        switch (state.getPhase()){
            case GREETING:
                greetingAnswer(message, currentUser);
                break;
        }

    }

    private void greetingAnswer(String message, TelegramUser currentUser) {
        if (!message.equals(Util.LETSGO.getText())) return;
        startTest(currentUser);
    }

    private void startTest(TelegramUser currentUser) {
        //TODO: make a real start test
         output.printSimpleMessage("Ой-йой а де тест.....", currentUser.getChatId());
    }
}
