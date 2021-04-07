package com.newsforright.bot.entities;

import com.newsforright.bot.enums.Phase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity(name="BotState")
@Table(
        name="bot_states",
        uniqueConstraints = {
                @UniqueConstraint(name = "botstate_userid_unique", columnNames = "user_id")
        }
)
@NoArgsConstructor
@ToString
@Scope(value = "prototype")
public class BotState {

    @SequenceGenerator(
            name = "states_sequence",
            sequenceName = "states_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "states_sequence"
    )
    @Column(
            name="id",
            updatable = false,
            nullable = false
    )
    @Id
    @Getter @Setter
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="phase", nullable = false)
    @Getter @Setter
    private Phase phase;

    @Column(name="question_number", nullable = false)
    @Getter @Setter
    private Long questionNumber;

    @Column(
            name="last_answer",
            nullable = false,
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String lastAnswer;

    @OneToOne
    @Getter @Setter
    private TelegramUser user;

    public BotState(Phase phase, Long questionNumber, String lastAnswer, TelegramUser user) {
        this.phase = phase;
        this.questionNumber = questionNumber;
        this.lastAnswer = lastAnswer;
        this.user = user;
    }

    public BotState(String lastAnswer, TelegramUser user) {
        this.lastAnswer = lastAnswer;
        this.user = user;
        this.phase = Phase.PRESTART;
        this.questionNumber = 0L;
    }
}
