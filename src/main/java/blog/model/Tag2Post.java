package blog.model;

import lombok.*;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tag2post")
public class Tag2Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;


    @Column(name = "post_id", nullable = false)
    private int postId;


    @Column(name = "tag_id", nullable = false)
    private int tagId;
}
