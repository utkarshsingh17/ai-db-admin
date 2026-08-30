package ai.utkarsh.db_admin_assisstant.application.recommendation.command;

import ai.utkarsh.db_admin_assisstant.domain.recommendation.model.PerformanceRecommendation;

public final class CommandFactory {

    private CommandFactory() {
    }

    public static DatabaseChangeCommand fromRecommendation(PerformanceRecommendation recommendation) {
        return switch (recommendation.getType()) {
            case INDEX -> new CreateIndexCommand(recommendation.getProposedSql());
            case CONFIG_CHANGE -> new UpdateConfigParameterCommand(recommendation.getProposedSql());
            case QUERY_REWRITE -> throw new IllegalStateException(
                    "QUERY_REWRITE recommendations are advisory only and cannot be auto-applied");
        };
    }
}
