package tcc.transcricao.tcctranscricaoimage.processor.chain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.processor.system.ExceptionToHttpResponseProcessor;
import tcc.transcricao.tcctranscricaoimage.processor.system.ImageAudioProcessor;
import tcc.transcricao.tcctranscricaoimage.processor.system.PerformanceMetricsProcessor;

/**
 * Chain de processors de sistema
 * Centraliza operações de métricas, exceções e processamento de imagem/áudio
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemProcessorChain {

    private final PerformanceMetricsProcessor performanceMetricsProcessor;
    private final ExceptionToHttpResponseProcessor exceptionToHttpResponseProcessor;
    private final ImageAudioProcessor imageAudioProcessor;

    /**
     * Processor de métricas de performance
     * @return PerformanceMetricsProcessor configurado
     */
    public Processor getMetricsProcessor() {
        log.debug("Acessando Performance Metrics Processor");
        return performanceMetricsProcessor;
    }

    /**
     * Processor para tratamento de exceções
     * @return ExceptionToHttpResponseProcessor configurado
     */
    public Processor getExceptionProcessor() {
        log.debug("Acessando Exception to HTTP Response Processor");
        return exceptionToHttpResponseProcessor;
    }

    /**
     * Processor para imagem e áudio
     * @return ImageAudioProcessor configurado
     */
    public Processor getImageAudioProcessor() {
        log.debug("Acessando Image Audio Processor");
        return imageAudioProcessor;
    }

    /**
     * Valida se todos os processors estão disponíveis
     * @return true se todos os processors estão configurados
     */
    public boolean isChainReady() {
        boolean ready = performanceMetricsProcessor != null &&
                exceptionToHttpResponseProcessor != null &&
                imageAudioProcessor != null;

        log.debug("System Processor Chain ready: {}", ready);
        return ready;
    }
}