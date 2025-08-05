package tcc.transcricao.tcctranscricaoimage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tcc.transcricao.tcctranscricaoimage.repository.SatisfactionSurveyRepository;
import tcc.transcricao.tcctranscricaoimage.model.SatisfactionSurvey;

import java.time.LocalDateTime;

@Service
public class SatisfactionSurveyService {
    @Autowired
    private SatisfactionSurveyRepository repo;

    // Salva resposta da pergunta
    public void saveAnswer(String userPhone, String imageId, int questionNumber, int score) {
        SatisfactionSurvey survey = new SatisfactionSurvey();
        survey.setUserPhone(userPhone);
        survey.setImageId(imageId);
        survey.setQuestionNumber(questionNumber);
        survey.setScore(score);
        survey.setAnsweredAt(LocalDateTime.now());
        repo.save(survey);
    }
}