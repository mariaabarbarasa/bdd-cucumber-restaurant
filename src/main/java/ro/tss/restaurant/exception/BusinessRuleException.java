package ro.tss.restaurant.exception;

/** The operation breaks a business rule (duplicate, table already booked, etc.) -> HTTP 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
