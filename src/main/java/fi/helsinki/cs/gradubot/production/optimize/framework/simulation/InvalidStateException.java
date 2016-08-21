package fi.helsinki.cs.gradubot.production.optimize.framework.simulation;

/**
 * Created by joza on 12.10.2014.
 */
public class InvalidStateException extends RuntimeException {
    public InvalidStateException() {
    }

    public InvalidStateException(String message) {
        super(message);
    }
}
