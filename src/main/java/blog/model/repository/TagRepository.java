package blog.model.repository;

import blog.api.response.tag.TagResponseBody;
import blog.model.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends CrudRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);


    List<Tag> findAll();

    @Query(
            "SELECT new blog.api.response.tag.TagResponseBody("+
                    "t.name, "+
                    "(( COUNT(*) * 1.0 )/  "+
                    "( SELECT COUNT(*) FROM Post pp  WHERE pp.isActive=1 and pp.moderationStatus='ACCEPTED' and pp.time < :checkTime ) "+
                    ") AS counts ) "+
                    "FROM Tag t "+
                    "JOIN t.posts p "+
                    "ON p.isActive=1 and "+
                    "p.moderationStatus='ACCEPTED' and "+
                    "p.time < :checkTime  "+
                    "WHERE t.name LIKE CONCAT(:query,'%') "+
                    "GROUP BY t.id "+
                    "ORDER  BY counts DESC"
    )
    List<TagResponseBody> findTagsWithQuery(
            @Param("checkTime") LocalDateTime currentTime,
            @Param("query") String query
    );

    @Query(
            "SELECT new blog.api.response.tag.TagResponseBody("+
            "t.name, "+
            "(( COUNT(*) * 1.0 )/  "+
            "( SELECT COUNT(*) FROM Post pp  WHERE pp.isActive=1 and pp.moderationStatus='ACCEPTED' and pp.time < :checkTime ) "+
            ") AS counts ) "+
            "FROM Tag t "+
            "JOIN t.posts p "+
            "ON p.isActive=1 and "+
            "p.moderationStatus='ACCEPTED' and "+
            "p.time < :checkTime  "+
            "GROUP BY t.id "+
            "ORDER  BY counts DESC"
            )
    List<TagResponseBody> findTags(
            @Param("checkTime") LocalDateTime currentTime
    );

    @Query("SELECT t FROM Tag t "+
            "WHERE t.name IN (:nameList) "
    )
    List<Tag> findTagsByNameList(@Param("nameList") List<String> nameList );

    @Query("SELECT t FROM Tag t "+
            "WHERE t.name NOT IN (:nameList) "
    )
    List<Tag> findTagsNotInNameList(@Param("nameList") List<String> nameList );

}
