package capstone.cycle.user.service;

import capstone.cycle.user.dto.*;
import capstone.cycle.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    public UserDTO socialLogin(SocialLoginDTO userCreateDTO); //O

    public DetailUserInfoDTO getUserDetailInfo(Long id);
    public User getUserInfo(String accessToken);


    public UserDTO getUserInfoByUsingRefreshToken(String refreshToken);


    public User findBySocialIdAndSocialProvider(String accessToken);

    public User findUserInSecurityContext();
    public UserInfosDTO getAllUserInfos(Long id);
    String getRefreshTokenForUser(Long userId);

    void refreshTokenIfNeeded(String refreshToken);

    void updateNickname(String nickname, Long id);

    void updateProfileImage(MultipartFile profileImage, Long id);

    void updateWorkAddress(UpdateAddressDTO updateAddressDTO, Long id);

    void updateHomeAddress(UpdateAddressDTO updateAddressDTO, Long id);

//    public UserInfosDTO getUserInfos(String accessToken);
}
