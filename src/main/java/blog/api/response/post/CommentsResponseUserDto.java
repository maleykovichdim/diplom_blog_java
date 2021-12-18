package blog.api.response.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentsResponseUserDto {

    private int id;
    private String name;
    private String photo;

}