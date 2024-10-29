package capstone.cycle.post.dto;

import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.post.entity.Post;
import capstone.cycle.post.entity.PostCategory;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimplePostResponseDTO {
    private Long id;
    private String title;
    private String categoryName;
    private Long viewCount;
    private int likeCount;
    private boolean isLiked;
    private String firstImageUrl;
    private String authorName;

    public static SimplePostResponseDTO fromPost(Post post, boolean isLiked) {
        return SimplePostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .categoryName(post.getCategory().getDisplayName())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .firstImageUrl(getFirstImageUrl(post))
                .authorName(post.getAuthor().getNickname())
                .isLiked(isLiked)
                .build();
    }

    private static String getFirstImageUrl(Post post) {
        if (post.getContentImageGroup() != null &&
                !post.getContentImageGroup().getFiles().isEmpty()) {
            return "/api/files/" + post.getContentImageGroup().getFiles().get(0).getId();
        }
        return null;
    }

}