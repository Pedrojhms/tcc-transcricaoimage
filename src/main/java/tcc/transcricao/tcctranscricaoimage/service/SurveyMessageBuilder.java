package tcc.transcricao.tcctranscricaoimage.service;

import org.springframework.stereotype.Service;
import tcc.transcricao.tcctranscricaoimage.constants.SurveyConstants;

@Service
public class SurveyMessageBuilder {

    public String buildQuestionMessage(int questionNumber) {
        return switch (questionNumber) {
            case 1 -> buildQuestion1();
            case 2 -> buildQuestion2();
            case 3 -> buildQuestion3();
            case 4 -> buildQuestion4();
            case 5 -> buildQuestion5();
            default -> "";
        };
    }

    public String buildCompletionMessage() {
        return SurveyConstants.SURVEY_COMPLETION_MESSAGE;
    }

    public String buildInvalidScoreMessage() {
        return SurveyConstants.INVALID_SCORE_MESSAGE;
    }

    private String buildQuestion1() {
        return """
               ⏱️ *Pergunta 1/5*
               *Como você avalia o tempo que a aplicação levou para processar sua imagem?*

               1️⃣ Muito lento
               2️⃣ Lento
               3️⃣ Razoável
               4️⃣ Rápido
               5️⃣ Muito rápido

               📝 Digite sua resposta (1-5):""";
    }

    private String buildQuestion2() {
        return """
               🎯 *Pergunta 2/5*
               *Qual sua satisfação com a qualidade da descrição da imagem obtida?*

               1️⃣ Muito insatisfeito
               2️⃣ Insatisfeito
               3️⃣ Neutro
               4️⃣ Satisfeito
               5️⃣ Muito satisfeito

               📝 Digite sua resposta (1-5):""";
    }

    private String buildQuestion3() {
        return """
               🎯 *Pergunta 3/5*
               *O quão precisa você considera a descrição gerada?*

               1️⃣ Nada precisa
               2️⃣ Pouco precisa
               3️⃣ Razoavelmente precisa
               4️⃣ Muito precisa
               5️⃣ Extremamente precisa

               📝 Digite sua resposta (1-5):""";
    }

    private String buildQuestion4() {
        return """
               🎯 *Pergunta 4/5*
               *Como você avalia a facilidade de uso da aplicação?*

               1️⃣ Muito difícil
               2️⃣ Difícil
               3️⃣ Razoável
               4️⃣ Fácil
               5️⃣ Muito fácil

               📝 Digite sua resposta (1-5):""";
    }

    private String buildQuestion5() {
        return """
               🎯 *Pergunta 5/5*
               *Qual sua satisfação geral com o resultado obtido?*

               1️⃣ Muito insatisfeito
               2️⃣ Insatisfeito
               3️⃣ Neutro
               4️⃣ Satisfeito
               5️⃣ Muito satisfeito

               📝 Digite sua resposta (1-5):""";
    }
}