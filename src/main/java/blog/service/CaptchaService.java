package blog.service;

import blog.api.response.auth.CaptchaResponse;
import blog.model.CaptchaCode;
import blog.model.repository.CaptchaRepository;
import com.github.cage.Cage;
import com.github.cage.GCage;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class CaptchaService {
    private final CaptchaRepository captchaRepository;
    private final static Logger LOGGER = LogManager.getLogger(CaptchaService.class);

    public CaptchaService(CaptchaRepository captchaRepository) {
        this.captchaRepository = captchaRepository;
    }

    public CaptchaResponse createAndSaveCaptcha() {
        LOGGER.info("Start creating captcha");
        String secretCode = UUID.randomUUID().toString();
        //System.out.println(secretCode);
        Pair<String, byte[]> captchaToImage = generateCaptcha();
        String captchaText = captchaToImage.getFirst();
        byte[] imageInBytes = captchaToImage.getSecond();
        String encodedImage = encodeStringIntoBase64(imageInBytes);
        //System.out.println("encodedImage: " + encodedImage);
        //System.out.println("secretCode: " + secretCode);

        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode(captchaText);
        captchaCode.setSecretCode(secretCode);
        captchaCode.setTime(LocalDateTime.now());
        captchaRepository.save(captchaCode);

        CaptchaResponse captchaResponse = new CaptchaResponse();
        captchaResponse.setSecret(secretCode);
        captchaResponse.setImage("data:image/png;base64, " + encodedImage);

        //System.out.println("captha generated: "+captchaResponse.toString());
        return captchaResponse;
    }

    private Pair<String, byte[]> generateCaptcha() {
        Cage cage = new GCage();
        String captchaText = cage.getTokenGenerator().next();
        //System.out.println("next: " + captchaText);
        byte[] imageInBytes = new byte[0];
        try {
            imageInBytes = cage.draw(captchaText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Pair<String, byte[]> textToImage = Pair.of(captchaText, imageInBytes);
        return textToImage;
    }

    private String encodeStringIntoBase64(byte[] imageInBytes) {
        return Base64.getEncoder().encodeToString(imageInBytes);
    }

    private byte[] decodeStringFromBase64(String encodedString) {
        return Base64.getDecoder().decode(encodedString);
    }

    @Transactional
    public void deleteExpiredCaptcha() {
        captchaRepository.deleteByTimeBefore(LocalDateTime.now().minusHours(1));
    }
}
