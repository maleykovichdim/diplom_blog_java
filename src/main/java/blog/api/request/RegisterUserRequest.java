package blog.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
public class RegisterUserRequest {
    @JsonProperty(value = "e_mail")
    private String email;

    private String password;
    private String name;
    private String captcha;

    @JsonProperty(value = "captcha_secret")
    private String captchaSecret;
}