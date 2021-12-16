package blog.model;

import blog.model.enums.ModerationStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="posts")
public class Post implements Comparable<Post>{

    @Override
    public int compareTo(Post otherPost) {
        return Integer.compare(getId(), otherPost.getId());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "is_active", nullable = false)
    private Byte isActive;

    @Column(name = "moderation_status", nullable = false, length = 8, columnDefinition = "varchar(8) default 'NEW'")
    @Enumerated(value = EnumType.STRING)
    private ModerationStatus moderationStatus = ModerationStatus.NEW;

    @Column(name = "moderator_id")
    private int moderatorId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition="text", nullable = false)
    private String text;

    @Column(nullable = false, name = "view_count")
    private int viewCount;

    @ManyToOne(cascade = CascadeType.ALL,  fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName="id", insertable = false, updatable = false)
    private User user;

    @OneToMany(cascade = CascadeType.ALL,  fetch=FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName="id")
    private List<PostVote> postVotes;

    public void addPostVote(PostVote postVote) {
        this.postVotes.add(postVote);
        postVote.setPost(this);
    }

    @OneToMany(cascade = CascadeType.ALL,  fetch=FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName="id")
    private List<PostComment> postComments;

    @ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "posts")
    private List<Tag> tags;

    @OneToMany(cascade = CascadeType.ALL,  fetch=FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName="id")
    private List<Tag2Post> tag2Posts;
}



