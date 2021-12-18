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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
        Pair<String, byte[]> captchaToImage = generateCaptcha();
        String captchaText = captchaToImage.getFirst();
        byte[] imageInBytes = captchaToImage.getSecond();
        String encodedImage = encodeStringIntoBase64(imageInBytes);
        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode(captchaText);
        captchaCode.setSecretCode(secretCode);
        captchaCode.setTime(LocalDateTime.now());
        captchaRepository.save(captchaCode);

        CaptchaResponse captchaResponse = new CaptchaResponse();
        captchaResponse.setSecret(secretCode);
        captchaResponse.setImage("data:image/png;base64, " + encodedImage);

        return captchaResponse;
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }

    private Pair<String, byte[]> generateCaptcha() {
        Cage cage = new GCage();
        String captchaText = cage.getTokenGenerator().next();
        byte[] imageInBytes = new byte[0];
        try {
            BufferedImage image = cage.drawImage(captchaText);
            BufferedImage newImage = resize(image, 100, 35);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(newImage, "png", baos);
            imageInBytes = baos.toByteArray();
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
