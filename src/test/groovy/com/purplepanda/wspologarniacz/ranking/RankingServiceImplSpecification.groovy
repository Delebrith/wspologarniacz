package com.purplepanda.wspologarniacz.ranking

import com.purplepanda.wspologarniacz.ranking.exception.CategoryNotFoundException
import com.purplepanda.wspologarniacz.ranking.exception.RankingNotFoundException
import com.purplepanda.wspologarniacz.user.User
import com.purplepanda.wspologarniacz.user.UserService
import spock.lang.Specification

class RankingServiceImplSpecification extends Specification {

    //tested
    RankingService rankingService

    //mocked
    UserService userService
    RankingRepository rankingRepository

    //test data
    User authenticated
    Ranking ranking
    Category category
    Score score

    void setup() {
        authenticated = User.builder()
            .id(1L)
            .name("user")
            .email("user@email.com")
            .build()

        score = Score.builder()
                    .id(1L)
                    .user(authenticated)
                    .points(1)
                    .build()

        category = Category.builder()
                    .id(1L)
                    .name("category 1")
                    .scores(Collections.singletonList(score).toSet())
                    .build()

        ranking = Ranking.builder()
            .name("ranking")
            .categories(Collections.singletonList(category).toSet())
            .build()
        ranking.id = 1L
        ranking.authorized = Collections.singletonList(authenticated).toSet()

        userService = Mock(UserService.class)
        rankingRepository = Mock(RankingRepository.class)

        rankingService = new RankingServiceImpl(rankingRepository, userService)
    }

    void "user should get information on existing ranking"() {
        given:
        rankingRepository.findById(ranking.id) >> Optional.ofNullable(ranking)
        when:
        Ranking result = rankingService.getRanking(ranking.id)
        then:
        result == ranking
    }

    void "user should get exception when requesting for non-existing ranking"() {
        given:
        rankingRepository.findById(ranking.id) >> Optional.empty()
        when:
        rankingService.getRanking(ranking.id)
        then:
        thrown(RankingNotFoundException.class)
    }

    void "user should successfully delete existing ranking"() {
        when:
        rankingService.deleteRanking(ranking)
        then:
        1 * rankingRepository.delete(ranking)
    }

    void "user should successfully modify ranking"() {
        given:
        String newName = "new name"
        rankingRepository.save(_) >> ranking
        when:
        Ranking result = rankingService.modify(ranking, newName)
        then:
        result.name == newName
    }

    void "user should successfully add category"() {
        given:
        rankingRepository.save(_) >> ranking
        Category newCategory = Category.builder()
                                    .name("category 2")
                                    .build()
        when:
        Ranking result = rankingService.addCategory(ranking, newCategory)
        then:
        result.categories.contains(newCategory)
    }

    void "user should successfully delete category"() {
        given:
        rankingRepository.save(ranking) >> ranking
        when:
        Ranking result = rankingService.deleteCategory(ranking, category.id)
        then:
        !result.categories.contains(category)
    }

    void "user should get exception when deleting non-existing category from ranking"() {
        when:
        rankingService.deleteCategory(ranking, 123)
        then:
        thrown(CategoryNotFoundException.class)
    }

    void "user should successfully add points to category"() {
        given:
        rankingRepository.save(_) >> ranking
        userService.getAuthenticatedUser() >> authenticated
        when:
        Ranking result = rankingService.addPoints(ranking, category.id, 1)
        then:
        result.categories.stream()
            .filter {c -> c.getId() == category.id}
            .flatMap {c -> c.getScores()}
            .filter {s -> s.getUser() == authenticated && s.getPoints() == 2}
    }

    void "user should get exception when adding points to non-existing category from ranking"() {
        given:
        userService.getAuthenticatedUser() >> authenticated
        when:
        Ranking result = rankingService.addPoints(ranking, 123, 1)
        then:
        thrown(CategoryNotFoundException.class)
    }
}
