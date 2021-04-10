package com.newsforright.bot.control;

import com.newsforright.bot.entities.Question;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.enums.Axe;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
import com.newsforright.bot.util.CommonUtils;
import com.newsforright.bot.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Class that is responsible for getting user through quiz
 * Strange architecture but ok (name change required)
 *
 * @author seaeagle
 */
@Service
public class QuizController {

    private TelegramOutputService output;
    private DBManager dbManager;
    private CommonUtils utils;

    @Autowired
    public void setUtils(CommonUtils utils) { this.utils = utils; }
    @Autowired
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }
    @Autowired
    public void setOutput(TelegramOutputService output) {
        this.output = output;
    }

    /**
     * Simple method that starts main quiz from the beginning
     * @param currentUser TelegramUser who sent a message
     */
    public void startQuiz(TelegramUser currentUser) {
        currentUser.getBotState().setCurrentQuestion(utils.questionList.get(0)); //Set current question to first
        output.askQuestion(
                currentQuestionText(currentQuestion(currentUser)),
                currentUser.getChatId(),
                true
        );
        dbManager.saveUser(currentUser);
    }

    /**
     * Method that asks user next question
     * Used when user answer with any except a BACK button
     * @param currentUser TelegramUser who sent a message
     */
    public void askNext(TelegramUser currentUser) {

        Question question = currentQuestion(currentUser);
        //Get next question from the list
        int questionIndex = Math.toIntExact(question.getNumber());
        //So here we have 0-base index meet 1-base index, and +1 is implicit
        Question nextQuestion = utils.questionList.get(questionIndex); ///+1
        output.askQuestion(
                currentQuestionText(nextQuestion),
                currentUser.getChatId(),
                false
        );
        currentUser.getBotState().setCurrentQuestion(nextQuestion);
        dbManager.saveUser(currentUser);
    }

    /**
     * Method that asks user previous question
     * Used when user answer with a BACK button
     * @param currentUser TelegramUser who sent a message
     */
    public void askPrevious(TelegramUser currentUser) {
        Question question = currentQuestion(currentUser);
        int questionIndex = Math.toIntExact(question.getNumber());
        if (questionIndex == 1) return; //NO PREVIOUS QUESTION
        //Get previous question from the list
        //Same problem as above, 0-base index and 1-base, so to go back we need (-2)
        Question nextQuestion = utils.questionList.get(questionIndex - 2); /// -1
        output.askQuestion(
                currentQuestionText(nextQuestion),
                currentUser.getChatId(),
                (questionIndex == 2)
        );
        currentUser.getBotState().setCurrentQuestion(nextQuestion);
        dbManager.saveUser(currentUser);
    }

    /**
     * Obviously a method that is responsible for calculating and printing results
     * TODO: implement text results
     * @param currentUser TelegramUser who sent a message
     */
    public void showResults(TelegramUser currentUser) {
        Pair<Integer, Integer> results = utils.parseResults(currentUser.getResult());
        Pair<Double, Double> finalResults = new Pair<>(
                (100 * (double) (utils.MAX_SCORE_ECON + results.first) / (double) (2 * utils.MAX_SCORE_ECON)),
                (100 * (double) (utils.MAX_SCORE_POLI + results.second) / (double) (2 * utils.MAX_SCORE_POLI))
        );
        output.sendResults(currentUser.getChatId(),
                utils.getCompassWithDot(finalResults));

        dbManager.saveUser(currentUser);
    }

    private Question currentQuestion(TelegramUser currentUser) {
        return currentUser.getBotState().getCurrentQuestion();
    }

    /**
     * Decor question text before printing
     * @param question current Question
     * @return         String in format:
     *
     * Тема : %тема%
     * Запитання %номер%
     *
     * %Власне запитання%
     */
    private String currentQuestionText(Question question) {
        String theme;

        if (question.getAxe() == Axe.ECONOMICAL) theme = "\"Економічна свобода\"";
        else theme = "\"Політична свобода\"";

        return "Тема " + theme
                + "\nЗапитання " + question.getNumber()
                + ":\n\n" + question.getText();
    }


}
