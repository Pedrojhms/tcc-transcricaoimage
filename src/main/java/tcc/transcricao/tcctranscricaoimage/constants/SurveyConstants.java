package tcc.transcricao.tcctranscricaoimage.constants;

public final class SurveyConstants {

    private SurveyConstants() {} // Utility class

    // Endpoints
    public static final String WHATSAPP_SEND_TEXT_URL = "http://whatsapp:3000/sendText?bridgeEndpoint=true&throwExceptionOnFailure=false";
    public static final String START_SURVEY_ENDPOINT = "direct:start-survey";
    public static final String SURVEY_RESPONSE_ENDPOINT = "rest:POST:/whatsapp-survey";

    // Headers
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    // Survey Configuration
    public static final int TOTAL_QUESTIONS = 5;
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    // Log Messages
    public static final String SURVEY_STARTED_LOG = "Primeira pergunta do questionário enviada.";
    public static final String RESPONSE_PROCESSED_LOG = "Resposta processada e próxima pergunta retornada.";

    // Survey Messages
    public static final String SURVEY_COMPLETION_MESSAGE = "✅ Obrigado por responder ao questionário!";
    public static final String INVALID_SCORE_MESSAGE = "❌ Valor inválido. Digite apenas números de 1 a 5.";
}