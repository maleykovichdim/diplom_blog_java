package blog.api.response.post;

import blog.api.response.user.UserForPostIdAndNameAndPhoto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CommentInPostResponse {
    private int id;
    private long timestamp;
    private String text;
    private UserForPostIdAndNameAndPhoto user;
}

