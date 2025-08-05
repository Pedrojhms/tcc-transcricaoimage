package tcc.transcricao.tcctranscricaoimage.constants;

public final class TTSConstants {

    private TTSConstants() {} // Utility class

    // API Configuration
    public static final String OPENAI_TTS_API_URL = "https://api.openai.com/v1/audio/speech";
    public static final String TTS_MODEL = "tts-1";
    public static final String DEFAULT_VOICE = "alloy";

    // Available Voices
    public static final String VOICE_ALLOY = "alloy";
    public static final String VOICE_ECHO = "echo";
    public static final String VOICE_FABLE = "fable";
    public static final String VOICE_ONYX = "onyx";
    public static final String VOICE_NOVA = "nova";
    public static final String VOICE_SHIMMER = "shimmer";

    // HTTP Headers
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String APPLICATION_JSON = "application/json";

    // Limits
    public static final int MAX_TEXT_LENGTH = 4096; // OpenAI TTS limit
    public static final int MIN_TEXT_LENGTH = 1;

    // Error Messages
    public static final String TTS_API_ERROR_MESSAGE = "Erro ao chamar OpenAI TTS API";
    public static final String INVALID_TEXT_ERROR = "Texto inválido para síntese de voz";
    public static final String TEXT_TOO_LONG_ERROR = "Texto muito longo para síntese (máximo: " + MAX_TEXT_LENGTH + " caracteres)";
    public static final String EMPTY_TEXT_ERROR = "Texto vazio não pode ser sintetizado";
    public static final String API_KEY_ERROR = "Chave da API OpenAI TTS não configurada";
    public static final String EMPTY_RESPONSE_ERROR = "Resposta vazia da API TTS";
    public static final String AUDIO_PROCESSING_ERROR = "Erro ao processar áudio TTS";

    // Log Messages
    public static final String TTS_START_LOG = "Iniciando síntese de voz";
    public static final String TTS_SUCCESS_LOG = "Síntese de voz concluída com sucesso";
    public static final String PAYLOAD_BUILT_LOG = "Payload TTS construído";
    public static final String REQUEST_SENT_LOG = "Requisição TTS enviada";
    public static final String RESPONSE_PROCESSED_LOG = "Resposta TTS processada";
}