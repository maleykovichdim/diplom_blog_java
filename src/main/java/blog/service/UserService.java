package blog.service;

import blog.api.request.AuthPasswordRequest;
import blog.api.response.Errors;
import blog.api.response.ResultResponse;
import blog.model.CaptchaCode;
import blog.model.User;
import blog.model.repository.CaptchaRepository;
import blog.model.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.io.File;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaRepository captchaRepository;

    private static final String FILE_PATH = "photos\\";
    private static final int PHOTO_SIZE_PX = 36;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CaptchaRepository captchaRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.captchaRepository = captchaRepository;
        File dir = new File(System.getProperty("user.dir")+"\\"+FILE_PATH);
        if (!dir.exists()){
            dir.mkdir();
        }
    }


    public ResultResponse validateChangeProfile(
            MultipartFile photo,
            String name,
            String email,
            String password,
            Byte removePhoto,
            User user){

        ResultResponse resultResponse = new ResultResponse(true);
        Errors errors = new Errors();

        Optional<User> tempUser = Optional.empty();
        if (name.length() > 2)
            tempUser = userRepository.findByName(name);
        if (name.length() < 3 || (tempUser.isPresent() && tempUser.get().getId() != user.getId())){
            resultResponse.setResult(false);
            errors.setName("Имя указано неверно");
            resultResponse.setErrors(errors);
            return resultResponse;
        }

        tempUser = userRepository.findByEmail(email);
        if (tempUser.isPresent() && tempUser.get().getId() != user.getId()){
            resultResponse.setResult(false);
            errors.setEmail("Этот e-mail уже зарегистрирован");
            resultResponse.setErrors(errors);
            return resultResponse;
        }

        if (password != null && password.length() < 6 ){
                resultResponse.setResult(false);
                errors.setPassword("Пароль короче 6-ти символов");
                resultResponse.setErrors(errors);
                return resultResponse;
        }

        if (photo != null && photo.getSize() > 5242880){//5 mb
            resultResponse.setResult(false);
            errors.setPhoto("Фото слишком большое, нужно не более 5 Мб");
            resultResponse.setErrors(errors);
            return resultResponse;
        }
        return  resultResponse;
    }

    public ResponseEntity<ResultResponse> changeProfile(
            MultipartFile photo,
            String name,
            String email,
            String password,
            Byte removePhoto,
            User user) throws IOException {

        user.setName(name);
        user.setEmail(email);

        ResultResponse resultResponse = new ResultResponse(true);
        if (removePhoto != null && removePhoto == 1){
            user.setPhoto(null);
            photo = null;// check correct ??
        }
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }

        if (photo != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String newName = String.format("photo_profile_%s.%s.%s.png", sdf.format(new Date()), RandomStringUtils.randomAlphanumeric(5), photo.getOriginalFilename());
            String filePath = System.getProperty("user.dir")+"\\"+FILE_PATH + newName;
            BufferedImage inputImage = ImageIO.read(photo.getInputStream());
            BufferedImage newImage = photoResizeAndCrop(inputImage);
            ImageIO.write(newImage, "png", new File(filePath));
            user.setPhoto(FILE_PATH + newName);
        }

        userRepository.save(user);
        resultResponse.setResult(true);
        return ResponseEntity.ok(resultResponse);
    }

    public BufferedImage photoResizeAndCrop(BufferedImage inputImage){
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int newWidth = 0;
        int newHeight = 0;
        double ratio = ((double)height)/width;
        if (width > height){
            newHeight = PHOTO_SIZE_PX;
            newWidth = (int)(((double)PHOTO_SIZE_PX) / ratio + 1);
        }else{
            newWidth = PHOTO_SIZE_PX;
            newHeight = (int)(((double)PHOTO_SIZE_PX) * ratio + 1);
        }
        BufferedImage newImage = Scalr.crop(
                Scalr.resize(inputImage,  Scalr.Method.QUALITY, newWidth, newHeight),
                PHOTO_SIZE_PX, PHOTO_SIZE_PX
        );
        return newImage;
    }

    public ResultResponse passwordChange(AuthPasswordRequest authPasswordRequest){
        ResultResponse resultResponse = new ResultResponse(false);
        Errors errors = new Errors();
        resultResponse.setErrors(errors);

        if (authPasswordRequest.getPassword().length() < 6){
            errors.setPassword("Пароль короче 6-ти символов");
            return resultResponse;
        }

        Optional<blog.model.User> user = userRepository.findByCode(authPasswordRequest.getCode());
        if (user.isEmpty()){
            errors.setCode("Ссылка для восстановления пароля устарела."+
                    "<a href="+
                    " \"/auth/restore\">Запросить ссылку снова</a>");
            return resultResponse;
        }

        Optional<CaptchaCode> captchaCode = captchaRepository.findCaptchaCodeBySecretCode(authPasswordRequest.getCaptchaSecret());
       if (captchaCode.isEmpty() ||
                (!captchaCode.get().getCode().equals(
                        authPasswordRequest.getCaptcha())) ){
            errors.setCode("Код с картинки введён неверно");
            return resultResponse;
        }


        blog.model.User currentUser = user.get();

        if (authPasswordRequest.getPassword() != null) {
            currentUser.setPassword(passwordEncoder.encode(authPasswordRequest.getPassword()));
        }
        userRepository.save(currentUser);
        return new ResultResponse(true);
    }
}
