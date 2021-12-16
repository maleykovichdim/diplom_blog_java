package blog.api.response.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TagResponse {
    @JsonProperty("tags")
    List<TagResponseBody> tagResponseBody;
}
