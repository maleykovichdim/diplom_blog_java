package blog.controller;

import blog.api.request.AuthPasswordRequest;
import blog.api.request.EmailRequest;
import blog.api.request.LoginRequest;

import blog.api.response.auth.CaptchaResponse;
import blog.api.response.auth.LoginResponse;
import blog.api.response.ResultResponse;
import blog.api.response.auth.UserLoginResponse;

import blog.model.repository.CaptchaRepository;
import blog.model.repository.UserRepository;
import blog.service.CaptchaService;
import blog.service.CommonService;
import blog.service.PostService;
import blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PostService postService;
    private final PasswordEncoder passwordEncoder;
    private final CommonService commonService;
    private final UserService userService;
    private final CaptchaService captchaService;


    @Autowired
    public ApiAuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PostService postService, PasswordEncoder passwordEncoder, CommonService commonService, CommonService commonService1, CaptchaRepository captchaRepository, UserService userService, CaptchaService captchaService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.postService = postService;
        this.passwordEncoder = passwordEncoder;
        this.commonService = commonService;
        this.userService = userService;
        this.captchaService = captchaService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody LoginRequest loginRequest){

        Optional<blog.model.User> userCheck = userRepository.findByEmail(loginRequest.getEmail());
        if (userCheck.isEmpty()){
            return ResponseEntity.ok(new ResultResponse(false));
        }
        String password = userCheck.get().getPassword();
        if (!passwordEncoder.matches(loginRequest.getPassword(), password)){
            return ResponseEntity.ok(new ResultResponse(false));
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            User user = (User) auth.getPrincipal();
            return ResponseEntity.ok(getLoginResponse(user.getUsername()));
        }catch(Exception e){
            return ResponseEntity.ok(new ResultResponse(false));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<ResultResponse> logout (HttpServletRequest request, HttpServletResponse response){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
            securityContextLogoutHandler.setClearAuthentication(true);
            securityContextLogoutHandler.setInvalidateHttpSession(true);
            securityContextLogoutHandler.logout(request, response, auth);
        }
        else{
            return ResponseEntity.ok(new ResultResponse(false));
        }
        return ResponseEntity.ok(new ResultResponse(true));
    }

    @GetMapping("/check")
    public ResponseEntity<LoginResponse> check(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(new LoginResponse());
        }
        return ResponseEntity.ok(getLoginResponse(principal.getName()));
    }


    private LoginResponse getLoginResponse(String email){
        blog.model.User currentUser =
                userRepository.findByEmail(email).
                        orElseThrow(()-> new UsernameNotFoundException(email));

        UserLoginResponse userLoginResponse = new UserLoginResponse();
        userLoginResponse.setEmail(currentUser.getEmail());
        userLoginResponse.setName(currentUser.getName());
        boolean isModerator = (currentUser.getIsModerator() == 1);
        userLoginResponse.setModeration(isModerator);
        userLoginResponse.setId(currentUser.getId());
        if (isModerator) {
            long newPosts = postService.getNumNewPosts();
            userLoginResponse.setModerationCount((int)newPosts);//Long to Int, small base
        }
        userLoginResponse.setSettings(isModerator);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setResult(true);
        loginResponse.setUserLoginResponse(userLoginResponse);
        return  loginResponse;
    }

    @PostMapping("/restore")
    public ResponseEntity< ResultResponse> passwordRecovery(
            @RequestBody EmailRequest emailRequest ){

        Optional<blog.model.User> user = userRepository.findByEmail(emailRequest.getEmail());
        if (user.isEmpty()){
            return ResponseEntity.ok(new ResultResponse(false));
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        user.get().setCode(uuid);
        blog.model.User currentUser = user.get();
        userRepository.save(currentUser);
        boolean isSent = commonService.sendChangePasswordEmail(currentUser.getEmail(), uuid);
        if (!isSent){
            return ResponseEntity.ok(new ResultResponse(false));
        }
        return ResponseEntity.ok(new ResultResponse(true));
    }

    @PostMapping("/password")
    public ResponseEntity< ResultResponse> passwordChange(
            @RequestBody AuthPasswordRequest authPasswordRequest ){
        return ResponseEntity.ok(userService.passwordChange(authPasswordRequest));
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha(){
        captchaService.deleteExpiredCaptcha();
        return ResponseEntity.ok(captchaService.createAndSaveCaptcha());
    }

}
