package blog.api.response.post;

import blog.api.response.user.UserForPostIdAndName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class OnePostResponse {
    private int id;
    private long timestamp;
    private boolean active;
    private UserForPostIdAndName user;
    private String title;
    private String text;
    private int likeCount;
    private int dislikeCount;
    private int viewCount;
    private List<CommentInPostResponse> comments;
    private List<String> tags;
}
