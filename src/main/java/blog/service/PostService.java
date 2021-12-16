package blog.service;


import blog.api.request.LikeRequest;
import blog.api.request.ModerateRequest;
import blog.api.request.PostRequest;
import blog.api.response.Errors;
import blog.api.response.ResultResponse;
import blog.api.response.StatisticResponse;
import blog.api.response.post.PostResponse;
import blog.api.response.post.PostResponseBody;
import blog.api.response.user.UserForPostIdAndName;
import blog.model.*;
import blog.model.enums.ModerationStatus;
import blog.model.repository.PostRepository;

import blog.model.repository.Tag2PostRepository;
import blog.model.repository.TagRepository;
import blog.model.repository.VoteRepository;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Configurable
public class PostService {

    static final String DECLINED = "declined";
    static final String NEW = "new";
    static final String ACCEPTED = "accepted";
    static final String RECENT = "recent";
    static final String EARLY = "early";
    static final String POPULAR = "popular";
    static final String INACTIVE = "inactive";
    static final String PENDING = "pending";
    static final String PUBLISHED = "published";

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final Tag2PostRepository tag2PostRepository ;
    private final VoteRepository voteRepository;

    @Autowired
    public PostService(PostRepository postRepository, TagRepository tagRepository, Tag2PostRepository tag2PostRepository, VoteRepository voteRepository, SettingsService settingsService) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.tag2PostRepository = tag2PostRepository;
        this.voteRepository = voteRepository;
    }

    private  List<PostResponseBody> convertToResponseManyPosts(List<Post> postList){
        ArrayList<PostResponseBody> postResponseBodyList = new ArrayList<>();
        for (Post post : postList) {
            PostResponseBody a = new PostResponseBody();
            a.setId(post.getId());
            a.setTimestamp((post.getTime().toEpochSecond(ZoneOffset.UTC)));
            a.setTitle(post.getTitle());
            a.setAnnounce(Jsoup.parse(post.getText()).text());
            a.setLikeCount(post.getPostVotes().stream().filter(e -> e.getValue()>0).mapToInt(PostVote::getValue).sum());
            a.setDislikeCount(post.getPostVotes().stream().filter(e -> e.getValue()<0).mapToInt(PostVote::getValue).sum());
            a.setCommentCount(post.getPostComments().size());
            a.setViewCount(post.getViewCount());
            a.setUser(new UserForPostIdAndName(post.getUser().getId(), post.getUser().getName()));
            postResponseBodyList.add(a);
        }
        return postResponseBodyList;
    }

    public ResponseEntity<PostResponse> getPosts(int offset, int limit, String mode){

        List<Post> postList = null;
        int count = (int)postRepository.findNumPostsWithCriteria((byte)1, ModerationStatus.ACCEPTED,
                LocalDateTime.now());

        if (count == 0){
            return ResponseEntity.ok(new PostResponse(0, new ArrayList<>()));
        }

        switch(mode.toLowerCase()){
            case RECENT: {
                Pageable pageable = PageRequest.of(offset, limit, Sort.by("time").descending());
                postList = postRepository.findPosts((byte) 1, ModerationStatus.ACCEPTED,
                        LocalDateTime.now(), pageable);
                }
                break;

            case  EARLY: {
                Pageable pageable = PageRequest.of(offset, limit, Sort.by("time").ascending());
                postList = postRepository.findPosts((byte) 1, ModerationStatus.ACCEPTED,
                        LocalDateTime.now(), pageable);
                }
                break;

            case  POPULAR: {
                Pageable pageable = PageRequest.of(offset, limit);
                postList = postRepository.findPostsPopular((byte) 1, ModerationStatus.ACCEPTED,
                        LocalDateTime.now(), pageable);
                }
                break;

            default: {
                Pageable pageable = PageRequest.of(offset, limit);
                postList = postRepository.findPostsBest((byte)1, ModerationStatus.ACCEPTED,
                        LocalDateTime.now(), pageable
                );
            }
        }

        List<PostResponseBody> postResponseBodyList = convertToResponseManyPosts(postList);
        return ResponseEntity.ok(new PostResponse(count, postResponseBodyList));
    }

    public ResponseEntity<PostResponse> getPostsSearch(int offset, int limit, String query){

        int count = (int)postRepository.findNumPostsWithCriteriaAndSearch((byte)1, ModerationStatus.ACCEPTED,
                LocalDateTime.now(), query);

        if (count == 0){
            return ResponseEntity.ok(new PostResponse(0, new ArrayList<>()));
        }

        Pageable pageable = PageRequest.of(offset, limit,  Sort.by("time").descending());
        List<Post> postList = postRepository.findPostsSearch((byte)1, ModerationStatus.ACCEPTED,
                LocalDateTime.now(), query, pageable
        );

        List<PostResponseBody> postResponseBodyList = convertToResponseManyPosts(postList);
        return ResponseEntity.ok(new PostResponse(count, postResponseBodyList));
    }

    public ResponseEntity<PostResponse> getPostsByDate(int offset, int limit, LocalDate date ){

        int count = (int)postRepository.findNumPostsWithCriteriaAndByDate((byte)1, ModerationStatus.ACCEPTED,
                LocalDateTime.now(), date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        if (count == 0){
            return ResponseEntity.ok(new PostResponse(0, new ArrayList<>()));
        }

        Pageable pageable = PageRequest.of(offset, limit,  Sort.by("time").descending());
        List<Post> postList = postRepository.findPostsByDate((byte)1, ModerationStatus.ACCEPTED,
                LocalDateTime.now(), date.atStartOfDay(), date.plusDays(1).atStartOfDay(), pageable
        );

        List<PostResponseBody> postResponseBodyList = convertToResponseManyPosts(postList);
        return ResponseEntity.ok(new PostResponse(count, postResponseBodyList));
    }

    public ResponseEntity<PostResponse> getPostsByTag(int offset, int limit, String tag){

        int count = (int)postRepository.findNumPostsWithCriteriaAndByTag((byte)1, ModerationStatus.ACCEPTED,
                LocalDateTime.now(),
                tag
        );

        if (count == 0){
            return ResponseEntity.ok(new PostResponse(0, new ArrayList<>()));
        }

        Pageable pageable = PageRequest.of(offset, limit,  Sort.by("time").descending());
        List<Post> postList = postRepository.findPostsByTag((byte)1, ModerationStatus.ACCEPTED,
                LocalDateTime.now(),
                tag,
                pageable
        );

        List<PostResponseBody> postResponseBodyList = convertToResponseManyPosts(postList);
        return ResponseEntity.ok(new PostResponse(count, postResponseBodyList));
    }

    public long getNumNewPosts(){
        return postRepository.countNumNewPosts();
    }

    public ResponseEntity<PostResponse> getPostsOfMine(int offset, int limit, String status, int userId){

        int count = 0;
        ModerationStatus moderationStatus;
        switch (status) {
            case PENDING:
                moderationStatus = ModerationStatus.NEW;
                break;
            case DECLINED:
                moderationStatus = ModerationStatus.DECLINED;
                break;
            case PUBLISHED:
                moderationStatus = ModerationStatus.ACCEPTED;
                break;
            default:
                moderationStatus = ModerationStatus.NEW;
        }

        switch(status.toLowerCase()){
            case INACTIVE:
                count = (int)postRepository.countNumMyPostsWithoutModerationStatus((byte)0, userId);
                break;
            case PENDING:
            case DECLINED:
            case PUBLISHED:
                count = (int)postRepository.countNumMyPostsWithModerationStatus(moderationStatus,(byte)1, userId);
                break;
            default:
        }

        if (count == 0){
            return ResponseEntity.ok(new PostResponse(0, new ArrayList<>()));
        }

        List<Post> postList = null;
        Pageable pageable = PageRequest.of(offset, limit,  Sort.by("time").descending());

        switch(status.toLowerCase()){
            case INACTIVE:
                postList = postRepository.findMyPostsWithoutModerationStatus((byte)0, userId, pageable);
                break;
            case PENDING:
            case DECLINED:
            case PUBLISHED:
                postList = postRepository.findMyPostsWithModerationStatus(moderationStatus,(byte)1, userId, pageable);
                break;
            default:
        }

        List<PostResponseBody> postResponseBodyList = convertToResponseManyPosts(postList);
        return ResponseEntity.ok(new PostResponse(count, postResponseBodyList));
    }


    public ResponseEntity<PostResponse> getPostsModeration(int offset, int limit, String status, int userId){

        ModerationStatus moderationStatus;
        switch (status) {
            case NEW:
                moderationStatus = ModerationStatus.NEW;
                break;
            case DECLINED:
                moderationStatus = ModerationStatus.DECLINED;
                break;
            default:
                moderationStatus = ModerationStatus.ACCEPTED;
                break;
        }
        int count = (int)postRepository.countNumPostsModeration(moderationStatus, (byte) 1, userId);

        if (count == 0){
            return ResponseEntity.ok(new PostResponse(0, new ArrayList<>()));
        }

        Pageable pageable = PageRequest.of(offset, limit,  Sort.by("time").descending());
        List<Post> postList = postRepository.findPostsModeration(moderationStatus,(byte)1, userId, pageable);

        List<PostResponseBody> postResponseBodyList = convertToResponseManyPosts(postList);
        return ResponseEntity.ok(new PostResponse(count, postResponseBodyList));
    }

    private Post convertInputToPost(PostRequest inputPost, int userId, Post post, boolean needToDoExtraOperation){
        LocalDateTime timeFromPost = inputPost.getTimestamp().toLocalDateTime();
        LocalDateTime lt  = LocalDateTime.now();
        if (lt.isAfter(timeFromPost))
            timeFromPost = lt;
        post.setTime(timeFromPost);
        post.setIsActive(inputPost.getActive());
        post.setTitle(inputPost.getTitle());
        post.setText(inputPost.getText());
        if (needToDoExtraOperation) {
            post.setUserId(userId);
            post.setModerationStatus(ModerationStatus.NEW);
        }
        return post;
    }

    private List<Tag>  saveTags(List<String> tags){
        List<Tag> needToAddTags = new ArrayList<>();
        List<Tag> noNeedToAdd = tagRepository.findTagsByNameList(tags);

        for (String tagName: tags){
            Tag newTag = new Tag();
            newTag.setName(tagName);
            boolean isNew = true;
            for (Tag tag: noNeedToAdd){
                if (tag.getName().equals(tagName)){
                    isNew = false;
                    break;
                }
            }
            if (isNew){
                needToAddTags.add(newTag);
            }
        }
        List<Tag> newNeedToAddTags = (List<Tag>) tagRepository.saveAll(needToAddTags);
        noNeedToAdd.addAll(newNeedToAddTags);
        return noNeedToAdd;
    }

    private void processTag2Post(Post currentPost, List<Tag> newTags){
        List<Tag2Post> tag2Posts = new ArrayList<>();
        for (Tag tag: newTags){
            Tag2Post tag2Post = new Tag2Post();
            tag2Post.setTagId(tag.getId());
            tag2Post.setPostId(currentPost.getId());
            tag2Posts.add(tag2Post);
        }
        tag2PostRepository.saveAll(tag2Posts);
    }

    public ResultResponse validatePostData(PostRequest inputPost){
        boolean hasError = false;
        Errors error = new Errors();
        if ( inputPost.getTitle().length() == 0) {
            error.setTitle("Заголовок не установлен");
            hasError = true;
        }
        else if ( inputPost.getTitle().length() < 3){
            error.setTitle("Текст заголовка слишком короткий");
            hasError = true;
        }
        if ( inputPost.getText().length() == 0) {
            error.setText("Текст публикации не установлен");
            hasError = true;
        }
        else if ( inputPost.getText().length() < 50){
            error.setText("Текст публикации слишком короткий");
            hasError = true;
        }
        ResultResponse resultResponse = new ResultResponse();
        resultResponse.setResult(true);
        if (hasError){
            resultResponse.setResult(false);
            resultResponse.setErrors(error);
        }
        return resultResponse;
    }


    public ResponseEntity<ResultResponse> putPost(PostRequest inputPost, int userId){
        Post post = convertInputToPost(inputPost, userId, new Post(), true);
        List<Tag> newTags = saveTags(inputPost.getTags());
        Post newPost = postRepository.save(post);
        processTag2Post(newPost, newTags);
        return ResponseEntity.ok(new ResultResponse(true));
    }

    public ResponseEntity<ResultResponse> updatePost(PostRequest inputPost, int postId, int userId) {

        Optional<Post> tempOldPost = postRepository.findById(postId);
        if (tempOldPost.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        Post oldPost = tempOldPost.get();
        convertInputToPost(inputPost, 0, oldPost , false);
        if (oldPost.getUserId() != userId) {//moderator
            oldPost.setModeratorId(userId);
        }
        else {//author
            oldPost.setModerationStatus(ModerationStatus.NEW);
        }

        List<Tag> newTags = saveTags(inputPost.getTags());
        Post newPost = postRepository.save(oldPost);
        processTag2Post(newPost, newTags);
        return ResponseEntity.ok(new ResultResponse(true));
    }


    public ResponseEntity<ResultResponse> likeOrDislikePost(LikeRequest like, int userId, boolean isLikeNoDislike) {

        byte likeOrDislikeValue = -1;
        if (isLikeNoDislike){
            likeOrDislikeValue = 1;
        }

        ResultResponse resultResponse = new ResultResponse(false);
        Optional<PostVote> tempVote = voteRepository.findByPostAndUser(like.getPostId(), userId);
        if (tempVote.isEmpty()){
            PostVote  postVote = new PostVote();
            postVote.setValue(likeOrDislikeValue);
            postVote.setTime(LocalDateTime.now());
            postVote.setUserId(userId);
            postVote.setPostId(like.getPostId());
            voteRepository.save(postVote);
        }else{
            PostVote  postVote = tempVote.get();
            byte previousLike = postVote.getValue();
            if (previousLike == likeOrDislikeValue) {
                return ResponseEntity.ok(resultResponse);
            }
            postVote.setValue(likeOrDislikeValue);
            postVote.setTime(LocalDateTime.now());
            voteRepository.save(postVote);
        }
        resultResponse.setResult(true);
        return ResponseEntity.ok(resultResponse);
    }

    public ResponseEntity<StatisticResponse> getAllStatistic(){
        StatisticResponse statisticResponse = new StatisticResponse();
        statisticResponse.setPostsCount((int)postRepository.size());
        statisticResponse.setLikesCount((int)voteRepository.countAllLikes());
        statisticResponse.setDislikesCount((int)voteRepository.countAllDisLikes());
        statisticResponse.setViewsCount((int)postRepository.sumViewCount());
        statisticResponse.setFirstPublication(postRepository.earliestTime().toEpochSecond(ZoneOffset.UTC));
        return ResponseEntity.ok(statisticResponse);
    }

    public ResponseEntity<StatisticResponse> getAllStatisticOfMine(User user){
        StatisticResponse statisticResponse = new StatisticResponse();
        int id = user.getId();
        statisticResponse.setPostsCount((int)postRepository.countPostsOfUser(id));//
        statisticResponse.setLikesCount((int)voteRepository.countAllLikesOfUser(id));
        statisticResponse.setDislikesCount((int)voteRepository.countAllDislikesOfUser(id));//
        statisticResponse.setViewsCount((int)postRepository.sumViewCountOfUser(id));
        statisticResponse.setFirstPublication(postRepository.earliestTimeOfUser(id).toEpochSecond(ZoneOffset.UTC));
        return ResponseEntity.ok(statisticResponse);
    }

    public ResultResponse moderatePost( ModerateRequest inputPost, User user )
    {
        ResultResponse  resultResponse = new ResultResponse(false);
        if (user.getIsModerator() != 1 ||
                ((!inputPost.getDecision().equals("accept")) &&
                        (!inputPost.getDecision().equals("decline")))){
            return resultResponse;
        }
        Optional<Post> post = postRepository.findById(inputPost.getPostId());
        if (post.isEmpty()){
            return resultResponse;
        }
        Post currentPost = post.get();
        currentPost.setModeratorId(user.getId());
        if (inputPost.getDecision().equals("accept")){
            currentPost.setModerationStatus(ModerationStatus.ACCEPTED);
        }else{
            currentPost.setModerationStatus(ModerationStatus.DECLINED);
        }
        postRepository.save(currentPost);
        resultResponse.setResult(true);
        return resultResponse;
    }
}
