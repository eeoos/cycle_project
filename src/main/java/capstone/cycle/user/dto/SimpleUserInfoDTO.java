package capstone.cycle.user.dto;

import capstone.cycle.file.dto.ProfileDTO;
import capstone.cycle.user.entity.User;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleUserInfoDTO {
    private Long id;
    private String nickname;
    private String profileImageUrl;

    public static SimpleUserInfoDTO from(User user) {
        return SimpleUserInfoDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(getProfileImageUrl(user))
                .build();
    }

    private static String getProfileImageUrl(User user) {
        if (user.getProfileImage() != null) {
            return "/api/files/" + user.getProfileImage().getId();
        } else if (user.getSnsProfileImageUrl() != null && !user.getSnsProfileImageUrl().isEmpty()) {
            return user.getSnsProfileImageUrl();
        } else {
            return "default-profile-image.jpg";
        }
    }
}
