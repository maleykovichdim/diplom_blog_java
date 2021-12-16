package blog.api.response.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PostResponse {
    int count;
    @JsonProperty("posts")
    List<PostResponseBody> postResponseBody;
}

