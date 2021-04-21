package com.newsforright.bot.control;

import com.newsforright.bot.entities.Question;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.enums.Axe;
import com.newsforright.bot.persistence.DBManager;
import com.newsforright.bot.service.TelegramOutputService;
import com.newsforright.bot.util.CommonUtils;
import com.newsforright.bot.util.Ideology;
import com.newsforright.bot.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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
     * Obviously a method that is responsible for calculating and printing results
     * @param currentUser TelegramUser who sent a message
     */
    public void showResults(TelegramUser currentUser) {

        Pair<Integer, Integer> results = countResult(currentUser);
        Pair<Double, Double> finalResults = new Pair<>(
                (100 * (double) (utils.MAX_SCORE_ECON + results.first) / (double) (2 * utils.MAX_SCORE_ECON)),
                (100 * (double) (utils.MAX_SCORE_POLI + results.second) / (double) (2 * utils.MAX_SCORE_POLI))
        );
        updateResults(currentUser, finalResults);
        output.sendResults(currentUser.getChatId(),
                utils.getCompassWithDot(finalResults),
                textResults(finalResults),
                getAdMessage());

        dbManager.saveUser(currentUser);
    }

    private String getAdMessage() {
        return "Сподіваємося, вам спободався наш тест і ви задумалися над деякими фундаментальними питаннями, можливо вперше. Щоб закріпити результат знань подивіться це відео від нашої Ліберті Берегині - це найкраще, що ви знайдете на Ютубі! Зустрінемось в коментарях \uD83D\uDE09  \n" +
                "https://youtu.be/lgPYXZT5_XY";
    }

    /**
     * Get string with 4 nearest political ideologies
     * @param results pair of doubles - a dot
     * @return String text ready to send
     */
    private String textResults(Pair<Double, Double> results) {
        ArrayList<Ideology> ideologies = utils.getNearestDots(results);
        StringBuilder text = new StringBuilder("Ваша основна політична доктрина:\n");
        text.append(getDoctrine(results)).append("\n\n");
        text.append("А це - три ідеології які можуть вам підійти:\n\n");

        for (Ideology ideology : ideologies) {
            text.append("<i>").append(ideology.name).append("</i>\n");
        }
        text.append("\nЦі країни можуть підійти вам для життя:\n\n");
        if (results.first >= 33.33 && results.first <= 66.66
                && results.second >= 33.33 && results.second <= 66.66){
            text.append("Колумбія, Мексика чи можна залишатись в Україні - ми теж десь по центру осей економічної та персональної свобод.");
        }
        else if (results.first >= 50.0 && results.second >= 50.0){
            text.append("Дубай, Малайзія, Сінгапур, штати Техас, Індіана і Флорида (привіт, зброє) і для любителів повного відриву - поселення Аміші.");
        }
        else if (results.first <= 50.0 && results.second >= 50.0){
            text.append("Штати Нью-Йорк і Каліфорнія, Англія, Франція та інші країни Європи з  \"соціалістичним раєм\" на землі.");
        }
        else if (results.first <= 50.0 && results.second <= 50.0){
            text.append("Північна Корея, Росія - як справжній поціновувач принижень і несвободи, тут ви зробите чудову кар'єру.");
        }
        else {
            text.append("Китай, Саудівська Аравія, Ірак - тут ви спробуєте, що таке втручання держави у свободу індивіда яким воно є, на практиці. Удачі!");
        }
        return text.toString();
    }

    private String getDoctrine(Pair<Double, Double> results) {
        String res;
        if (results.first >= 33.33 && results.first <= 66.66
                && results.second >= 40.0 && results.second <= 66.66){
            res = "Лібералізм";
        }
        else if (results.first >= 33.33 && results.second > 50.0){
            res = "Лібертаріанство";
        }
        else if (results.first < 33.33 && results.second > 50.0){
            res = "Соціалізм";
        }
        else if (results.first < 50.0 && results.second < 50.0){
            res = "Коммунізм";
        }
        else {
            res = "Консератизм";
        }
        return "<b>" + res + "</b>";
    }

    private Pair<Integer, Integer> countResult(TelegramUser currentUser) {
        Integer sumEconomical = 0;
        Integer sumPolitical = 0;
        for (int i = 0; i < utils.LAST_QUESTION; i++) {
            if (utils.questionList.get(i).getAxe() == Axe.POLITICAL)
                sumPolitical += currentUser.getAnswers().get(i);
            else
                sumEconomical += currentUser.getAnswers().get(i);
        }
        return new Pair<>(sumEconomical, sumPolitical);
    }

    private void updateResults(TelegramUser currentUser, Pair<Double, Double> results){
        currentUser.setResult(utils.resultsToString(results));
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
        return "Запитання " + question.getNumber()
                + ":\n\n" + question.getText();
    }


}
