package com.purplepanda.wspologarniacz.ranking.exception;

public class RankingNotFoundException extends RuntimeException {
    public RankingNotFoundException() {
        super("Ranking with given ID does not exist");
    }
}
