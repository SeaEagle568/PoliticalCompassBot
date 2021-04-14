package com.newsforright.bot.control;

import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.enums.Answer;
import com.newsforright.bot.enums.Button;
import com.newsforright.bot.enums.Util;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
import com.newsforright.bot.util.CommonUtils;
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
            case PRESTART -> restartTest(message,currentUser, false);
            case GREETING -> greetingAnswer(message, currentUser);
            case TESTING -> handleQuizAnswer(message, currentUser);
            case SOCIAL -> sendResults(message, currentUser);
            case RESULTS -> restartTest(message, currentUser, true);
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

        //If BACK print previous question UNUSED
        /*
        if (button.equals(Util.BACK) &&

                currentUser.getBotState().getCurrentQuestion().getNumber() != 1) {

            goBack(currentUser);
            return;
        }
        */

        //No button found, random text -> ignore
        if (button.getButtonType().equals("UTIL")) return;

        //Else go to next question
        assert button instanceof Answer;
        goForward((Answer) button, currentUser);
    }

    private void restartTest(String message, TelegramUser currentUser, boolean needCheck) {
        if (!message.equals(Util.RESTART.getText()) && needCheck) return;

        output.printGreeting(currentUser.getChatId());
        dbManager.nextPhase(currentUser.getBotState());
    }

    /**
     * Annihilates results from last question, then asks quizController to do something
     * @param currentUser Telegram user who send message
     */
    private void goBack(TelegramUser currentUser){
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
        currentUser.getAnswers().set(questionNumber-1, buttonValue);

        if (questionNumber == utils.LAST_QUESTION.intValue()) { //if last show results
            sendSocialForm(currentUser);
        }
        else quizController.askNext(currentUser); //else go next
    }

    private void startTest(TelegramUser currentUser) {
        dbManager.nextPhase(currentUser.getBotState());
        quizController.startQuiz(currentUser);
    }

    private void sendSocialForm(TelegramUser currentUser){
        dbManager.nextPhase(currentUser.getBotState());
        output.askGoogleForm(currentUser);
        dbManager.saveUser(currentUser);
    }

    private void sendResults(String message, TelegramUser currentUser){
        if (!message.equals(Util.RESULTS.getText())) return;
        dbManager.nextPhase(currentUser.getBotState());
        quizController.showResults(currentUser);
    }

}
