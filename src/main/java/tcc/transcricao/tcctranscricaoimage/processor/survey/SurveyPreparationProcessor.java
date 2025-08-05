package tcc.transcricao.tcctranscricaoimage.processor.survey;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.WhatsAppConstants;

@Component
@Slf4j
public class SurveyPreparationProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String phone = (String) exchange.getProperty(WhatsAppConstants.PHONE_PROPERTY);
            String imageId = (String) exchange.getProperty(WhatsAppConstants.IMAGE_ID_PROPERTY);

            // Validações
            if (phone == null || phone.trim().isEmpty()) {
                throw new IllegalStateException("Número de telefone não encontrado para iniciar questionário");
            }
            if (imageId == null || imageId.trim().isEmpty()) {
                throw new IllegalStateException("ID da imagem não encontrado para iniciar questionário");
            }

            JSONObject json = new JSONObject();
            json.put("phone", phone);
            json.put("imageId", imageId);

            exchange.getIn().setBody(json.toString());

            log.info("Questionário preparado para telefone: {} com imageId: {}", phone, imageId);

        } catch (Exception e) {
            log.error("Erro ao preparar questionário", e);
            throw e;
        }
    }
}