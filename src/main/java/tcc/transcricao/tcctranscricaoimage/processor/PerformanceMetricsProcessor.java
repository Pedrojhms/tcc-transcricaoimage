package tcc.transcricao.tcctranscricaoimage.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.WhatsAppConstants;
import tcc.transcricao.tcctranscricaoimage.model.PerformanceMetric;
import tcc.transcricao.tcctranscricaoimage.repository.PerformanceMetricRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceMetricsProcessor implements Processor {

    private final PerformanceMetricRepository metricRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            // Recupera os tempos do exchange
            long start = (long) exchange.getProperty(WhatsAppConstants.START_TIME_PROPERTY);
            long desc = (long) exchange.getProperty(WhatsAppConstants.DESC_TIME_PROPERTY);
            long tts = (long) exchange.getProperty(WhatsAppConstants.TTS_TIME_PROPERTY);
            long send = System.currentTimeMillis();
            String phone = (String) exchange.getProperty(WhatsAppConstants.PHONE_PROPERTY);

            // Calcula os tempos
            long tempoDescricao = desc - start;
            long tempoTts = tts - desc;
            long tempoEnvio = send - tts;
            long tempoTotal = send - start;

            // Cria e salva a métrica
            PerformanceMetric metric = new PerformanceMetric();
            metric.setTempoDescricao(tempoDescricao);
            metric.setTempoTts(tempoTts);
            metric.setTempoEnvio(tempoEnvio);
            metric.setTempoTotal(tempoTotal);
            metric.setPhone(phone);
            metric.setData(LocalDateTime.now());

            metricRepository.save(metric);

            log.info("Métricas salvas - Total: {}ms, Descrição: {}ms, TTS: {}ms, Envio: {}ms",
                    tempoTotal, tempoDescricao, tempoTts, tempoEnvio);

        } catch (Exception e) {
            log.error("Erro ao processar métricas de performance", e);
            throw e;
        }
    }
}