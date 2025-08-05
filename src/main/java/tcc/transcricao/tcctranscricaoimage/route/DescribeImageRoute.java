package tcc.transcricao.tcctranscricaoimage.route;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.SurveyConstants;
import tcc.transcricao.tcctranscricaoimage.constants.WhatsAppConstants;
import tcc.transcricao.tcctranscricaoimage.exception.DescricaoImagemException;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;
import tcc.transcricao.tcctranscricaoimage.processor.chain.ProcessorRegistry;

@Component
@RequiredArgsConstructor
public class DescribeImageRoute extends RouteBuilder {

    private final ProcessorRegistry processorRegistry;

    @Override
    public void configure() {

        // Log do status das chains na inicialização
        processorRegistry.logChainStatus();

        // Tratamento de erros
        onException(DescricaoImagemException.class, TtsException.class, Exception.class)
                .handled(true)
                .log("Erro no processamento: ${exception.message}")
                .process(processorRegistry.getSystemChain().getExceptionProcessor());

        // Route Principal
        from(WhatsAppConstants.WEBHOOK_ENDPOINT)
                .routeId("whatsapp-webhook-route")
                .log(WhatsAppConstants.WEBHOOK_RECEIVED_LOG)
                .process(processorRegistry.getWhatsAppChain().getWebhookProcessor())
                .log("Property inicio time: " + exchangeProperty(WhatsAppConstants.START_TIME_PROPERTY).toString())
                .wireTap(WhatsAppConstants.SEND_CONFIRMATION_ENDPOINT)
                .to(WhatsAppConstants.PROCESS_IMAGE_AUDIO_ENDPOINT)
                .to(WhatsAppConstants.SEND_VOICE_ENDPOINT)
                .to(WhatsAppConstants.DB_METRICS_ENDPOINT)
                .process(processorRegistry.getSurveyChain().getPreparationProcessor())
                .to(SurveyConstants.START_SURVEY_ENDPOINT)
                .setBody(constant("OK"));

        // Sub-routes
        from(WhatsAppConstants.SEND_CONFIRMATION_ENDPOINT)
                .routeId("send-confirmation")
                .process(processorRegistry.getWhatsAppChain().getConfirmationProcessor())
                .to(WhatsAppConstants.WHATSAPP_SEND_TEXT_URL)
                .log(WhatsAppConstants.CONFIRMATION_RECEIVED_LOG);

        from(WhatsAppConstants.PROCESS_IMAGE_AUDIO_ENDPOINT)
                .routeId("process-image-audio")
                .process(processorRegistry.getSystemChain().getImageAudioProcessor())
                .log(WhatsAppConstants.DESCRIPTION_GENERATED_LOG);

        from(WhatsAppConstants.SEND_VOICE_ENDPOINT)
                .routeId("send-voice")
                .process(processorRegistry.getWhatsAppChain().getVoiceMessageProcessor())
                .to(WhatsAppConstants.WHATSAPP_SEND_VOICE_URL)
                .log(WhatsAppConstants.AUDIO_GENERATED_LOG);

        from(WhatsAppConstants.DB_METRICS_ENDPOINT)
                .routeId("save-metrics")
                .process(processorRegistry.getSystemChain().getMetricsProcessor())
                .log(WhatsAppConstants.METRICS_SAVED_LOG);
    }
}