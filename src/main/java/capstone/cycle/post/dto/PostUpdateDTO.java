package capstone.cycle.post.dto;

import capstone.cycle.post.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostUpdateDTO {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Size(min = 1, max = 5000, message = "내용은 1자 이상 5000자 이하여야 합니다.")
    private String content;
    private List<Long> deletedImageIds;

    @NotNull(message = "카테고리는 필수 입력 항목입니다.")
    private PostCategory category;
}
