package blog.controller.exceptions;


import java.util.HashMap;
import java.util.Map;

public class BadRequestException extends RuntimeException {
    private final Map<String, String> errorsDescription;

    public BadRequestException(String message) {
        super(message);
        this.errorsDescription = new HashMap<>();
    }

    public Map<String, String> getErrorsDescription() {
        return errorsDescription;
    }

    public void addErrorDescription(String errorType, String description) {
        this.errorsDescription.put(errorType, description);

    }
}
