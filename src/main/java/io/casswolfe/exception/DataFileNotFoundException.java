package io.casswolfe.exception;

public class DataFileNotFoundException extends RuntimeException{
    public DataFileNotFoundException(Throwable cause) {
        super(cause);
    }

    public DataFileNotFoundException() {
        super();
    }

    public DataFileNotFoundException(String message) {
        super(message);
    }

    public DataFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
