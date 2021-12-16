package blog.model.repository;

import blog.model.PostComment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CommentRepository extends CrudRepository<PostComment, Integer> {

    Optional<PostComment> findById(int id);

}
