package tcc.transcricao.tcctranscricaoimage.route;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.WhatsAppConstants;
import tcc.transcricao.tcctranscricaoimage.exception.DescricaoImagemException;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;
import tcc.transcricao.tcctranscricaoimage.processor.*;

@Component
@RequiredArgsConstructor
public class DescribeImageRoute extends RouteBuilder {

    private final WhatsAppWebhookProcessor webhookProcessor;
    private final ConfirmationProcessor confirmationProcessor;
    private final ImageAudioProcessor imageAudioProcessor;
    private final VoiceMessageProcessor voiceMessageProcessor;
    private final PerformanceMetricsProcessor performanceMetricsProcessor;
    private final SurveyPreparationProcessor surveyPreparationProcessor;
    private final ExceptionToHttpResponseProcessor exceptionToHttpResponseProcessor;

    @Override
    public void configure() {

        // Configuração global de tratamento de erros
        onException(DescricaoImagemException.class)
                .handled(true)
                .log("Erro de descrição de imagem: ${exception.message}")
                .process(exceptionToHttpResponseProcessor);

        onException(TtsException.class)
                .handled(true)
                .log("Erro de síntese de voz: ${exception.message}")
                .process(exceptionToHttpResponseProcessor);

        onException(Exception.class)
                .handled(true)
                .log("Erro geral no processamento: ${exception.message}")
                .process(exceptionToHttpResponseProcessor);

        // Route Principal: Webhook do WhatsApp
        from(WhatsAppConstants.WEBHOOK_ENDPOINT)
                .routeId("whatsapp-webhook-route")
                .log(WhatsAppConstants.WEBHOOK_RECEIVED_LOG)
                .process(webhookProcessor)
                .wireTap(WhatsAppConstants.SEND_CONFIRMATION_ENDPOINT)
                .to(WhatsAppConstants.PROCESS_IMAGE_AUDIO_ENDPOINT)
                .to(WhatsAppConstants.SEND_VOICE_ENDPOINT)
                .to(WhatsAppConstants.DB_METRICS_ENDPOINT)
                .log(WhatsAppConstants.METRICS_SAVED_LOG)
                .process(surveyPreparationProcessor)
                .to(WhatsAppConstants.START_SURVEY_ENDPOINT)
                .setBody(constant("OK"));

        // Route: Envio de Confirmação
        from(WhatsAppConstants.SEND_CONFIRMATION_ENDPOINT)
                .routeId("send-confirmation-route")
                .process(confirmationProcessor)
                .to(WhatsAppConstants.WHATSAPP_SEND_TEXT_URL);

        // Route: Processamento de Imagem e Áudio
        from(WhatsAppConstants.PROCESS_IMAGE_AUDIO_ENDPOINT)
                .routeId("process-image-audio-route")
                .log(WhatsAppConstants.IMAGE_PROCESSING_START_LOG)
                .process(imageAudioProcessor)
                .log(WhatsAppConstants.DESCRIPTION_GENERATED_LOG)
                .log(WhatsAppConstants.AUDIO_GENERATED_LOG);

        // Route: Envio de Mensagem de Voz
        from(WhatsAppConstants.SEND_VOICE_ENDPOINT)
                .routeId("send-voice-route")
                .log(WhatsAppConstants.SENDING_VOICE_LOG)
                .process(voiceMessageProcessor)
                .to(WhatsAppConstants.WHATSAPP_SEND_VOICE_URL);

        // Route: Salvamento de Métricas
        from(WhatsAppConstants.DB_METRICS_ENDPOINT)
                .routeId("db-metrics-route")
                .log(WhatsAppConstants.DB_METRICS_START_LOG)
                .process(performanceMetricsProcessor);
    }
}