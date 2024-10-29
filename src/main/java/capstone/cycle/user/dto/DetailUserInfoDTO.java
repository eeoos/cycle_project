package capstone.cycle.user.dto;

import capstone.cycle.user.entity.User;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DetailUserInfoDTO {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private String workAddress;
    private String homeAddress;

    public static DetailUserInfoDTO from(User user) {
        return DetailUserInfoDTO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(getProfileImageUrl(user))
                .workAddress(user.getWorkAddress().getFullAddress())
                .homeAddress(user.getHomeAddress().getFullAddress())
                .build();
    }

    private static String getProfileImageUrl(User user) {
        if (user.getProfileImage() != null) {
            return "/api/files/" + user.getProfileImage().getId();
        } else if (user.getSnsProfileImageUrl() != null && !user.getSnsProfileImageUrl().isEmpty()) {
            return user.getSnsProfileImageUrl();
        } else {
            return "updateProfileImage";
        }
    }
}
