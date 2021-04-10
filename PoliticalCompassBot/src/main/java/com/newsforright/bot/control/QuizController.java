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


    public void startQuiz(TelegramUser currentUser) {
        currentUser.getBotState().setCurrentQuestion(utils.questionList.get(0));
        output.askQuestion(
                currentQuestionText(currentQuestion(currentUser)),
                currentUser.getChatId(),
                true
        );
        dbManager.saveUser(currentUser);
    }


    public void askNext(TelegramUser currentUser) {
        Question question = currentQuestion(currentUser);
        //Get next question from the list
        int questionIndex = Math.toIntExact(question.getNumber());
        Question nextQuestion = utils.questionList.get(questionIndex); //+1
        currentUser.getBotState().setCurrentQuestion(nextQuestion);
        output.askQuestion(
                currentQuestionText(nextQuestion),
                currentUser.getChatId(),
                false
        );
        dbManager.saveUser(currentUser);
    }

    public void askPrevious(TelegramUser currentUser) {
        Question question = currentQuestion(currentUser);
        int questionIndex = Math.toIntExact(question.getNumber());
        if (questionIndex == 1) return;
        //Get previous question from the list
        Question nextQuestion = utils.questionList.get(questionIndex - 2);
        currentUser.getBotState().setCurrentQuestion(nextQuestion);
        output.askQuestion(
                currentQuestionText(nextQuestion),
                currentUser.getChatId(),
                (questionIndex == 2)
        );
        dbManager.saveUser(currentUser);
    }

    private Question currentQuestion(TelegramUser currentUser) {
        return currentUser.getBotState().getCurrentQuestion();
    }

    private String currentQuestionText(Question question) {
        String theme;

        if (question.getAxe() == Axe.ECONOMICAL) theme = "\"Економічна свобода\"";
        else theme = "\"Політична свобода\"";

        return "Тема " + theme
                + "\nЗапитання " + question.getNumber()
                + ":\n\n" + question.getText();
    }


    public void showResults(TelegramUser currentUser) {
        Pair<Integer, Integer> results = utils.parseResults(currentUser.getResult());
        Pair<Double, Double> finalResults = new Pair<Double, Double>(
                (100 * (double) (utils.MAX_SCORE_ECON + results.getFirst()) / (double) (2 * utils.MAX_SCORE_ECON)),
                (100 * (double) (utils.MAX_SCORE_POLI + results.getSecond()) / (double) (2 * utils.MAX_SCORE_POLI))
                );
        output.sendResults(currentUser.getChatId(), finalResults);
        dbManager.saveUser(currentUser);
    }

}
