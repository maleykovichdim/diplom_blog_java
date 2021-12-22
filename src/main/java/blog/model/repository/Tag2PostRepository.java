package blog.model.repository;

import blog.model.Tag2Post;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface Tag2PostRepository extends CrudRepository<Tag2Post, Integer> {


    @Query("SELECT tp FROM Tag2Post tp "+
            "WHERE tp.postId=:curPostId "
    )
    List<Tag2Post> findTag2PostList(@Param("curPostId") int curPostId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Tag2Post tp WHERE tp.id IN :idList")
    void cleanTag2Post(@Param("idList") List<Integer> idList);

}
