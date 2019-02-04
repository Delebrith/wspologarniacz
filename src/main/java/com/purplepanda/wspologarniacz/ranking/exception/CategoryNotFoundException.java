package com.purplepanda.wspologarniacz.ranking.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
        super("Category with given ID does not exist");
    }
}
