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

/**
 * Handler class that is responsible for button answers
 *
 * @author seaeagle
 */
@Service
public class AnswerHandler {

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

    /**
     * Parsing message method called from MainController
     * @param message String input message
     * @param currentUser Telegram user who send message
     */
    public void parseMessage(String message, TelegramUser currentUser) {
        switch (currentUser.getBotState().getPhase()) {
            case GREETING -> greetingAnswer(message, currentUser);
            case TESTING -> handleQuizAnswer(message, currentUser);
            case RESULTS -> restartTest(message, currentUser);
        }

    }

    private void greetingAnswer(String message, TelegramUser currentUser) {
        if (!message.equals(Util.LETSGO.getText())) return;
        startTest(currentUser);
    }

    /**
     * Method that understands what button while test was clicked
     * And recounts result delegating the other to QuizController
     * @param message String input message
     * @param currentUser Telegram user who send message
     */
    private void handleQuizAnswer(String message, TelegramUser currentUser) {
        Button button = Button.getButton(message);

        //If BACK print previous question
        if (button.equals(Util.BACK)) {
            goBack(currentUser);
            return;
        }
        //No button found, random text -> ignore
        if (button.getButtonType().equals("UTIL")) return;

        //Else go to next question
        goForward((Answer) button, currentUser);
    }

    private void restartTest(String message, TelegramUser currentUser) {
        if (!message.equals(Util.RESTART.getText())) return;

        output.printGreeting(currentUser.getChatId());
        dbManager.nextPhase(currentUser.getBotState());
    }

    /**
     * Annihilates results from last question, then asks quizController to do something
     * @param currentUser Telegram user who send message
     */
    private void goBack(TelegramUser currentUser){
        int questionNumber = Math.toIntExact(currentUser.getBotState().getCurrentQuestion().getNumber());
        //We need -2 because current question is the one that on the screen, left unanswered
        Integer value = currentUser.getAnswers().get(questionNumber - 2);
        updateResults(currentUser, -value); //subtracting
        quizController.askPrevious(currentUser);
    }

    /**
     * Counts results and goes to next question
     * @param button Answer button that was pressed
     * @param currentUser Telegram user who send message
     */
    private void goForward(Answer button, TelegramUser currentUser){
        int questionNumber = Math.toIntExact(currentUser.getBotState().getCurrentQuestion().getNumber());
        boolean inverted = currentUser.getBotState().getCurrentQuestion().getInverted();

        Integer buttonValue = button.getValue(inverted);
        updateResults(currentUser, buttonValue);
        currentUser.getAnswers().set(questionNumber-1, buttonValue);

        if (questionNumber == utils.LAST_QUESTION.intValue()) { //if last show results
            quizController.showResults(currentUser);
            dbManager.nextPhase(currentUser.getBotState());
            //TODO: DELETE THIS WHEN SOCIAL IMPLEMENTED!!!
            dbManager.nextPhase(currentUser.getBotState());
        }
        else quizController.askNext(currentUser); //else go next
    }

    private void updateResults(TelegramUser currentUser, Integer value){
        Pair<Integer, Integer> currentResults = utils.parseResults(currentUser.getResult());
        Question currentQuestion = currentUser.getBotState().getCurrentQuestion();
        switch (currentQuestion.getAxe()) {
            case ECONOMICAL -> currentResults.first += value;
            case POLITICAL -> currentResults.second += value;
        }
        currentUser.setResult(utils.resultsToString(currentResults));

    }

    private void startTest(TelegramUser currentUser) {
        dbManager.nextPhase(currentUser.getBotState());
        quizController.startQuiz(currentUser);
    }

}
