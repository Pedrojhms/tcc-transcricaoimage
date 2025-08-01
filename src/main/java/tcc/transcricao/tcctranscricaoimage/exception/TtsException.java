package tcc.transcricao.tcctranscricaoimage.exception;

public class TtsException extends RuntimeException {
    public TtsException(String message) {
        super(message);
    }

    public TtsException(String message, Throwable cause) {
        super(message, cause);
    }
}