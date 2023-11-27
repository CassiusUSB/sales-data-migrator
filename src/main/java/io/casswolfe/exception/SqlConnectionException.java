package io.casswolfe.exception;

public class SqlConnectionException extends RuntimeException{
    public SqlConnectionException(Throwable cause) {
        super(cause);
    }

    public SqlConnectionException() {
        super();
    }

    public SqlConnectionException(String message) {
        super(message);
    }

    public SqlConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
