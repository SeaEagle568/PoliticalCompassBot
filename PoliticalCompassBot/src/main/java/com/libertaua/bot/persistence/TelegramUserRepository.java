package com.libertaua.bot.persistence;

import com.libertaua.bot.entities.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Personal repository for TelegramUser entity
 */
@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    Boolean existsByChatId(String chatId);
    TelegramUser findByChatId(String chatId);
}
