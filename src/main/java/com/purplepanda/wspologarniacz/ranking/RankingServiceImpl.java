package com.purplepanda.wspologarniacz.ranking;

import com.purplepanda.wspologarniacz.ranking.exception.CategoryNotFoundException;
import com.purplepanda.wspologarniacz.ranking.exception.RankingNotFoundException;
import com.purplepanda.wspologarniacz.user.User;
import com.purplepanda.wspologarniacz.user.UserService;
import com.purplepanda.wspologarniacz.user.authorization.InvalidModificationAttemptException;
import com.purplepanda.wspologarniacz.user.authorization.InvalidResourceStateException;
import com.purplepanda.wspologarniacz.user.authorization.ResourceAccessAuthorization;
import com.purplepanda.wspologarniacz.user.authorization.ResourceModificationAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class RankingServiceImpl implements RankingService {

    private final RankingRepository rankingRepository;
    private final UserService userServiceImpl;

    @Autowired
    public RankingServiceImpl(RankingRepository rankingRepository,
                              UserService userServiceImpl) {
        this.rankingRepository = rankingRepository;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    @ResourceModificationAuthorization
    public Ranking addCategory(Ranking ranking, Category category) {
        category.setScores(
                ranking.getAuthorized().stream()
                .map(u -> Score.builder()
                        .user(u)
                        .points(0)
                        .build())
                .collect(Collectors.toSet())
        );
        ranking.getCategories().add(category);
        return rankingRepository.save(ranking);
    }

    @Override
    @ResourceModificationAuthorization
    public void deleteRanking(Ranking ranking) {
        rankingRepository.delete(ranking);
    }

    @Override
    @ResourceModificationAuthorization
    public Ranking deleteCategory(Ranking ranking, Long categoryId) {
        Category removed = ranking.getCategories().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(CategoryNotFoundException::new);
        ranking.getCategories().remove(removed);
        return rankingRepository.save(ranking);
    }

    @Override
    @ResourceModificationAuthorization
    public Ranking addPoints(Ranking ranking, Long categoryId, Integer points) {
        ranking.getCategories().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findAny()
                .orElseThrow(CategoryNotFoundException::new)
                .getScores().stream()
                .filter(s -> s.getUser().equals(userServiceImpl.getAuthenticatedUser()))
                .forEach(s -> s.setPoints(s.getPoints() + points));

        return rankingRepository.save(ranking);
    }

    @Override
    @ResourceAccessAuthorization
    public Ranking getRanking(Long rankingId) {
        return rankingRepository.findById(rankingId).orElseThrow(RankingNotFoundException::new);
    }

    @Override
    @ResourceModificationAuthorization
    public Ranking modify(Ranking ranking, Optional<String> name, List<User> participants) {
        name.ifPresent(ranking::setName);
        if (!participants.isEmpty()){
            if (participants.stream().anyMatch(p -> !ranking.getAuthorized().contains(p)))
                throw new InvalidModificationAttemptException();

            //TODO
        }
        return rankingRepository.save(ranking);
    }
}
