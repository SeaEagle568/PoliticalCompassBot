package com.newsforright.bot.persistence;

import com.newsforright.bot.entities.BotState;
import com.newsforright.bot.entities.Question;
import com.newsforright.bot.entities.TelegramUser;
import com.newsforright.bot.service.TelegramOutputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DBManager {
    private TelegramUserRepository userRepository;
    private BotStateRepository botStateRepository;
    private QuestionRepository questionRepository;

    //TODO: DELETE THIS!!
    @Autowired
    private TelegramOutputService output;

    @Autowired
    public void setUserRepository(TelegramUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    public void setBotStateRepository(BotStateRepository botStateRepository) {
        this.botStateRepository = botStateRepository;
    }
    @Autowired
    public void setQuestionRepository(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public TelegramUser userByChatId(String chatId) {
        return userRepository.findByChatId(chatId);
    }
    public boolean userExists(String chatId){
        return userRepository.existsByChatId(chatId);
    }

    public void saveUser(TelegramUser user){
        userRepository.save(user);
    }
    public void saveQuestions(List<Question> question){
        questionRepository.saveAll(question);
    }
    public void saveState(BotState state){
        botStateRepository.save(state);
    }

}
