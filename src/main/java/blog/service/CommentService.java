package blog.service;

import blog.api.request.PostCommentRequest;
import blog.api.response.Errors;
import blog.api.response.ResultResponse;
import blog.api.response.IdResponse;
import blog.model.Post;
import blog.model.PostComment;
import blog.model.repository.CommentRepository;
import blog.model.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
    @Configurable
    public class CommentService {


        private final CommentRepository commentRepository;
        private final PostRepository postRepository;

        @Autowired
        public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
            this.commentRepository = commentRepository;
            this.postRepository = postRepository;
        }

        public ResultResponse validatePutComment(PostCommentRequest inputPostComment){
            ResultResponse resultResponse = new ResultResponse(true);
            if (inputPostComment.getText().length() < 3){
                Errors error = new Errors();
                error.setText("Текст комментария не задан или слишком короткий");
                resultResponse.setResult(false);
                resultResponse.setErrors(error);
                return resultResponse;
            }
            return resultResponse;
        }

        public ResponseEntity<?> putComment(PostCommentRequest inputPostComment, int userId )  {

            PostComment parent = null;
            if (inputPostComment.getParentId().isPresent()){
                Optional<PostComment> tempPostComment = commentRepository.findById(inputPostComment.getParentId().get());
                if (tempPostComment.isPresent()) {
                    parent = tempPostComment.get();
                }
                else
                {
                    return ResponseEntity.badRequest()
                            .body("Error. Parent comment with this id doesn`t exist.");
                }
            }

            Optional<Post> tempPost = postRepository.findById(inputPostComment.getPostId());
            if (tempPost.isEmpty()){
                return ResponseEntity.badRequest()
                        .body("Error. Post with this id doesn`t exist.");
            }
            Post post = tempPost.get();

            PostComment postComment = new PostComment();
            if (parent != null){
                postComment.setParentId(parent.getId());
            }
            postComment.setPostId(post.getId());
            postComment.setUserId(userId);
            postComment.setText(inputPostComment.getText());
            postComment.setTime(LocalDateTime.now());
            PostComment newPostComment = commentRepository.save(postComment);
            IdResponse idResponse = new IdResponse();
            idResponse.setId(newPostComment.getId());
            return ResponseEntity.ok(idResponse);
        }

    }
