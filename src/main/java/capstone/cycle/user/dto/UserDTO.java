package capstone.cycle.user.dto;

import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.file.dto.FileGroupDTO;
import capstone.cycle.file.entity.File;
import capstone.cycle.file.entity.FileGroup;
import capstone.cycle.user.entity.Address;
import capstone.cycle.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {
    private Long userId;
    private String socialId;
    private String socialProvider;
    private String email;
    private String nickname;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private FileDTO fileDTO;
    private String refreshToken;
    private Address workAddress;
    private Address homeAddress;
    private String snsProfileImageUrl;

    public UserDTO(Long userId, String socialId, String socialProvider) {
        this.userId = userId;
        this.socialId = socialId;
        this.socialProvider = socialProvider;
    }
    public User toEntity(Supplier<File> fileSupplier) {
        User.UserBuilder entity = User.builder()
                .id(userId)
                .socialId(socialId)
                .socialProvider(socialProvider)
                .email(email)
                .nickname(nickname)
                .role(role)
                .refreshToken(refreshToken)
                .workAddress(workAddress)
                .homeAddress(homeAddress)
                .snsProfileImageUrl(snsProfileImageUrl);
        if (fileDTO != null) {
            entity.profileImage(fileSupplier.get());
        }
        return entity.build();
    }

    public User toEntity() {
        User.UserBuilder userBuilder = User.builder()
                .id(userId)
                .socialId(socialId)
                .socialProvider(socialProvider)
                .email(email)
                .nickname(nickname)
                .role(role)
                .refreshToken(refreshToken)
                .workAddress(workAddress)
                .homeAddress(homeAddress)
                .snsProfileImageUrl(snsProfileImageUrl);

        if (fileDTO != null) {
            File.FileBuilder fileBuilder = File.builder()
                    .id(fileDTO.getId())
                    .name(fileDTO.getName())
                    .originalName(fileDTO.getOriginalName())
                    .path(fileDTO.getPath())
                    .contentType(fileDTO.getContentType())
                    .size(fileDTO.getSize())
                    .extension(fileDTO.getExtension())
                    .checksum(fileDTO.getChecksum())
                    .createdAt(fileDTO.getCreatedAt());

            // FileGroup 처리
            if (fileDTO.getFileGroupId() != null) {
                FileGroup fileGroup = FileGroup.builder()
                        .id(fileDTO.getFileGroupId())
                        .build();
                fileBuilder.fileGroup(fileGroup);
            }

            userBuilder.profileImage(fileBuilder.build());
        }
        return userBuilder.build();
    }

    // refreshToken을 변경한 새로운 인스턴스를 반환하는 메서드
    public UserDTO withRefreshToken(String newRefreshToken) {
        return UserDTO.builder()
                .userId(this.userId)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(this.nickname)
                .role(this.role)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .fileDTO(this.fileDTO)
                .refreshToken(newRefreshToken)
                .workAddress(this.workAddress)
                .homeAddress(this.homeAddress)
                .snsProfileImageUrl(this.snsProfileImageUrl)
                .build();
    }

}
