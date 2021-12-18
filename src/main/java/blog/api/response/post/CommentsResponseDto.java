package blog.api.response.post;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentsResponseDto {
    private int id;
    private long timestamp;
    private String text;

    @JsonProperty("user")
    private CommentsResponseUserDto commentsResponseUserDto;
}
