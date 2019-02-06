package com.purplepanda.wspologarniacz.api;

import com.purplepanda.wspologarniacz.api.model.CategoryDto;
import com.purplepanda.wspologarniacz.api.model.RankingDto;
import com.purplepanda.wspologarniacz.api.model.UserDto;
import com.purplepanda.wspologarniacz.ranking.Ranking;
import com.purplepanda.wspologarniacz.ranking.RankingMapper;
import com.purplepanda.wspologarniacz.ranking.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public class RankingApiDelegateImpl implements RankingApiDelegate {

    private final RankingService rankingService;
    private final RankingMapper rankingMapper = RankingMapper.getInstance();

    @Autowired
    public RankingApiDelegateImpl(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @Override
    public ResponseEntity<Void> addCategory(Long rankingId, CategoryDto category) {
        Ranking ranking = rankingService.getRanking(rankingId);
        ranking = rankingService.addCategory(ranking, rankingMapper.fromDto(category));
        return ResponseEntity.created(URI.create("/ranking/" + ranking.getId() + "/find")).build();
    }

    @Override
    public ResponseEntity<Void> addPoints(Long categoryId, Integer points, Long rankingId) {
        Ranking ranking = rankingService.getRanking(rankingId);
        rankingService.addPoints(ranking, categoryId, points);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteCategory(Long categoryId, Long rankingId) {
        Ranking ranking = rankingService.getRanking(rankingId);
        rankingService.deleteCategory(ranking, categoryId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteRanking(Long rankingId) {
        Ranking ranking = rankingService.getRanking(rankingId);
        rankingService.deleteRanking(ranking);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RankingDto> getRanking(Long rankingId) {
        return ResponseEntity.ok(
                rankingMapper.toDto(rankingService.getRanking(rankingId))
        );
    }

    @Override
    public ResponseEntity<RankingDto> modify(Long rankingId, String name, List<UserDto> participants) {
        Ranking ranking = rankingService.getRanking(rankingId);
        return ResponseEntity.ok(
               rankingMapper.toDto(rankingService.modify(ranking, name))
        );
    }
}
