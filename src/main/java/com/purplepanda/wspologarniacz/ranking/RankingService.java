package com.purplepanda.wspologarniacz.ranking;

import com.purplepanda.wspologarniacz.user.User;

import java.util.List;
import java.util.Optional;

public interface RankingService {
    Ranking addCategory(Ranking ranking, Category category);

    void deleteRanking(Ranking ranking);

    Ranking deleteCategory(Ranking ranking, Long categoryId);

    Ranking addPoints(Ranking ranking, Long categoryId, Integer points);

    Ranking getRanking(Long rankingId);

    Ranking modify(Ranking ranking, Optional<String> name, List<User> participants);
}
