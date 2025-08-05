package tcc.transcricao.tcctranscricaoimage.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.SurveyConstants;
import tcc.transcricao.tcctranscricaoimage.service.SurveyMessageBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class SurveyProcessor implements Processor {

    private final SurveyMessageBuilder messageBuilder;

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String body = exchange.getIn().getBody(String.class);
            JSONObject json = new JSONObject(body);

            String userPhone = json.getString("phone");
            String imageId = json.getString("imageId");

            log.info("Iniciando questionário para usuário: {} com imagem: {}", userPhone, imageId);

            String firstQuestion = messageBuilder.buildQuestionMessage(1);

            JSONObject payload = new JSONObject();
            payload.put("to", userPhone);
            payload.put("message", firstQuestion);
            payload.put("imageId", imageId);

            exchange.getIn().setHeader(SurveyConstants.CONTENT_TYPE_HEADER, SurveyConstants.APPLICATION_JSON);
            exchange.getIn().setBody(payload.toString());

            log.debug("Payload do questionário preparado para envio: {}", payload.toString());

        } catch (Exception e) {
            log.error("Erro ao processar início do questionário", e);
            throw e;
        }
    }
}