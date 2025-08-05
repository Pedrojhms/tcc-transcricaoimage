package tcc.transcricao.tcctranscricaoimage.route;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.SurveyConstants;
import tcc.transcricao.tcctranscricaoimage.processor.chain.ProcessorRegistry;

@Component
@RequiredArgsConstructor
public class SatisfactionSurveyRoute extends RouteBuilder {

    private final ProcessorRegistry processorRegistry;

    @Override
    public void configure() {

        // Tratamento de erros
        onException(Exception.class)
                .handled(true)
                .log("Erro no survey: ${exception.message}")
                .process(processorRegistry.getSystemChain().getExceptionProcessor());

        // ENDPOINT REST PARA RECEBER RESPOSTAS DO SURVEY
        from(SurveyConstants.SURVEY_RESPONSE_ENDPOINT)
                .routeId("whatsapp-survey-endpoint")
                .to(SurveyConstants.PROCESS_SURVEY_RESPONSE_ENDPOINT)
                .log(SurveyConstants.RESPONSE_PROCESSED_LOG);

        // Route Principal do Survey
        from(SurveyConstants.START_SURVEY_ENDPOINT)
                .routeId("satisfaction-survey-route")
                .process(processorRegistry.getSurveyChain().getSurveyProcessor())
                .to(SurveyConstants.SEND_SURVEY_MESSAGE_ENDPOINT)
                .log(SurveyConstants.SURVEY_STARTED_LOG);

        // Sub-routes do Survey
        from(SurveyConstants.SEND_SURVEY_MESSAGE_ENDPOINT)
                .routeId("send-survey-message")
                .process(processorRegistry.getSurveyChain().getMessageProcessor())
                .to(SurveyConstants.WHATSAPP_SEND_TEXT_URL)
                .log(SurveyConstants.SURVEY_MESSAGE_SENT_LOG);

        from(SurveyConstants.PROCESS_SURVEY_RESPONSE_ENDPOINT)
                .routeId("process-survey-response")
                .process(processorRegistry.getSurveyChain().getResponseProcessor())
                .log(SurveyConstants.SURVEY_RESPONSE_PROCESSED_LOG);
    }
}