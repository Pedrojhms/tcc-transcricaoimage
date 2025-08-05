package tcc.transcricao.tcctranscricaoimage.processor.chain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Registry central para acessar todas as chains de processors
 * Facilita injeção de dependências e controle centralizado
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessorRegistry {

    private final WhatsAppProcessorChain whatsAppChain;
    private final SurveyProcessorChain surveyChain;
    private final SystemProcessorChain systemChain;

    /**
     * Obtém a chain de processors do WhatsApp
     * @return WhatsAppProcessorChain configurada
     */
    public WhatsAppProcessorChain getWhatsAppChain() {
        log.debug("Acessando WhatsApp Processor Chain");
        return whatsAppChain;
    }

    /**
     * Obtém a chain de processors de pesquisa
     * @return SurveyProcessorChain configurada
     */
    public SurveyProcessorChain getSurveyChain() {
        log.debug("Acessando Survey Processor Chain");
        return surveyChain;
    }

    /**
     * Obtém a chain de processors de sistema
     * @return SystemProcessorChain configurada
     */
    public SystemProcessorChain getSystemChain() {
        log.debug("Acessando System Processor Chain");
        return systemChain;
    }

    /**
     * Valida se todas as chains estão prontas
     * @return true se todas as chains estão configuradas
     */
    public boolean areAllChainsReady() {
        boolean ready = whatsAppChain.isChainReady() &&
                surveyChain.isChainReady() &&
                systemChain.isChainReady();

        log.info("All Processor Chains ready: {}", ready);
        return ready;
    }

    /**
     * Logs detalhados sobre o status das chains
     */
    public void logChainStatus() {
        log.info("=== Processor Registry Status ===");
        log.info("WhatsApp Chain: {}", whatsAppChain.isChainReady() ? "✓ READY" : "✗ NOT READY");
        log.info("Survey Chain: {}", surveyChain.isChainReady() ? "✓ READY" : "✗ NOT READY");
        log.info("System Chain: {}", systemChain.isChainReady() ? "✓ READY" : "✗ NOT READY");
        log.info("================================");
    }
}