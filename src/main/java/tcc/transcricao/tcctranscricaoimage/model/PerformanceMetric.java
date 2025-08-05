package tcc.transcricao.tcctranscricaoimage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
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

    @Column(name = "tempo_descricao")
    private long tempoDescricao;

    @Column(name = "tempo_tts")
    private long tempoTts;

    @Column(name = "tempo_envio")
    private long tempoEnvio;

    @Column(name = "tempo_total")
    private long tempoTotal;

    @Column(name = "phone")
    private String phone;

    @Column(name = "data")
    private LocalDateTime data;

}