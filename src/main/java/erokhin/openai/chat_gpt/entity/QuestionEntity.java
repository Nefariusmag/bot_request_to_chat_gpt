package erokhin.openai.chat_gpt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "text", length = 5000)
    private String text;

    /**
     * Author of the text, can be user or response from OpenAI
     */
    @Column(name = "author")
    private String author;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "context_id")
    private Long contextId;

    @Column(name = "date_time")
    private LocalDateTime datetime;

}
