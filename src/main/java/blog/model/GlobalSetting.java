package blog.model;

import lombok.*;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="global_settings")
public class GlobalSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;


    @Lob
    @Column(columnDefinition="text", nullable = false)
    private String code;


    @Lob
    @Column(columnDefinition="text", nullable = false)
    private String name;


    @Lob
    @Column(columnDefinition="text", nullable = false)
    private String value;


}
