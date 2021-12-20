package blog.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterResponse {
    private boolean result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HashMap<String, String> errors;

    public AuthRegisterResponse(boolean result) {
        this.result = result;
    }
}
