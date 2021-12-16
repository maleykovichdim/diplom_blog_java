package blog.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCommentRequest {
    @JsonProperty("parent_id")
    private Optional<Integer> parentId;
    @JsonProperty("post_id")
    private int postId;
    private String text;
}
