package blog.api.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProfileData {
    private String email;
    private String name;
    private String password;
    private Boolean removePhoto;
    private String photo;
}