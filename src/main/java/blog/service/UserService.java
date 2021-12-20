package blog.service;

import blog.api.request.AuthPasswordRequest;
import blog.api.request.RegisterUserRequest;
import blog.api.response.AuthRegisterResponse;
import blog.api.response.Errors;
import blog.api.response.ResultResponse;
import blog.controller.exceptions.BadRequestException;
import blog.model.CaptchaCode;
import blog.model.User;
import blog.model.repository.CaptchaRepository;
import blog.model.repository.UserRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.io.File;
import java.util.UUID;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaRepository captchaRepository;
    private final CloudinaryService cloudinaryService;
    private final CaptchaService captchaService;



    private static final String FILE_PATH = "photos\\";
    private static final int PHOTO_SIZE_PX = 36;
    static final int PASSWORD_LENGTH_THRESHOLD = 6;

    private static final String rootFolder = "/upload";
    @Value("${blog_engine.additional.uploadedMaxFileWeight}")
    private int FILE_MAX_WEIGHT;
    @Value("${blog_engine.additional.passwordMinLength}")
    private int PASSWORD_MIN_LENGTH;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CaptchaRepository captchaRepository, CloudinaryService cloudinaryService, CaptchaService captchaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.captchaRepository = captchaRepository;
        this.cloudinaryService = cloudinaryService;
        this.captchaService = captchaService;
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

//    private String saveImage(ImageData image) {
//        return saveImage(image.getImage(), false);
//    }

    private Path generateFilePath() {
        String[] pathParts = UUID.randomUUID().toString().split("-");
        Path path = Paths.get(rootFolder);
        for (int i = 0; i < 3; i++) {
            path = path.resolve(pathParts[i]);
        }
        return path;
    }

    private String saveImage(MultipartFile image, boolean cut) {
        String savedImageUrl;
        String originalFileName = image.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        if (!extension.equals("jpg") && !extension.equals("png")) {
            System.out.println("Got error with image {}. Image extension should be jpg or png"+ originalFileName);
            BadRequestException exception = new BadRequestException("Wrong image extension");
            exception.addErrorDescription("photo", "Неправильное разрешение файла. Ожидается jpg или png.");
            throw exception;

        }
        if (image.getSize() > FILE_MAX_WEIGHT) {
            System.out.println("Got error with image {}. Weight is more than expected."+originalFileName);
            BadRequestException exception = new BadRequestException("Wrong image weight");
            exception.addErrorDescription("photo", "Размер файла превышает допустимый размер 5Мб");
            throw exception;
        }

        Path uniquePath = generateFilePath();

        Path currentPath = FileSystems.getDefault().getPath("").toAbsolutePath();
        Path fullPath = Path.of(currentPath.toString(), uniquePath.toString(), originalFileName);
        savedImageUrl = fullPath.toString();
        System.out.println("Save image to path: {}"+ fullPath);
        fullPath.getParent().toFile().mkdirs();
        try {
            image.transferTo(new File(fullPath.toString()));
            savedImageUrl = cloudinaryService.uploadImage(fullPath.toString(), uniquePath.toString().replace("\\","/"), cut);
            Files.delete(fullPath); //после загрузки файла в облако, освобождаем место на диске
        } catch (IOException e) {
            System.out.println("Got error while saving image {} to path {}"+ originalFileName+" "+ fullPath.toString()+" "+ e);
        }
        System.out.println("Image has been saved into cloudinary, url:\n" + savedImageUrl);

        return savedImageUrl;
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
            String imagePath = saveImage(photo, true);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            String newName = String.format("photo_profile_%s.%s.%s.png", sdf.format(new Date()), RandomStringUtils.randomAlphanumeric(5), photo.getOriginalFilename());
//            String filePath = System.getProperty("user.dir")+"\\"+FILE_PATH + newName;
//            BufferedImage inputImage = ImageIO.read(photo.getInputStream());
//            BufferedImage newImage = photoResizeAndCrop(inputImage);
//            ImageIO.write(newImage, "png", new File(filePath));
            user.setPhoto(imagePath);
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

    public AuthRegisterResponse getAuthRegisterResponse(final RegisterUserRequest registerUserRequest) {
        String name = registerUserRequest.getName().trim().replaceAll("\\s+", " ");
        HashMap<String, String> errors = new HashMap<>();
        boolean result = true;
        if (userRepository.findByEmail(registerUserRequest.getEmail()).isPresent()) {
            errors.put("email", "Этот e-mail уже зарегистрирован");
            result = false;
        }

        if (!checkName(name)) {
            errors.put("name", "Имя указано не верно");
            result = false;
        }

        if (registerUserRequest.getPassword().length() < PASSWORD_LENGTH_THRESHOLD) {
            errors.put("password", "Пароль короче 6-ти символов");
            result = false;
        }


        if (captchaRepository.getCaptchaCode(registerUserRequest.getCaptcha(), registerUserRequest.getCaptchaSecret()) == null) {
            errors.put("captcha", "Код с картинки введён неверно");
            result = false;
        }

        if (result) {
            addNewUser(registerUserRequest);
            return new AuthRegisterResponse(result);
        } else {
            return new AuthRegisterResponse(result, errors);
        }
    }

    boolean checkName(final String name) {
        return name.replaceAll("[a-zа-яёA-ZА-ЯЁ\\s]+", "").equals("");
    }

    private void addNewUser(final RegisterUserRequest registerUserRequest) {
        User user = new User();
        user.setEmail(registerUserRequest.getEmail());
        user.setName(registerUserRequest.getName());
        user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));
        user.setRegTime(LocalDateTime.now());
        userRepository.save(user);
    }

}
