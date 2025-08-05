package tcc.transcricao.tcctranscricaoimage.route;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.SurveyConstants;
import tcc.transcricao.tcctranscricaoimage.processor.SurveyProcessor;
import tcc.transcricao.tcctranscricaoimage.processor.SurveyResponseProcessor;

@Component
@RequiredArgsConstructor
public class SatisfactionSurveyRoute extends RouteBuilder {

    private final SurveyProcessor surveyProcessor;
    private final SurveyResponseProcessor surveyResponseProcessor;

    @Override
    public void configure() throws Exception {

        // Configuração global de tratamento de erros
        onException(Exception.class)
                .handled(true)
                .log("Erro no questionário de satisfação: ${exception.message}")
                .setBody(constant("{\"error\": true, \"message\": \"Erro interno do servidor\"}"))
                .setHeader(SurveyConstants.CONTENT_TYPE_HEADER, constant(SurveyConstants.APPLICATION_JSON));

        // Route: Inicia o questionário - envia a primeira pergunta via WhatsApp
        from(SurveyConstants.START_SURVEY_ENDPOINT)
                .routeId("start-satisfaction-survey")
                .log("Iniciando questionário de satisfação")
                .process(surveyProcessor)
                .to(SurveyConstants.WHATSAPP_SEND_TEXT_URL)
                .log(SurveyConstants.SURVEY_STARTED_LOG);

        // Route: Recebe resposta do questionário, salva e retorna próxima pergunta OU finaliza
        from(SurveyConstants.SURVEY_RESPONSE_ENDPOINT)
                .routeId("process-satisfaction-survey-response")
                .log("Processando resposta do questionário de satisfação")
                .process(surveyResponseProcessor)
                .log(SurveyConstants.RESPONSE_PROCESSED_LOG);
    }
}