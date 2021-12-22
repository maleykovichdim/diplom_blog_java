package blog.controller;

import blog.api.request.PostCommentRequest;
import blog.api.request.ProfileData;
import blog.api.response.CalendarResponse;
import blog.api.response.InitResponse;
import blog.api.response.ResultResponse;
import blog.api.response.SettingResponseRequest;
import blog.api.response.tag.TagResponse;
import blog.model.User;
import blog.model.repository.UserRepository;
import blog.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;


@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final SettingsService settingsService;
    private final TagService tagService;
    private final CommentService commentService;
    private final InitResponse initResponse;
    private final UserService userService;
    private final CommonService commonService;
    private final PostService postService;

    @Autowired
    public ApiGeneralController(SettingsService settingsService, TagService tagService, CommentService commentService, InitResponse initResponse, UserRepository userRepository, UserService userService, CommonService commonService, PostService postService) {
        this.settingsService = settingsService;
        this.tagService = tagService;
        this.commentService = commentService;
        this.initResponse = initResponse;
        this.userService = userService;
        this.commonService = commonService;
        this.postService = postService;
    }

    @GetMapping("/settings")
    public SettingResponseRequest settings() {
        return settingsService.getGlobalSettings();
    }

    @GetMapping("/init")
    public InitResponse init(){
        return  initResponse;
    }

    @GetMapping("/tag")
    public ResponseEntity<TagResponse> getTags(@RequestBody (required=false) String query)  {
        return tagService.getTags(query);
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> putComment(@RequestBody PostCommentRequest inputPostComment, Principal principal )  {

        User currentUser = commonService.getCurrentUser(principal);
        int currentUserId = currentUser.getId();

        ResultResponse resultResponse = commentService.validatePutComment(inputPostComment);
        if (!resultResponse.isResult()){
            return ResponseEntity.ok(resultResponse);
        }
        return commentService.putComment(inputPostComment, currentUserId);
    }

    @PostMapping(value="/profile/my", produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> changeProfile(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("removePhoto") boolean removePhoto,
            @RequestParam(value = "password", defaultValue = "") String password,
            @RequestParam("photo") MultipartFile photo,
            Principal principal) throws IOException {

        User user = commonService.getCurrentUser(principal);
        ResultResponse resultResponse = userService.validateChangeProfile(
                photo, name, email, password,
                removePhoto?(byte)1:(byte)0, user);
        if (!resultResponse.isResult()){
            return  ResponseEntity.ok(resultResponse);
        }
        return userService.changeProfile(photo, name, email, password,
                removePhoto?(byte)1:(byte)0, user);
    }

    @PostMapping("/profile/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> changeProfile(
            @RequestBody ProfileData profileData, Principal principal) throws IOException {

        User user = commonService.getCurrentUser(principal);
        Boolean rp = profileData.getRemovePhoto();
        byte removePhoto = 0;
        if (rp != null && rp){
            removePhoto = (byte) 1;
        }

        ResultResponse resultResponse = userService.validateChangeProfile(
                null,//profileData.getPhoto(),
                profileData.getName(),
                profileData.getEmail(), profileData.getPassword(),
                removePhoto, user);
        if (!resultResponse.isResult()){
            return  ResponseEntity.ok(resultResponse);
        }
        return userService.changeProfile(null, //profileData.getPhoto(),
                profileData.getName(),
                profileData.getEmail(), profileData.getPassword(),
                removePhoto, user);
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> putSettings(
            @RequestBody SettingResponseRequest requestBody,
            Principal principal
    ){
        ResultResponse resultResponse = new ResultResponse(false);
        User user = commonService.getCurrentUser(principal);
        if (user.getIsModerator()!=1){
            return ResponseEntity.ok(resultResponse);
        }
        settingsService.setGlobalSettings(requestBody);
        resultResponse.setResult(true);
        return ResponseEntity.ok(resultResponse);
    }

    @GetMapping("/calendar")
    public CalendarResponse getCalendar(@RequestParam(defaultValue = "-1") int year) {
        if (year < 0) {
            year = LocalDate.now().getYear();
        }
        return postService.selectPostsCountsByYear(year);
    }

}
