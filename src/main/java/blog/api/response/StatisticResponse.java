package blog.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticResponse {
    private Long postsCount;
    private Long likesCount;
    private Long dislikesCount;
    private Long viewsCount;
    private Long firstPublication;
}

