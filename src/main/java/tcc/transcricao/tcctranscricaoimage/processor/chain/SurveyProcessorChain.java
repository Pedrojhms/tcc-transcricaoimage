package tcc.transcricao.tcctranscricaoimage.processor.chain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.processor.survey.SurveyMessageProcessor;
import tcc.transcricao.tcctranscricaoimage.processor.survey.SurveyPreparationProcessor;
import tcc.transcricao.tcctranscricaoimage.processor.survey.SurveyProcessor;
import tcc.transcricao.tcctranscricaoimage.processor.survey.SurveyResponseProcessor;

/**
 * Chain de processors relacionados ao sistema de pesquisa
 * Centraliza operações de pesquisa, preparação, resposta e mensagens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SurveyProcessorChain {

    private final SurveyProcessor surveyProcessor;
    private final SurveyResponseProcessor surveyResponseProcessor;
    private final SurveyPreparationProcessor surveyPreparationProcessor;
    private final SurveyMessageProcessor surveyMessageProcessor;

    /**
     * Processor principal de pesquisa
     * @return SurveyProcessor configurado
     */
    public Processor getSurveyProcessor() {
        log.debug("Acessando Survey Processor");
        return surveyProcessor;
    }

    /**
     * Processor para respostas de pesquisa
     * @return SurveyResponseProcessor configurado
     */
    public Processor getResponseProcessor() {
        log.debug("Acessando Survey Response Processor");
        return surveyResponseProcessor;
    }

    /**
     * Processor para preparação de pesquisa
     * @return SurveyPreparationProcessor configurado
     */
    public Processor getPreparationProcessor() {
        log.debug("Acessando Survey Preparation Processor");
        return surveyPreparationProcessor;
    }

    /**
     * Processor para mensagens de pesquisa
     * @return SurveyMessageProcessor configurado
     */
    public Processor getMessageProcessor() {
        log.debug("Acessando Survey Message Processor");
        return surveyMessageProcessor;
    }

    /**
     * Valida se todos os processors estão disponíveis
     * @return true se todos os processors estão configurados
     */
    public boolean isChainReady() {
        boolean ready = surveyProcessor != null &&
                surveyResponseProcessor != null &&
                surveyPreparationProcessor != null &&
                surveyMessageProcessor != null;

        log.debug("Survey Processor Chain ready: {}", ready);
        return ready;
    }
}