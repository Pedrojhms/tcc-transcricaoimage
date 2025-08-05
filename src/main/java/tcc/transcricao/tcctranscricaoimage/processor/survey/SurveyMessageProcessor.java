package tcc.transcricao.tcctranscricaoimage.processor.survey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.SurveyConstants;

@Component
@RequiredArgsConstructor
@Slf4j
public class SurveyMessageProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String body = exchange.getIn().getBody(String.class);
            log.debug("Processando mensagem do questionário: {}", body);

            // Validação básica do JSON
            JSONObject json = new JSONObject(body);

            // Validação de campos obrigatórios
            validateRequiredFields(json);

            // Headers padrão
            exchange.getIn().setHeader(SurveyConstants.CONTENT_TYPE_HEADER, SurveyConstants.APPLICATION_JSON);

            log.debug("Mensagem validada e processada com sucesso");

        } catch (Exception e) {
            log.error("Erro ao processar mensagem do questionário", e);
            throw e;
        }
    }

    private void validateRequiredFields(JSONObject json) {
        // Se tem 'to' e 'message', é uma pergunta sendo enviada
        if (json.has("to") && json.has("message")) {
            validateQuestionFields(json);
        }
        // Se tem 'from' e 'score', é uma resposta
        else if (json.has("from") && json.has("score")) {
            validateResponseFields(json);
        }
        else {
            throw new IllegalArgumentException("Tipo de mensagem não reconhecido");
        }
    }

    private void validateQuestionFields(JSONObject json) {
        if (!json.has("imageId")) {
            throw new IllegalArgumentException("Campo 'imageId' é obrigatório para perguntas");
        }
        if (!json.has("to")) {
            throw new IllegalArgumentException("Campo 'to' é obrigatório para perguntas");
        }
        if (!json.has("message")) {
            throw new IllegalArgumentException("Campo 'message' é obrigatório para perguntas");
        }
    }

    private void validateResponseFields(JSONObject json) {
        if (!json.has("from")) {
            throw new IllegalArgumentException("Campo 'from' é obrigatório para respostas");
        }
        if (!json.has("imageId")) {
            throw new IllegalArgumentException("Campo 'imageId' é obrigatório para respostas");
        }
        if (!json.has("questionNumber")) {
            throw new IllegalArgumentException("Campo 'questionNumber' é obrigatório para respostas");
        }
        if (!json.has("score")) {
            throw new IllegalArgumentException("Campo 'score' é obrigatório para respostas");
        }
    }
}