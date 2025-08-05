package tcc.transcricao.tcctranscricaoimage.constants;

public final class WhatsAppConstants {

    private WhatsAppConstants() {} // Utility class

    // Endpoints WhatsApp
    public static final String WHATSAPP_SEND_TEXT_URL = "http://whatsapp:3000/sendText?bridgeEndpoint=true&throwExceptionOnFailure=false";
    public static final String WHATSAPP_SEND_VOICE_URL = "http://whatsapp:3000/sendVoice?bridgeEndpoint=true&throwExceptionOnFailure=false";

    // Endpoints Internos
    public static final String WEBHOOK_ENDPOINT = "rest:POST:/whatsapp-webhook";
    public static final String SEND_CONFIRMATION_ENDPOINT = "direct:send-confirmation";
    public static final String PROCESS_IMAGE_AUDIO_ENDPOINT = "direct:process-image-and-audio";
    public static final String SEND_VOICE_ENDPOINT = "direct:send-whatsapp-voice";
    public static final String DB_METRICS_ENDPOINT = "direct:detail-db-metrics";


    // Headers
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    // Exchange Properties
    public static final String START_TIME_PROPERTY = "startTime";
    public static final String IMAGE_ID_PROPERTY = "imageId";
    public static final String PHONE_PROPERTY = "phone";
    public static final String IMAGE_BASE64_PROPERTY = "imageBase64";
    public static final String DESCRIPTION_PROPERTY = "descricao";
    public static final String AUDIO_BASE64_PROPERTY = "audioBase64";
    public static final String DESC_TIME_PROPERTY = "descTime";
    public static final String TTS_TIME_PROPERTY = "ttsTime";

    // Messages
    public static final String CONFIRMATION_MESSAGE = "Imagem recebida com sucesso! Estamos processando sua solicitação.";


    // Log Messages
    public static final String WEBHOOK_RECEIVED_LOG = "Webhook do whatsapp recebido!";
    public static final String METRICS_SAVED_LOG = "Métricas salvas na base de dados!";
    public static final String DB_METRICS_START_LOG = "Iniciando log DB métricas";
    public static final String IMAGE_PROCESSING_START_LOG = "Iniciando processamento de imagem e síntese de áudio";
    public static final String DESCRIPTION_GENERATED_LOG = "Descrição gerada!";
    public static final String AUDIO_GENERATED_LOG = "Áudio gerado e disponibilizado!";
    public static final String CONFIRMATION_RECEIVED_LOG = "Imagem recebida com sucesso!";
}