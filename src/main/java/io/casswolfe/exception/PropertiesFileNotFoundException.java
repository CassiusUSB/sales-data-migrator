package io.casswolfe.exception;

public class PropertiesFileNotFoundException extends RuntimeException{
    public PropertiesFileNotFoundException(Throwable cause) {
        super(cause);
    }

    public PropertiesFileNotFoundException() {
        super();
    }

    public PropertiesFileNotFoundException(String message) {
        super(message);
    }

    public PropertiesFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
