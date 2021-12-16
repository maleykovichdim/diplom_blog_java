package blog.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultResponse {

    public ResultResponse(boolean result){
        this.result = result;
        this.errors = null;
    }

    private boolean result;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Errors errors;
}
