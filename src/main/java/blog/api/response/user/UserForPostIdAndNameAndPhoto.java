package blog.api.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserForPostIdAndNameAndPhoto {
    private int id;
    private String name;
    private String photo;
}
