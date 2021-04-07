package com.newsforright.bot.persistence;

import com.newsforright.bot.entities.BotState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotStateRepository extends JpaRepository<BotState, Long> {
}
