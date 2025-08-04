package tcc.transcricao.tcctranscricaoimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tcc.transcricao.tcctranscricaoimage.model.PerformanceMetric;

public interface PerformanceMetricRepository extends JpaRepository<PerformanceMetric, Long> {
}