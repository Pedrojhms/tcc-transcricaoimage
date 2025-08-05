package tcc.transcricao.tcctranscricaoimage.processor.survey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.SurveyConstants;
import tcc.transcricao.tcctranscricaoimage.service.SatisfactionSurveyService;
import tcc.transcricao.tcctranscricaoimage.service.SurveyMessageBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class SurveyResponseProcessor implements Processor {

    private final SatisfactionSurveyService surveyService;
    private final SurveyMessageBuilder messageBuilder;

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String body = exchange.getIn().getBody(String.class);
            JSONObject json = new JSONObject(body);

            String userPhone = json.getString("from");
            String imageId = json.getString("imageId");
            int questionNumber = json.getInt("questionNumber");
            int score = json.getInt("score");

            log.info("Processando resposta - Usuário: {}, Pergunta: {}, Pontuação: {}",
                    userPhone, questionNumber, score);

            // Validação da pontuação
            if (!isValidScore(score)) {
                String errorMessage = messageBuilder.buildInvalidScoreMessage();
                buildErrorResponse(exchange, errorMessage);
                return;
            }

            // Salva a resposta
            surveyService.saveAnswer(userPhone, imageId, questionNumber, score);

            // Determina próxima ação
            JSONObject response = new JSONObject();
            if (questionNumber < SurveyConstants.TOTAL_QUESTIONS) {
                // Próxima pergunta
                String nextQuestion = messageBuilder.buildQuestionMessage(questionNumber + 1);
                response.put("message", nextQuestion);
                response.put("finished", false);
                response.put("nextQuestion", questionNumber + 1);

                log.debug("Enviando pergunta {} para usuário {}", questionNumber + 1, userPhone);
            } else {
                // Questionário finalizado
                String completionMessage = messageBuilder.buildCompletionMessage();
                response.put("message", completionMessage);
                response.put("finished", true);

                log.info("Questionário finalizado para usuário: {}", userPhone);
            }

            exchange.getIn().setHeader(SurveyConstants.CONTENT_TYPE_HEADER, SurveyConstants.APPLICATION_JSON);
            exchange.getIn().setBody(response.toString());

        } catch (Exception e) {
            log.error("Erro ao processar resposta do questionário", e);
            buildErrorResponse(exchange, "❌ Erro interno. Tente novamente.");
            throw e;
        }
    }

    private boolean isValidScore(int score) {
        return score >= SurveyConstants.MIN_SCORE && score <= SurveyConstants.MAX_SCORE;
    }

    private void buildErrorResponse(Exchange exchange, String errorMessage) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("message", errorMessage);
        errorResponse.put("error", true);

        exchange.getIn().setHeader(SurveyConstants.CONTENT_TYPE_HEADER, SurveyConstants.APPLICATION_JSON);
        exchange.getIn().setBody(errorResponse.toString());
    }
}