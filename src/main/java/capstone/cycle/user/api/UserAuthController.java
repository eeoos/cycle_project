package capstone.cycle.user.api;

import capstone.cycle.common.security.service.JwtUtil;
import capstone.cycle.common.security.service.SecurityService;
import capstone.cycle.user.dto.SocialLoginDTO;
import capstone.cycle.user.dto.UserDTO;
import capstone.cycle.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/u/v1")
public class UserAuthController {

    private final UserService userService;
    private final SecurityService securityService;
    private final JwtUtil jwtUtil;

    private UserAuthController(UserService userService, SecurityService securityService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.securityService = securityService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/social-login")
    public ResponseEntity<Map<String, String>> socialLogin(@RequestBody @Valid SocialLoginDTO socialLoginDTO) {
        UserDTO savedOrFindUser = userService.socialLogin(socialLoginDTO);
        securityService.saveUserInSecurityContext(savedOrFindUser);
        Map<String, String> tokenMap = jwtUtil.initToken(savedOrFindUser);

        return ResponseEntity.ok(tokenMap);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> refreshingAccessToken(HttpServletRequest request) {
        String refreshToken = jwtUtil.extractTokenFromHeader(request);
        jwtUtil.validateRefreshToken(refreshToken);

        // RefreshToken 갱신 로직 추가
        userService.refreshTokenIfNeeded(refreshToken);

        UserDTO userDTO = userService.getUserInfoByUsingRefreshToken(refreshToken);
        Map<String, String> tokenMap = jwtUtil.refreshingAccessToken(userDTO, refreshToken);
        return ResponseEntity.ok(tokenMap);
    }
}
