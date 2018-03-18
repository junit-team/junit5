package org.junit.jupiter.theories.exceptions;

/**
 * Exception that is thrown if there are any errors while retrieving a data point.
 */
public class DataPointRetrievalException extends RuntimeException {
    public DataPointRetrievalException(String message) {
        super(message);
    }

    public DataPointRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
