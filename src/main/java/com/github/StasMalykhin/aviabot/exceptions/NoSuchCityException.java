package com.github.StasMalykhin.aviabot.exceptions;

/**
 * @author Stanislav Malykhin
 */
public class NoSuchCityException extends RuntimeException {
    public NoSuchCityException(String message) {
        super(message);
    }
}
