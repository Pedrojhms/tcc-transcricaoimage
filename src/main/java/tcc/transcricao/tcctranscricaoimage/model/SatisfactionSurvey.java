package tcc.transcricao.tcctranscricaoimage.model;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class SatisfactionSurvey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "image_id")
    private String imageId; // Relacionar à imagem/processamento

    @Column(name = "question_number")
    private Integer questionNumber; // 1..5

    @Column(name = "score")
    private Integer score; // 1..5

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}