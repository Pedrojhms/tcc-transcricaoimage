package tcc.transcricao.tcctranscricaoimage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "performance_metric", schema = "public")
public class PerformanceMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long tempoDescricao;
    private long tempoTts;
    private long tempoEnvio;
    private long tempoTotal;

    private String phone;
    private LocalDateTime data;

}