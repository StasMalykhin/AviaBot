package com.github.StasMalykhin.aviabot.exceptions;

/**
 * @author Stanislav Malykhin
 */
public class NoSuchCountryException extends RuntimeException {
    public NoSuchCountryException(String message) {
        super(message);
    }
}
