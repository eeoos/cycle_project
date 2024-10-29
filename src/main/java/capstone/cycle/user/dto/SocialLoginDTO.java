package capstone.cycle.user.dto;

import capstone.cycle.file.entity.File;
import capstone.cycle.user.annotation.IsSocialProvider;
import capstone.cycle.user.entity.User;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.function.Function;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SocialLoginDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "socialId는 빈 값일 수 없습니다")
    private String socialId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "KAKAO / NAVER / GOOGLE", example = "KAKAO")
    @IsSocialProvider
    private String socialProvider;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 빈 값일 수 없습니다")
    private String nickname;

    @Hidden
    private String role;

    @Email(message = "올바른 이메일 형식이어야 합니다")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String snsProfileImageUrl;

//    private MultipartFile profile;

    public User toEntity(Function<String, File> profileImageConverter) {
        User.UserBuilder builder = User.builder()
                .socialId(socialId)
                .socialProvider(socialProvider)
                .nickname(nickname)
                .role(role)
                .email(email);

        if (snsProfileImageUrl != null && !snsProfileImageUrl.isEmpty()) {
            File profileImage = profileImageConverter.apply(snsProfileImageUrl);
            builder.profileImage(profileImage);
        }

        return builder.build();
    }

    // 정적 팩토리 메서드
    public static SocialLoginDTO of(String socialId, String socialProvider, String nickname,
                                    String email, String snsProfileImageUrl) {
        return SocialLoginDTO.builder()
                .socialId(socialId)
                .socialProvider(socialProvider)
                .nickname(nickname)
                .email(email)
                .snsProfileImageUrl(snsProfileImageUrl)
                .build();
    }


}
