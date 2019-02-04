package com.purplepanda.wspologarniacz.api

import com.purplepanda.wspologarniacz.api.model.AuthorityDto
import com.purplepanda.wspologarniacz.api.model.CategoryDto
import com.purplepanda.wspologarniacz.api.model.RankingDto
import com.purplepanda.wspologarniacz.api.model.ScoreDto
import com.purplepanda.wspologarniacz.api.model.UserDto
import com.purplepanda.wspologarniacz.ranking.Category
import com.purplepanda.wspologarniacz.ranking.Ranking
import com.purplepanda.wspologarniacz.ranking.RankingMapper
import com.purplepanda.wspologarniacz.ranking.RankingService
import com.purplepanda.wspologarniacz.ranking.Score
import com.purplepanda.wspologarniacz.ranking.exception.CategoryNotFoundException
import com.purplepanda.wspologarniacz.ranking.exception.RankingNotFoundException
import com.purplepanda.wspologarniacz.user.AuthorityName
import com.purplepanda.wspologarniacz.user.User
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class RankingApiDelegateSpecification extends Specification {

    //mocked
    private RankingService rankingService

    //testes
    private RankingApiDelegate rankingApiDelegate

    //test data
    private User authenticated
    private Ranking ranking
    private RankingDto rankingDto
    private UserDto userDto
    private ScoreDto scoreDto
    private CategoryDto categoryDto

    void setup() {
        rankingService = Mock(RankingService.class)
        rankingApiDelegate = new RankingApiDelegateImpl(rankingService)

        authenticated = User.builder()
                .id(1L)
                .name("user")
                .active(true)
                .authorities(Collections.singletonList(AuthorityName.USER))
                .build()

        ranking = Ranking.builder()
                .name("ranking 1")
                .categories(Collections.singletonList(
                    Category.builder()
                        .id(1L)
                        .name("category 1")
                        .scores(Collections.singletonList(
                            Score.builder()
                                .id(1L)
                                .user(authenticated)
                                .points(1)
                                .build()
                            ).toSet())
                        .build()
                    ).toSet())
                .build()
        ranking.id = 1L
        ranking.authorized = Collections.singletonList(authenticated).toSet()

        userDto = new UserDto()
                .id(1L)
                .name("user")
                .password("***")

        scoreDto =  new ScoreDto()
                .user(userDto)
                .points(1)

        categoryDto =  new CategoryDto()
                .id(1L)
                .name("category 1")
                .scores(Collections.singletonList(scoreDto))

        rankingDto = new RankingDto()
                        .id(1L)
                        .name("ranking 1")
                        .categories(Collections.singletonList(categoryDto))
    }

    void "user should get information on existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> ranking
        when:
        ResponseEntity result = rankingApiDelegate.getRanking(ranking.id)
        then:
        result.statusCode.is2xxSuccessful()
        result.getBody() == rankingDto
    }

    void "user should get exception when requesting for non-existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> {throw new RankingNotFoundException()}
        when:
        rankingApiDelegate.getRanking(ranking.id)
        then:
        thrown(RankingNotFoundException.class)
    }

    void "user should successfully delete existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> ranking
        when:
        ResponseEntity result = rankingApiDelegate.deleteRanking(ranking.id)
        then:
        result.statusCode.is2xxSuccessful()
    }

    void "user should get exception when deleting non-existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> {throw new RankingNotFoundException()}
        when:
        rankingApiDelegate.deleteRanking(ranking.id)
        then:
        thrown(RankingNotFoundException.class)
    }

    void "user should successfully modify existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> ranking
        Ranking modified = Ranking.builder()
                .name("new name")
                .categories(ranking.categories)
                .build()
        modified.setId(ranking.id)
        rankingService.modify(ranking, "new name") >> modified

        when:
        ResponseEntity result = rankingApiDelegate.modify(ranking.id, "new name")

        then:
        result.statusCode.is2xxSuccessful()
        result.getBody() == RankingMapper.getInstance().toDto(modified)
    }

    void "user should get exception when modifying non-existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> {throw new RankingNotFoundException()}
        when:
        rankingApiDelegate.modify(ranking.id, "new name")
        then:
        thrown(RankingNotFoundException.class)
    }

    void "user should successfully add category existing ranking"() {
        given:
        categoryDto =  new CategoryDto()
                .name("category 2")
                .scores(Collections.singletonList(scoreDto))
        rankingService.getRanking(ranking.id) >> ranking
        Ranking modified = new Ranking()
        modified.id = ranking.id
        modified.name = ranking.name
        modified.authorized = ranking.authorized
        modified.categories = modified.categories
        modified.categories.add(RankingMapper.getInstance().fromDto(categoryDto))
        rankingService.addCategory(ranking, RankingMapper.getInstance().fromDto(categoryDto)) >> modified
        when:
        ResponseEntity result = rankingApiDelegate.addCategory(ranking.id, categoryDto)
        then:
        result.statusCode.is2xxSuccessful()
    }

    void "user should get exception when adding category non-existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> {throw new RankingNotFoundException()}
        when:
        rankingApiDelegate.addCategory(ranking.id, categoryDto)
        then:
        thrown(RankingNotFoundException.class)
    }

    void "user should successfully delete category existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> ranking
        when:
        ResponseEntity result = rankingApiDelegate.deleteCategory(categoryDto.id, ranking.id)
        then:
        result.statusCode.is2xxSuccessful()
    }

    void "user should get exception when deleting category from non-existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> {throw new RankingNotFoundException()}
        when:
        rankingApiDelegate.deleteCategory(categoryDto.id, ranking.id)
        then:
        thrown(RankingNotFoundException.class)
    }

    void "user should get exception when deleting non-existing category from ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> ranking
        rankingService.deleteCategory(ranking, categoryDto.id) >> {throw new CategoryNotFoundException()}
        when:
        rankingApiDelegate.deleteCategory(categoryDto.id, ranking.id)
        then:
        thrown(CategoryNotFoundException.class)
    }

    void "user should successfully add points to category"() {
        given:
        rankingService.getRanking(ranking.id) >> ranking
        when:
        ResponseEntity result = rankingApiDelegate.addPoints(categoryDto.id, 1, ranking.id)
        then:
        result.statusCode.is2xxSuccessful()
    }

    void "user should get exception when adding points to category from non-existing ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> {throw new RankingNotFoundException()}
        when:
        rankingApiDelegate.addPoints(categoryDto.id, 1, ranking.id)
        then:
        thrown(RankingNotFoundException.class)
    }

    void "user should get exception when adding points to non-existing category from ranking"() {
        given:
        rankingService.getRanking(ranking.id) >> ranking
        rankingService.addPoints(ranking, categoryDto.id, 1) >> {throw new CategoryNotFoundException()}
        when:
        rankingApiDelegate.addPoints(categoryDto.id, 1, ranking.id)
        then:
        thrown(CategoryNotFoundException.class)
    }
}
