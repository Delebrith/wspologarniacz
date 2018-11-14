package com.purplepanda.wspologarniacz.ranking

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

    void setup() {
        authenticated = User.builder()
            .id(1L)
            .name("user")
            .email("user@email.com")
            .build()

        ranking = Ranking.builder()
            .name("ranking")
            .categories(Collections.singletonList(
                Category.builder()
                        .id(1L)
                        .name("category")
                        .build())
                .toSet())
            .build()
        ranking.id = 1L

        userService = Mock(UserService.class)
        rankingRepository = Mock(RankingRepository.class)

        rankingService = new RankingServiceImpl(rankingRepository, userService)
    }

    void "user should find information on existing ranking"() {

    }
}
