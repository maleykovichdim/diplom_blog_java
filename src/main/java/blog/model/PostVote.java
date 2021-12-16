package blog.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="post_votes")
public class PostVote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;


    @Column(name = "user_id", nullable = false)
    private int userId;


    @Column(name = "post_id", nullable = false)
    private int postId;


    @Column(nullable = false)
    private LocalDateTime time;


    @Column(nullable = false)
    private Byte value;

    @ManyToOne(cascade = CascadeType.ALL,  fetch=FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName="id", insertable = false, updatable = false)
    private Post post;


    @ManyToOne(cascade = CascadeType.ALL,  fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName="id", insertable = false, updatable = false)
    private User user;

}
