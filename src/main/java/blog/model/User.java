package blog.model;

import blog.model.enums.Role;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;


    @Column(name = "is_moderator", nullable = false)
    @ColumnDefault("0")
    private Byte isModerator;


    @Column(name = "reg_time", nullable = false)
    private LocalDateTime regTime;


    @Column(nullable = false)
    private String name;


    @Column(nullable = false)
    private String email;


    @Column(nullable = false)
    private String password;

    private String code;

    @Lob
    @Column(columnDefinition="text")
    private String photo;

    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName="id", insertable = false, updatable = false)
    private Collection<Post> posts;

    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName="id", insertable = false, updatable = false)
    private Collection<PostVote> postVotes;

    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName="id", insertable = false, updatable = false)
    private Collection<PostComment> postComments;

    public Role getRole(){
        return isModerator == 1 ? Role.MODERATOR : Role.USER;
    }
}
