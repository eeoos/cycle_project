package capstone.cycle.user.service;

import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.common.security.service.JwtUtil;
import capstone.cycle.common.security.service.SecurityService;
import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.file.entity.File;
import capstone.cycle.file.service.FileService;
import capstone.cycle.user.dto.*;
import capstone.cycle.user.entity.Address;
import capstone.cycle.user.entity.User;
import capstone.cycle.common.security.role.UserRole;
import capstone.cycle.user.error.UserErrorResult;
import capstone.cycle.user.error.UserException;
import capstone.cycle.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final FileService fileService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserDTO socialLogin(SocialLoginDTO socialLoginDTO) {
        User user = userRepository.findBySocialIdAndSocialProvider(
                socialLoginDTO.getSocialId(), socialLoginDTO.getSocialProvider());

        if (user == null) {
            user = createNewUser(socialLoginDTO);
        }

        return user.toDTO();
    }

    private User createNewUser(SocialLoginDTO dto) {
        User newUser = User.createUser(
                dto.getSocialId(),
                dto.getSocialProvider(),
                dto.getEmail(),
                dto.getNickname(),
                UserRole.USER.getRole(),
                dto.getSnsProfileImageUrl()
        );

        return userRepository.save(newUser);
    }

    /*private User updateExistingUser(User user, SocialLoginDTO dto) {
        // 자체 프로필 이미지가 없는 경우 sns 프로필 이미지 업데이트
        if (user.getProfileImage() == null) {
            user.setSnsProfileImageUrl(dto.getSnsProfileImageUrl());
        }
        // 다른 필요한 정보 업데이트
        return userRepository.save(user);
    }
*/

    @Override
    public User getUserInfo(String accessToken) {
        checkTokenExpired(accessToken);
        return findBySocialIdAndSocialProvider(accessToken);
    }

    private void checkTokenExpired(String accessToken) {
        if (jwtUtil.checkTokenExpired(accessToken)) {
            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
        }
    }

    @Override
    public UserDTO getUserInfoByUsingRefreshToken(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken);
        return user.toDTO();
    }

    public User findUserBySocialIdAndSocialProvider(SocialLoginDTO userDTO) {
        String socialId = userDTO.getSocialId();
        String socialProvider = userDTO.getSocialProvider();
        User user = userRepository.findBySocialIdAndSocialProvider(socialId, socialProvider);

        if (user != null) {
            return user;
        } else {
            return null;
        }
    }

    @Override
    public User findBySocialIdAndSocialProvider(String accessToken) {
        String socialId = jwtUtil.extractSocialIdFromToken(accessToken);
        String socialProvider = jwtUtil.extractSocialProviderFromToken(accessToken);

        User user = userRepository.findBySocialIdAndSocialProvider(socialId, socialProvider);
        if (user == null) {
            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
        }
        return user;
    }

    @Override
    public User findUserInSecurityContext() {
        UserDTO userDTO = securityService.getUserInfoSecurityContext();
        return userRepository.findById(userDTO.getUserId())
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));
    }

    private User readUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));
    }

    @Override
    @Transactional
    public DetailUserInfoDTO getUserDetailInfo(Long id) {
        User user = readUser(id);

        if (user.getWorkAddress() == null) {
            user = user.withWorkAddress(Address.createDefaultAddress());
        }
        if (user.getHomeAddress() == null) {
            user = user.withHomeAddress(Address.createDefaultAddress());
        }

        return DetailUserInfoDTO.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfosDTO getAllUserInfos(Long id) {
        if (!"ROLE_ADMIN".equals(readUser(id).getRole())) {
            throw new AccessDeniedException("관리자만 접근 가능합니다.");
        }

        List<User> allUsers = userRepository.findAll();
        List<SimpleUserInfoDTO> userInfos = allUsers.stream()
                .map(SimpleUserInfoDTO::from)
                .collect(Collectors.toList());
        return new UserInfosDTO(userInfos);
    }

    @Override
    public String getRefreshTokenForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));
        return user.getRefreshToken();
    }

    @Override
    @Transactional
    public void refreshTokenIfNeeded(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken);
        if (user != null) {
            LocalDateTime tokenCreationTime = jwtUtil.extractTokenCreationTime(refreshToken);
            if (tokenCreationTime.plusDays(7).isBefore(LocalDateTime.now())) {
                log.info("Refreshing token for user: {}", user.getId());
                String newRefreshToken = jwtUtil.generateRefreshToken(user.toDTO());
                User updatedUser = user.withRefreshToken(newRefreshToken);
                userRepository.save(updatedUser);
            }
        } else {
            log.warn("Attempted to refresh token for non-existent user");
        }
    }

    @Override
    @Transactional
    public void updateProfileImage(MultipartFile imageFile, Long userId) {
        User user = readUser(userId);

        // 기존 프로필 이미지가 있다면 삭제
        if (user.getProfileImage() != null) {
            fileService.deleteFile(user.getProfileImage().getId());
        }

        // 새 프로필 이미지 업로드
        String contentName = "profile_" + userId;
        FileDTO newProfileImageDTO = fileService.uploadFile(imageFile, contentName);
        File newProfileImage = fileService.getFile(newProfileImageDTO.getId());

        // 새로운 User 인스턴스 생성 및 저장
        User updatedUser = user.withProfileImage(newProfileImage);
        userRepository.save(updatedUser);
    }

    @Override
    @Transactional
    public void updateNickname(String nickname, Long id) {
        User user = readUser(id);
        User updatedUser = user.withNickname(nickname);
        userRepository.save(updatedUser);
    }

    @Override
    @Transactional
    public void updateWorkAddress(UpdateAddressDTO updateAddressDTO, Long id) {
        User user = readUser(id);

        Address newWorkAddress = new Address(
                updateAddressDTO.getRoadAddress(),
                updateAddressDTO.getBuildingNumber(),
                updateAddressDTO.getDetailAddress(),
                updateAddressDTO.getZipCode()
        );

        User updatedUser = user.withWorkAddress(newWorkAddress);
        userRepository.save(updatedUser);
    }

    @Override
    @Transactional
    public void updateHomeAddress(UpdateAddressDTO updateAddressDTO, Long id) {
        User user = readUser(id);

        Address newHomeAddress = new Address(
                updateAddressDTO.getRoadAddress(),
                updateAddressDTO.getBuildingNumber(),
                updateAddressDTO.getDetailAddress(),
                updateAddressDTO.getZipCode()
        );

        User updatedUser = user.withHomeAddress(newHomeAddress);
        userRepository.save(updatedUser);
    }
}
