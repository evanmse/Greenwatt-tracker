package fr.greenwatt.exception;

public class StockageException extends GreenWattException {
    public StockageException(String message, Throwable cause) { super(message, cause); }
    public StockageException(String message) { super(message); }
}
