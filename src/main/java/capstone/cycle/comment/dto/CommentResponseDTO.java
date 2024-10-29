package capstone.cycle.comment.dto;

import capstone.cycle.comment.entity.Comment;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {

    private Long id;
    private String content;
    private SimpleUserInfoDTO author;
    private Long parentId;
    private List<CommentResponseDTO> replies;
    private int likeCount;
    private boolean likedByCurrentUser;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static CommentResponseDTO from(Comment comment, Long currentUserId) {
        CommentResponseDTO.CommentResponseDTOBuilder builder = CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(SimpleUserInfoDTO.from(comment.getAuthor()))
                .likeCount(comment.getLikeCount())
                .likedByCurrentUser(comment.getLikes().stream()
                        .anyMatch(like -> like.getUser().getId().equals(currentUserId)))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt());

        if (comment.getParent() != null) {
            // 대댓글인 경우 부모 댓글의 ID를 설정하고 replies는 null로 설정
            builder.parentId(comment.getParent().getId())
                    .replies(null);  // 명시적으로 null 설정
        } else {
            // 원본 댓글인 경우 parentId는 null이고 대댓글 목록을 설정
            builder.parentId(null)
                    .replies(comment.getReplies().stream()
                            .map(reply -> CommentResponseDTO.from(reply, currentUserId))
                            .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
