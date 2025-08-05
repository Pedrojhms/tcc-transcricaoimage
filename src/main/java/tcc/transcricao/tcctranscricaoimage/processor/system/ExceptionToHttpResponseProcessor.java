package tcc.transcricao.tcctranscricaoimage.processor.system;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.exception.DescricaoImagemException;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;

@Component
public class ExceptionToHttpResponseProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        if (exception instanceof DescricaoImagemException) {
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
            exchange.getIn().setBody("Erro ao descrever imagem: " + exception.getMessage());
        } else if (exception instanceof TtsException) {
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            exchange.getIn().setBody("Erro interno ao gerar áudio: " + exception.getMessage());
        } else {
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            exchange.getIn().setBody("Erro inesperado: " + exception.getMessage());
        }
    }
}
