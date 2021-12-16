package blog.api.response.post;

import blog.api.response.user.UserForPostIdAndName;
import lombok.Data;


@Data
public class PostResponseBody {
    private int id;
    private long timestamp;
    private UserForPostIdAndName user;
    private String title;
    private String announce;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;
}
