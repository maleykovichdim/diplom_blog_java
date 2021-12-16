package blog.api.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserAuthCheckResponse {
         private int id;
         private String name;
         private String photo;
         private String email;
         private Boolean moderation;
         private int moderationCount;
         private Boolean settings;
}

