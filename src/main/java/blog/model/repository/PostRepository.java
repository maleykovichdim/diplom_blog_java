package blog.model.repository;

import blog.model.enums.ModerationStatus;
import blog.model.Post;
import blog.model.other.PostsCountPerDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Integer>{

    @Override
    Optional<Post> findById(Integer integer);

    @Query( "SELECT count(*) FROM Post p WHERE p.userId=:userId" )
    Long countPostsOfUser(@Param("userId") int userId);

    @Query( "SELECT SUM(p.viewCount) FROM Post p WHERE p.userId=:userId" )
    Long sumViewCountOfUser(@Param("userId") int userId);

    @Query( "SELECT MIN(p.time) FROM Post p WHERE p.userId=:userId" )
    LocalDateTime earliestTimeOfUser(@Param("userId") int userId);

    @Query( "select f from Post f" )
    List<Post> findAllCustom(Pageable pageable);

    @Query( "SELECT COUNT(p) FROM Post p" )
    Long size();

    @Query( "SELECT SUM(p.viewCount) FROM Post p" )
    Long sumViewCount();

    @Query( "SELECT MIN(p.time) FROM Post p" )
    LocalDateTime earliestTime();

    //sort by Pageable
    @Query("SELECT p FROM Post p WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime ")
            List<Post> findPosts( @Param("activeStatus") Byte isActiveStatus,
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("checkTime") LocalDateTime currentTime,
            Pageable pageable
    );

    //sort by jpql - popular
    @Query("SELECT p FROM Post p WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime order by size(p.postComments) desc")//Found use of deprecated 'collection property' syntax in HQL/JPQL query [p.postComments.size]; use collection function syntax instead [size(p.postComments)].
    List<Post> findPostsPopular( @Param("activeStatus") Byte isActiveStatus,
                          @Param("moderationStatus") ModerationStatus moderationStatus,
                          @Param("checkTime") LocalDateTime currentTime,
                          Pageable pageable
    );


    //sort by jpql - best
    @Query("SELECT p, COUNT(pv.value) AS countlike FROM Post p "+
            "LEFT OUTER JOIN p.postVotes pv "+
            "ON pv.value = 1 "+
            "WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime "+
            "GROUP BY p.id "+
            "order by countlike desc "
    )
    List<Post> findPostsBest( @Param("activeStatus") Byte isActiveStatus,
                              @Param("moderationStatus") ModerationStatus moderationStatus,
                              @Param("checkTime") LocalDateTime currentTime,
                              Pageable pageable
    );


    //quantity of elements for this condition
    @Query("SELECT COUNT(p) FROM Post p WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime")
    long findNumPostsWithCriteria( @Param("activeStatus") Byte isActiveStatus,
                          @Param("moderationStatus") ModerationStatus moderationStatus,
                          @Param("checkTime") LocalDateTime currentTime
    );

    //sort by Pageable
    @Query("SELECT p FROM Post p WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime and "+
            "p.title LIKE CONCAT('%',:query,'%') ")// LIKE UPPER(CONCAT('%',:asunto,'%'))
    List<Post> findPostsSearch( @Param("activeStatus") Byte isActiveStatus,
                                @Param("moderationStatus") ModerationStatus moderationStatus,
                                @Param("checkTime") LocalDateTime currentTime,
                                @Param("query") String query,
                                Pageable pageable
    );



    @Query("SELECT COUNT(p) FROM Post p WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime and " +
            "p.title LIKE CONCAT('%',:query,'%') ")
    long findNumPostsWithCriteriaAndSearch( @Param("activeStatus") Byte isActiveStatus,
                                           @Param("moderationStatus") ModerationStatus moderationStatus,
                                           @Param("checkTime") LocalDateTime currentTime,
                                           @Param("query") String query
    );

    //sort by Pageable
    @Query("SELECT p FROM Post p WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime and "+
            "p.time >= :startDate and p.time < :endDate ")
    List<Post> findPostsByDate( @Param("activeStatus") Byte isActiveStatus,
                                @Param("moderationStatus") ModerationStatus moderationStatus,
                                @Param("checkTime") LocalDateTime currentTime,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable
    );



    @Query("SELECT COUNT(p) FROM Post p WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime and " +
            "p.time >= :startDate and p.time < :endDate ")
    long findNumPostsWithCriteriaAndByDate( @Param("activeStatus") Byte isActiveStatus,
                                            @Param("moderationStatus") ModerationStatus moderationStatus,
                                            @Param("checkTime") LocalDateTime currentTime,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate
    );

    //sort by Pageable
    @Query("SELECT p FROM Post p "+
            "JOIN p.tags tgs "+
            "WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime and "+
            "tgs.name = :tag "
    )
    List<Post> findPostsByTag( @Param("activeStatus") Byte isActiveStatus,
                                @Param("moderationStatus") ModerationStatus moderationStatus,
                                @Param("checkTime") LocalDateTime currentTime,
                                @Param("tag") String tag,
                                Pageable pageable
    );



    @Query("SELECT COUNT(p) FROM Post p "+
            "JOIN p.tags tgs "+
            "WHERE p.isActive = :activeStatus and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.time < :checkTime and "+
            "tgs.name = :tag "
    )
    long findNumPostsWithCriteriaAndByTag( @Param("activeStatus") Byte isActiveStatus,
                                           @Param("moderationStatus") ModerationStatus moderationStatus,
                                           @Param("checkTime") LocalDateTime currentTime,
                                           @Param("tag") String tag
    );

    @Query("SELECT COUNT(p) FROM Post p "+
            "WHERE p.moderationStatus = 'NEW' "
    )
    long countNumNewPosts();

    @Query("SELECT COUNT(p) FROM Post p "+
            "WHERE p.userId = :userId and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.isActive = :isActive "
    )
    long countNumMyPostsWithModerationStatus(    @Param("moderationStatus") ModerationStatus moderationStatus,
                                                 @Param("isActive") Byte isActive,
                                                 @Param("userId") int userId

    );

    @Query("SELECT p FROM Post p "+
            "WHERE p.userId = :userId and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.isActive = :isActive "
    )
    List<Post> findMyPostsWithModerationStatus(  @Param("moderationStatus") ModerationStatus moderationStatus,
                                          @Param("isActive") Byte isActive,
                                          @Param("userId") int userId,
                                          Pageable pageable

    );

    @Query("SELECT COUNT(p) FROM Post p "+
            "WHERE p.userId = :userId and "+
            "p.isActive = :isActive "
    )
    long countNumMyPostsWithoutModerationStatus(    @Param("isActive") Byte isActive,
                                                    @Param("userId") int userId
    );

    @Query("SELECT p FROM Post p "+
            "WHERE p.userId = :userId and "+
            "p.isActive = :isActive "
    )
    List<Post> findMyPostsWithoutModerationStatus(    @Param("isActive") Byte isActive,
                                                      @Param("userId") int userId,
                                                      Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Post p "+
            "WHERE "+
            //"p.moderatorId = :userId and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.isActive = :isActive "
    )
    long countNumPostsModeration(    @Param("moderationStatus") ModerationStatus moderationStatus,
                                                 @Param("isActive") Byte isActive
                                                 //,@Param("userId") int userId

    );

    @Query("SELECT p FROM Post p "+
            "WHERE "+
            //"p.moderatorId = :userId and "+
            "p.moderationStatus = :moderationStatus and "+
            "p.isActive = :isActive "
    )
    List<Post> findPostsModeration(  @Param("moderationStatus") ModerationStatus moderationStatus,
                                                 @Param("isActive") Byte isActive,
                                                 //@Param("userId") int userId,
                                                 Pageable pageable

    );

    @Query("SELECT new blog.model.other.PostsCountPerDate(DATE_FORMAT(p.time,'%Y-%m-%d') AS postsDate" +
            ", COUNT(p.id) AS postsCount) FROM Post p" +
            " WHERE year(p.time) = :year" +
            " GROUP BY DATE_FORMAT(p.time,'%Y-%m-%d')")
    List<PostsCountPerDate> findPostsCountPerDateByYear(int year);

    @Query("SELECT DISTINCT year(p.time) FROM Post p")
    List<Integer> findDistinctYears();

    Post findPostById(@Param("id") int id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE posts SET posts.view_count = posts.view_count + 1 WHERE posts.id = :id",
            nativeQuery = true)
    void updateViewCount(@Param("id") int id);


}


