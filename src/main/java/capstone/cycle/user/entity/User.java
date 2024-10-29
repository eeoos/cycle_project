package capstone.cycle.user.entity;

import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.file.dto.ProfileDTO;
import capstone.cycle.file.entity.File;
import capstone.cycle.file.entity.FileGroup;
import capstone.cycle.user.dto.SimpleUserInfoDTO;
import capstone.cycle.user.dto.UserDTO;
import capstone.cycle.user.dto.DetailUserInfoDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotNull
    private String socialId;

    @Column(nullable = false, unique = true)
    @NotNull
    private String email;

    @NotNull
    private String nickname;

    private String snsProfileImageUrl;

    private String role;
    private String refreshToken;

    @NotNull
    private String socialProvider;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "roadAddress", column = @Column(name = "WORK_ROAD_ADDRESS")),
            @AttributeOverride(name = "buildingNumber", column = @Column(name = "WORK_BUILDING_NUMBER")),
            @AttributeOverride(name = "detailAddress", column = @Column(name = "WORK_DETAIL_ADDRESS")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "WORK_ZIP_CODE"))
    })
    private Address workAddress = Address.createDefaultAddress();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "roadAddress", column = @Column(name = "HOME_ROAD_ADDRESS")),
            @AttributeOverride(name = "buildingNumber", column = @Column(name = "HOME_BUILDING_NUMBER")),
            @AttributeOverride(name = "detailAddress", column = @Column(name = "HOME_DETAIL_ADDRESS")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "HOME_ZIP_CODE"))
    })
    private Address homeAddress = Address.createDefaultAddress();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private File profileImage;


    // 정적 팩토리 메서드
    public static User createUser(String socialId, String socialProvider, String email,
                                  String nickname, String role, String snsProfileImageUrl) {
        return User.builder()
                .socialId(socialId)
                .socialProvider(socialProvider)
                .email(email)
                .nickname(nickname)
                .role(role)
                .snsProfileImageUrl(snsProfileImageUrl)
                .workAddress(Address.createDefaultAddress())
                .homeAddress(Address.createDefaultAddress())
                .build();
    }

    // refreshToken 업데이트
    public User withRefreshToken(String newRefreshToken) {
        return User.builder()
                .id(this.id)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(this.nickname)
                .role(this.role)
                .refreshToken(newRefreshToken)
                .workAddress(this.workAddress)
                .homeAddress(this.homeAddress)
                .profileImage(this.profileImage)
                .snsProfileImageUrl(this.snsProfileImageUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    // 프로필 이미지 업데이트
    public User withProfileImage(File newProfileImage) {
        return User.builder()
                .id(this.id)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(this.nickname)
                .role(this.role)
                .refreshToken(this.refreshToken)
                .workAddress(this.workAddress)
                .homeAddress(this.homeAddress)
                .profileImage(newProfileImage)
                .snsProfileImageUrl(null)  // 프로필 이미지 설정 시 SNS 이미지 URL 제거
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    // 닉네임 업데이트
    public User withNickname(String newNickname) {
        return User.builder()
                .id(this.id)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(newNickname)
                .role(this.role)
                .refreshToken(this.refreshToken)
                .workAddress(this.workAddress)
                .homeAddress(this.homeAddress)
                .profileImage(this.profileImage)
                .snsProfileImageUrl(this.snsProfileImageUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    // 근무지 주소 업데이트
    public User withWorkAddress(Address newWorkAddress) {
        return User.builder()
                .id(this.id)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(this.nickname)
                .role(this.role)
                .refreshToken(this.refreshToken)
                .workAddress(newWorkAddress)
                .homeAddress(this.homeAddress)
                .profileImage(this.profileImage)
                .snsProfileImageUrl(this.snsProfileImageUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    // 거주지 주소 업데이트
    public User withHomeAddress(Address newHomeAddress) {
        return User.builder()
                .id(this.id)
                .socialId(this.socialId)
                .socialProvider(this.socialProvider)
                .email(this.email)
                .nickname(this.nickname)
                .role(this.role)
                .refreshToken(this.refreshToken)
                .workAddress(this.workAddress)
                .homeAddress(newHomeAddress)
                .profileImage(this.profileImage)
                .snsProfileImageUrl(this.snsProfileImageUrl)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
    public UserDTO toDTO() {
        return UserDTO.builder()
                .userId(id)
                .nickname(nickname)
                .email(email)
                .workAddress(workAddress)
                .homeAddress(homeAddress)
                .socialProvider(socialProvider)
                .socialId(socialId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .role(role)
                .refreshToken(refreshToken)
                .snsProfileImageUrl(snsProfileImageUrl)
                .fileDTO(profileImage != null ? profileImage.toDTO() : null)
                .build();
    }
}
