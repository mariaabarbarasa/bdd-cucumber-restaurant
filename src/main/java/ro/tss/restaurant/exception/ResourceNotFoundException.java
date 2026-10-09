package ro.tss.restaurant.exception;

/** The requested resource (menu item, table, reservation, order) does not exist -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " with id " + id + " was not found");
    }
}
