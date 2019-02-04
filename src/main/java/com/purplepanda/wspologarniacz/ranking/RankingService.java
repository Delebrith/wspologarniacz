package com.purplepanda.wspologarniacz.ranking;

public interface RankingService {
    Ranking addCategory(Ranking ranking, Category category);

    void deleteRanking(Ranking ranking);

    Ranking deleteCategory(Ranking ranking, Long categoryId);

    Ranking addPoints(Ranking ranking, Long categoryId, Integer points);

    Ranking getRanking(Long rankingId);

    Ranking modify(Ranking ranking, String name);
}
