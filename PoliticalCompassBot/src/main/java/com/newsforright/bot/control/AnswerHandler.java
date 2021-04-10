package com.newsforright.bot.control;

import com.newsforright.bot.entities.Question;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.enums.Answer;
import com.newsforright.bot.enums.Button;
import com.newsforright.bot.enums.Util;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
import com.newsforright.bot.util.CommonUtils;
import com.newsforright.bot.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerHandler {
    //TODO: code review

    private TelegramOutputService output;
    private DBManager dbManager;
    private QuizController quizController;
    private CommonUtils utils;

    @Autowired
    public void setUtils(CommonUtils utils) {
        this.utils = utils;
    }

    @Autowired
    public void setQuizController(QuizController quizController) {
        this.quizController = quizController;
    }

    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
    }

    public void parseMessage(String message, TelegramUser currentUser) {
        switch (currentUser.getBotState().getPhase()) {
            case GREETING -> greetingAnswer(message, currentUser);
            case TESTING -> handleQuizAnswer(message, currentUser);
            case RESULTS -> restartTest(message, currentUser);
        }

    }

    private void restartTest(String message, TelegramUser currentUser) {
        Button button = Button.getButton(message);
        if (!message.equals(Util.RESTART.getText())) return;

        output.printGreeting(currentUser.getChatId());
        dbManager.nextPhase(currentUser.getBotState());
    }

    private void goBack(TelegramUser currentUser){
        Question currentQuestion = currentUser.getBotState().getCurrentQuestion();
        Integer value = currentUser.getAnswers().get((int) (currentQuestion.getNumber()-2));
        updateResults(currentUser, -value);
        quizController.askPrevious(currentUser);
    }

    private void goForward(Answer button, TelegramUser currentUser){
        Question currentQuestion = currentUser.getBotState().getCurrentQuestion();
        Integer buttonValue = button.getValue(currentQuestion.getInverted());
        updateResults(currentUser, buttonValue);
        currentUser.getAnswers().set((int) (currentQuestion.getNumber()-1), buttonValue);
        if (currentQuestion.getNumber().equals(utils.LAST_QUESTION)) {
            quizController.showResults(currentUser);
            dbManager.nextPhase(currentUser.getBotState());
            //TODO: DELETE THIS WHEN SOCIAL IMPLEMENTED!!!
            dbManager.nextPhase(currentUser.getBotState());
        }
        else quizController.askNext(currentUser);
    }

    private void updateResults(TelegramUser currentUser, Integer value){
        Pair<Integer, Integer> currentResults = utils.parseResults(currentUser.getResult());
        Question currentQuestion = currentUser.getBotState().getCurrentQuestion();
        switch (currentQuestion.getAxe()) {
            case ECONOMICAL -> currentResults.setFirst(currentResults.getFirst() + value);
            case POLITICAL -> currentResults.setSecond(currentResults.getSecond() + value);
        }
        currentUser.setResult(utils.resultsToString(currentResults));

    }

    private void handleQuizAnswer(String message, TelegramUser currentUser) {
        Button button = Button.getButton(message);

        //If BACK print previous question
        if (button.equals(Util.BACK)) {
            goBack(currentUser);
            return;
        }
        //No button found, random text
        if (button.getButtonType().equals("UTIL")) return;
        goForward((Answer) button, currentUser);
    }

    private void greetingAnswer(String message, TelegramUser currentUser) {
        if (!message.equals(Util.LETSGO.getText())) return;
        startTest(currentUser);
    }

    private void startTest(TelegramUser currentUser) {
        dbManager.nextPhase(currentUser.getBotState());
        quizController.startQuiz(currentUser);
    }


}
