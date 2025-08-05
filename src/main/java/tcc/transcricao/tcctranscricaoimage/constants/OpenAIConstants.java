package tcc.transcricao.tcctranscricaoimage.constants;

public final class OpenAIConstants {

    private OpenAIConstants() {} // Utility class

    // API Configuration
    public static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    public static final String GPT_4O_MODEL = "gpt-4o";
    public static final int MAX_TOKENS = 200;
    public static final int MAX_DESCRIPTION_WORDS = 100;

    // HTTP Headers
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String BROTLI_ENCODING = "br";

    // JSON Paths
    public static final String CHOICES_PATH = "choices";
    public static final String MESSAGE_PATH = "message";
    public static final String CONTENT_PATH = "content";

    // Error Messages
    public static final String OPENAI_ERROR_MESSAGE = "Erro ao chamar OpenAI API";
    public static final String BROTLI_DECOMPRESSION_ERROR = "Erro ao descompactar resposta Brotli";
    public static final String JSON_PARSING_ERROR = "Erro ao processar resposta JSON da OpenAI";
    public static final String INVALID_BASE64_ERROR = "Imagem base64 inválida ou vazia";
    public static final String EMPTY_RESPONSE_ERROR = "Resposta vazia da OpenAI";

    // Prompt System Message
    public static final String SYSTEM_PROMPT = """
            Descreva a imagem de forma clara, objetiva e sensorial, com até 100 palavras, 
            como se estivesse guiando uma pessoa com deficiência visual. Destaque os elementos 
            principais da cena, o contexto, as cores predominantes e detalhes visuais marcantes. 
            Se houver pessoas na imagem, não tente identificar quem são. Em vez disso, descreva 
            características visuais como expressões faciais, postura, cor e estilo das roupas, 
            penteado, tom de pele e outros traços visíveis, ressaltando os pontos fortes de sua 
            aparência de maneira respeitosa e inclusiva. Evite termos vagos como "bonito" ou 
            "agradável" e priorize uma descrição útil, empática e descritiva.""";

    // Image URL Template
    public static final String IMAGE_URL_TEMPLATE = "data:image/jpeg;base64,%s";
}