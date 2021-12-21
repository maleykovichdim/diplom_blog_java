package blog.model.repository;

import blog.model.PostVote;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VoteRepository extends CrudRepository<PostVote, Integer> {

    Optional<PostVote> findById(int id);


    @Query("SELECT v FROM PostVote v  "+
            " WHERE v.postId=:postId AND v.userId=:userId" )
    Optional<PostVote>  findByPostAndUser(
                         @Param("postId") int postId,
                         @Param("userId") int userId
    );


    @Query("SELECT COUNT(v) FROM PostVote v "+
            "WHERE v.userId=:userId AND v.value=1"
    )
    Long countAllLikesOfUser(@Param("userId") int userId);

    @Query("SELECT COUNT(v) FROM PostVote v "+
            "WHERE v.userId=:userId AND v.value=-1"
    )
    Long countAllDislikesOfUser(@Param("userId") int userId);


    @Query("SELECT COUNT(v) FROM PostVote v "+
            "WHERE v.value = 1 "
    )
    Long countAllLikes();

    @Query("SELECT COUNT(v) FROM PostVote v "+
            "WHERE v.value = -1 "
    )
    Long countAllDisLikes();
}
