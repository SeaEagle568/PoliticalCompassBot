package com.newsforright.bot.persistence;

import com.newsforright.bot.entities.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    Boolean existsByChatId(String chatId);
    TelegramUser findByChatId(String chatId);
}
