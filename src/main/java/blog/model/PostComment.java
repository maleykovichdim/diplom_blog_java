package blog.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Entity
@Table(name="post_comments")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;


    @Column(name = "parent_id", nullable = true)
    private Integer parentId;


    @Column(name = "post_id", nullable = false)
    private int postId;


    @Column(name = "user_id", nullable = false)
    private int userId;


    @Column(nullable = false)
    private LocalDateTime time;


    @Lob
    @Column(columnDefinition="text", nullable = false)
    private String text;


    @ManyToOne(cascade = CascadeType.ALL,  fetch=FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName="id", insertable = false, updatable = false)
    private Post post;


    @ManyToOne(cascade = CascadeType.ALL,  fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName="id", insertable = false, updatable = false)
    private User user;
}
