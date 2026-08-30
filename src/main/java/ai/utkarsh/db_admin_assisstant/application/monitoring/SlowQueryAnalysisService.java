package ai.utkarsh.db_admin_assisstant.application.monitoring;

import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.DatabaseId;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQueryEvent;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.model.SlowQuerySeverity;
import ai.utkarsh.db_admin_assisstant.domain.monitoring.service.SlowQueryClassifier;
import ai.utkarsh.db_admin_assisstant.domain.recommendation.port.in.RequestRecommendationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Decides which newly captured slow queries are worth an LLM call. Only SEVERE queries trigger an
 * automatic AI recommendation draft; MODERATE ones are still recorded and visible via the API, but
 * an admin has to request a recommendation for those manually — this bounds AI spend on noisy data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlowQueryAnalysisService {

    private final SlowQueryClassifier slowQueryClassifier;
    private final RequestRecommendationUseCase requestRecommendationUseCase;

    public void analyze(DatabaseId databaseId, List<SlowQueryEvent> newEvents) {
        for (SlowQueryEvent event : newEvents) {
            if (slowQueryClassifier.classify(event) == SlowQuerySeverity.SEVERE) {
                try {
                    requestRecommendationUseCase.requestForSlowQuery(databaseId, event.getId());
                } catch (Exception e) {
                    log.warn("AI recommendation drafting failed for slow query event {}", event.getId(), e);
                }
            }
        }
    }
}
