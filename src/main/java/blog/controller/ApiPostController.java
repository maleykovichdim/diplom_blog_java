package blog.controller;

import blog.api.request.LikeRequest;
import blog.api.request.ModerateRequest;
import blog.api.request.PostRequest;
import blog.api.response.ResultResponse;
import blog.api.response.StatisticResponse;
import blog.api.response.post.PostResponse;
import blog.model.User;
import blog.model.repository.UserRepository;
import blog.service.CommonService;
import blog.service.PostService;
import blog.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class ApiPostController {

    private final PostService postService;
    private final SettingsService settingsService;
    private final CommonService commonService;

    @Autowired
    public ApiPostController(PostService postService, SettingsService settingsService, UserRepository userRepository, CommonService commonService) {
        this.postService = postService;
        this.settingsService = settingsService;
        this.commonService = commonService;
    }

    @GetMapping("/post")
    //@PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostResponse> getPosts(@RequestParam(defaultValue = "0", required = false) int offset,
                                                 @RequestParam(defaultValue = "20", required = false) int limit,
                                                 @RequestParam(defaultValue = "recent", required = false) String mode )  {
        return postService.getPosts(offset, limit, mode);
    }

    @GetMapping("/post/search")
    public ResponseEntity<PostResponse> getPostsSearch(@RequestParam(defaultValue = "0", required = false) int offset,
                                                 @RequestParam(defaultValue = "20", required = false) int limit,
                                                 @RequestParam(required = false) String query )  {
        if (query == null){
            return postService.getPosts(offset, limit, "recent");
        }
        return postService.getPostsSearch(offset, limit, query);
    }

    @GetMapping("/post/byDate")
    public ResponseEntity<PostResponse> getPostsByDate(@RequestParam(defaultValue = "0", required = false) int offset,
                                                       @RequestParam(defaultValue = "20", required = false) int limit,
                                                       @RequestParam(required = true) String date )  {
        return postService.getPostsByDate(offset, limit, LocalDate.parse(date));
    }

    @GetMapping("/post/byTag")
    public ResponseEntity<PostResponse> getPostsByTag(@RequestParam(defaultValue = "0", required = false) int offset,
                                                      @RequestParam(defaultValue = "20", required = false) int limit,
                                                      @RequestParam(required = true) String tag )  {
        return postService.getPostsByTag(offset, limit, tag);
    }

    @GetMapping("/post/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostResponse> getPostsOfMine(@RequestParam(defaultValue = "0", required = false) int offset,
                                                       @RequestParam(defaultValue = "20", required = false) int limit,
                                                       @RequestParam(required = true) String status,
                                                       Principal principal)  {

        User currentUser = commonService.getCurrentUser(principal);
        return postService.getPostsOfMine(offset, limit, status, currentUser.getId());
    }

    @GetMapping("/post/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<PostResponse> getPostsModeration(@RequestParam(defaultValue = "0", required = false) int offset,
                                                       @RequestParam(defaultValue = "20", required = false) int limit,
                                                       @RequestParam(required = true) String status,
                                                       Principal principal)  {

        User currentUser = commonService.getCurrentUser(principal);
        return postService.getPostsModeration(offset, limit, status, currentUser.getId());
    }


    @PostMapping("/post")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> putPost(@RequestBody PostRequest inputPost, Principal principal )  {

        int currentUserId = commonService.getCurrentUser(principal).getId();
        ResultResponse  resultResponse = postService.validatePostData(inputPost);
        if (!resultResponse.isResult()){
            return ResponseEntity.ok(resultResponse);
        }
        return postService.putPost(inputPost, currentUserId);
    }

    @PutMapping("/post/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> updatePost(@PathVariable(name = "id") int id, @RequestBody PostRequest inputPost, Principal principal)  {

        int currentUserId = commonService.getCurrentUser(principal).getId();
        ResultResponse  resultResponse = postService.validatePostData(inputPost);
        if (!resultResponse.isResult()){
            return ResponseEntity.ok(resultResponse);
        }
        return postService.updatePost(inputPost, id, currentUserId);
    }


    @PostMapping("/post/like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> likePost(@RequestBody LikeRequest like, Principal principal)  {
        int currentUserId = commonService.getCurrentUser(principal).getId();
        return postService.likeOrDislikePost(like, currentUserId, true);
    }

    @PostMapping("/post/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> dislikePost(@RequestBody LikeRequest like, Principal principal)  {
        int currentUserId = commonService.getCurrentUser(principal).getId();
        return postService.likeOrDislikePost(like, currentUserId, false);
    }

    @GetMapping("/statistics/all")
    public ResponseEntity<StatisticResponse> getAllStatistic(Principal principal)  {
        User user = commonService.getCurrentUser(principal);
        if ( (!settingsService.getGlobalSettings().isStatisticsIsPublic())
                && (user == null || user.getIsModerator() != 1 )){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return postService.getAllStatistic();
    }

    @GetMapping("/statistics/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<StatisticResponse> getAllStatisticOfMine(Principal principal)  {
        User user = commonService.getCurrentUser(principal);
        return postService.getAllStatisticOfMine(user);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> moderatePost(@RequestBody ModerateRequest inputPost, Principal principal )  {
        try {
            User currentUser = commonService.getCurrentUser(principal);
            ResultResponse  resultResponse = postService.moderatePost(inputPost, currentUser);
            return ResponseEntity.ok(resultResponse);
        }
        catch(Exception exception){
            return ResponseEntity.ok(new ResultResponse(false));
        }
    }
}
