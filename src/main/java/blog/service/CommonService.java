package blog.service;

import blog.model.User;
import blog.model.repository.CaptchaRepository;
import blog.model.repository.UserRepository;
import com.github.cage.Cage;
import com.github.cage.YCage;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Principal;
import java.util.Properties;



@Service
@Configurable
public class CommonService {

    private static final String URL_CHANGE_PASSWORD = "http://localhost:8080/login/change-password/";
    private static final String AUTH_EMAIL = "dmaleyk@gmail.com";
    private static final int CAPTCHA_NUM_SYMBOLS = 5;
    private static final int CAPTCHA_SECRET_CODE_NUM_SYMBOLS = 20;

    private static final Cage cage = new YCage();
    private final UserRepository userRepository;
    private final CaptchaRepository captchaRepository;

    public CommonService(UserRepository userRepository, CaptchaRepository captchaRepository) {
        this.userRepository = userRepository;
        this.captchaRepository = captchaRepository;
    }

    public User getCurrentUser(Principal principal){
        if (principal == null)
            return null;
        String email = principal.getName();
        return userRepository.findByEmail(email).
                orElseThrow(() -> new UsernameNotFoundException(email));
    }

    public boolean sendChangePasswordEmail(String email, String hash){

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        String authPassword = System.getenv("EMAIL_PASSWORD");//need set system variable

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(AUTH_EMAIL, "nuauocwnfrsrcwto");
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AUTH_EMAIL));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(email)
            );
            message.setSubject("Password changing");
            message.setText("Hello. To change you password follow this link: "
                    + "\n\n " + URL_CHANGE_PASSWORD + hash);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
