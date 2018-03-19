package org.junit.jupiter.theories.exceptions;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Exception that is thrown if there are any errors while retrieving a data point.
 */
@API(status = INTERNAL, since = "5.2")
public class DataPointRetrievalException extends RuntimeException {

    private static final long serialVersionUID = -2002844107705394342L;

    public DataPointRetrievalException(String message) {
        super(message);
    }

    public DataPointRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
