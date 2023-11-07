package com.github.StasMalykhin.aviabot.exceptions;

/**
 * @author Stanislav Malykhin
 */
public class FoundWrongSearchResultException extends RuntimeException {
    public FoundWrongSearchResultException(String message) {
        super(message);
    }
}
