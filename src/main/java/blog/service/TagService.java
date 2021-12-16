package blog.service;

import blog.api.response.tag.TagResponse;
import blog.api.response.tag.TagResponseBody;
import blog.model.repository.PostRepository;
import blog.model.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Configurable
public class TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository, PostRepository postRepository) {
        this.tagRepository = tagRepository;
    }

    public ResponseEntity<TagResponse>
    getTags(String query)
    {
        List<TagResponseBody> tagResponseBody;
        if (query == null || query.isEmpty()){
            tagResponseBody = tagRepository.findTags(LocalDateTime.now());
        }
        else{
            tagResponseBody = tagRepository.findTagsWithQuery(LocalDateTime.now(), query);
        }

        double maxTagsWeight = tagResponseBody.get(0).getWeight();
        double k = 1 / maxTagsWeight;

        for (TagResponseBody tag: tagResponseBody) {
            tag.setWeight(tag.getWeight()* k);
            tag.setWeight(Math.max(tag.getWeight(), 0.3));
        }
        return ResponseEntity.ok(new TagResponse(tagResponseBody));
    }
}