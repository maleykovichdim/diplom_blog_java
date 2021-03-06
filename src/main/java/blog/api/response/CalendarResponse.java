package blog.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class CalendarResponse {

    private List<Integer> years;
    private Map<String, Long> posts;
}
