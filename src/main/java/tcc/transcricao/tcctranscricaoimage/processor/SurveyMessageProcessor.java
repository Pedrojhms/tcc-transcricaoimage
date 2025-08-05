package tcc.transcricao.tcctranscricaoimage.processor;

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
        if (!json.has("from")) {
            throw new IllegalArgumentException("Campo 'from' é obrigatório");
        }
        if (!json.has("imageId")) {
            throw new IllegalArgumentException("Campo 'imageId' é obrigatório");
        }
        if (!json.has("questionNumber")) {
            throw new IllegalArgumentException("Campo 'questionNumber' é obrigatório");
        }
        if (!json.has("score")) {
            throw new IllegalArgumentException("Campo 'score' é obrigatório");
        }
    }
}