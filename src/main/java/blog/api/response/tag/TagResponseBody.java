package blog.api.response.tag;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagResponseBody {

    private String name;
    private double weight;

}
