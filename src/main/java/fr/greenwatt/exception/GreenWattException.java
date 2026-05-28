package fr.greenwatt.exception;

/** Exception racine de l'application GreenWatt. */
public class GreenWattException extends RuntimeException {
    public GreenWattException(String message) { super(message); }
    public GreenWattException(String message, Throwable cause) { super(message, cause); }
}
