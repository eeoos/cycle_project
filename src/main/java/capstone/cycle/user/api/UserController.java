package capstone.cycle.user.api;

import capstone.cycle.common.security.dto.UserDetailsImpl;
import capstone.cycle.user.dto.UpdateAddressDTO;
import capstone.cycle.user.dto.UpdateNicknameDTO;
import capstone.cycle.user.dto.DetailUserInfoDTO;
import capstone.cycle.user.dto.UserInfosDTO;
import capstone.cycle.user.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/u/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401",
                    description = "1. 엑세스 토큰이 없을 때 \t\n 2. 엑세스 토큰이 만료되었을 때 \t\n 3. 엑세스 토큰으로 유저를 찾을 수 없을 때",
                    content = @Content(schema = @Schema(example = "{\"code\" : \"401 UNAUTHORIZED\", \"message\" : \"message\"}"))),
            @ApiResponse(responseCode = "200",
                    description = "유저 정보 가져오기 성공",
                    content = @Content(schema = @Schema(implementation = DetailUserInfoDTO.class))),
    })
    public ResponseEntity<DetailUserInfoDTO> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        DetailUserInfoDTO detailUserInfoDTO = userService.getUserDetailInfo(userDetails.getUser().getId());
        return ResponseEntity.ok(detailUserInfoDTO);
    }

    // 프로필 이미지 수정
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping(value = "/profileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProfileImage(
            @RequestPart(value = "profileImage", required = true) MultipartFile profileImage,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.updateProfileImage(profileImage, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 닉네임 수정
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/nickname")
    public ResponseEntity<Void> updateNickname(
            @RequestBody @Valid UpdateNicknameDTO updateNicknameDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.updateNickname(updateNicknameDTO.getNickname(), userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }



    // 근무지 수정
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/workAddress")
    public ResponseEntity<Void> updateWorkAddress(
            @RequestBody @Valid UpdateAddressDTO updateAddressDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.updateWorkAddress(updateAddressDTO, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    // 거주지 수정
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/homeAddress")
    public ResponseEntity<Void> updateHomeAddress(
            @RequestBody @Valid UpdateAddressDTO updateAddressDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.updateHomeAddress(updateAddressDTO, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }


    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401",
                    description = "1. 엑세스 토큰이 없을 때 \t\n 2. 엑세스 토큰이 만료되었을 때 \t\n 3. 엑세스 토큰으로 유저를 찾을 수 없을 때",
                    content = @Content(schema = @Schema(example = "{\"code\" : \"401 UNAUTHORIZED\", \"message\" : \"message\"}"))),
            @ApiResponse(responseCode = "200",
                    description = "모든 유저 정보 가져오기 성공",
                    content = @Content(schema = @Schema(implementation = UserInfosDTO.class))),
    })
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfosDTO> getAllUserInfos(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        UserInfosDTO userInfosDTO = userService.getAllUserInfos(userDetails.getUser().getId());
        return ResponseEntity.ok(userInfosDTO);
    }




    /*@SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "2. 파라미터가 부적절한 값일 때",
                    content = @Content(schema = @Schema(example = "{\"code\" : \"400\", \"message\" : \"message\"}"))),
            @ApiResponse(responseCode = "200",
                    description = "유저 목록 가져오기 성공",
                    content = @Content(schema = @Schema(implementation = UserInfosDTO.class))),
    })
    @GetMapping("/users")
    public ResponseEntity<UserInfosDTO> getUserInfos(HttpServletRequest request) {
        String accessToken = jwtUtil.extractTokenFromHeader(request);
        UserInfosDTO userInfosDTO = userService.getUserInfos(accessToken);
        return ResponseEntity.ok(userInfosDTO);
    }*/
}
