package com.newsforright.bot.entities;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.context.annotation.Scope;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity(name="TelegramUser")
@Table(
        name="telegram_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "users_chatid_unique", columnNames = "chat_id")
        }
)
@ToString
@NoArgsConstructor
@TypeDef(
        name = "list-array",
        typeClass = ListArrayType.class
)
@Scope(value = "prototype")
public class TelegramUser {

    @SequenceGenerator(
            name = "users_sequence",
            sequenceName = "users_sequence",
            allocationSize = 1
    )

    @Id
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "users_sequence"
    )
    @Column(
            name="id",
            updatable = false,
            nullable = false
    )

    @Getter @Setter
    private Long id;

    @Column(
            name="name",
            nullable = false,
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String name;

    @Column(
            name="username",
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String username;

    @Column(
            name="chat_id",
            nullable = false,
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String chatId;

    @Column(
            name="email",
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String email;

    @Column(
            name="result",
            columnDefinition = "TEXT"
    )
    @Getter @Setter
    private String result;

    @Column(name="social_data_id")
    @Getter @Setter
    private Long socialDataId;


    @OneToOne
    @JoinColumn(name = "bot_state_id", referencedColumnName = "id")
    @Getter @Setter
    private BotState botState;

    @Column(
            name="answer_list",
            columnDefinition = "integer[]"
    )
    @Type(type="list-array")
    @Getter @Setter
    private List<Integer> answers;

    public TelegramUser(String name,
                        String username,
                        String chatId,
                        String email,
                        String result,
                        Long socialDataId,
                        BotState botState,
                        List<Integer> answers) {

        this.name = name;
        this.username = username;
        this.chatId = chatId;
        this.email = email;
        this.result = result;
        this.socialDataId = socialDataId;
        this.botState = botState;
        this.answers = answers;
    }
}
