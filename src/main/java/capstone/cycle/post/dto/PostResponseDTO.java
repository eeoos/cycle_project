package capstone.cycle.post.dto;

import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.file.entity.File;
import capstone.cycle.like.dto.LikeStatus;
import capstone.cycle.post.entity.Post;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private SimpleUserInfoDTO author;
    private List<String> imageUrls;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private Long viewCount;
    private int likeCount;
    private LikeStatus likeStatus;
    private String categoryName;

    public PostResponseDTO(Post post, LikeStatus likeStatus) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.author = post.getAuthor() != null ? SimpleUserInfoDTO.from(post.getAuthor()) : null;
        this.updatedAt = post.getUpdatedAt();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.likeStatus = likeStatus;
        this.categoryName = post.getCategory().getDisplayName();
        if (post.getContentImageGroup() != null && post.getContentImageGroup().getFiles() != null) {
            this.imageUrls = post.getContentImageGroup().getFiles().stream()
                    .map(file -> "/api/files/" + file.getId())
                    .collect(Collectors.toList());
        }
    }
}
