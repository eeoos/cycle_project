package capstone.cycle.common.security.filter;

import capstone.cycle.common.security.service.JwtUtil;
import capstone.cycle.common.security.service.SecurityService;
import capstone.cycle.user.dto.UserDTO;
import capstone.cycle.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SecurityService securityService;
    private final UserService userService;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.extractTokenFromHeader(request);

        if (accessToken != null) {
            log.info("Processing request with access token");
            if (jwtUtil.validateAccessToken(accessToken)) {
                log.info("Access token is valid");
                securityService.saveUserInSecurityContext(accessToken);
            } else {
                log.warn("Access token is invalid or expired. Attempting to use refresh token.");
                // AccessToken이 만료된 경우, 사용자 정보를 통해 RefreshToken을 확인하고 새로운 AccessToken 발급
                try {
                    UserDTO userDTO = jwtUtil.extractUserFromExpiredToken(accessToken);
                    String refreshToken = userService.getRefreshTokenForUser(userDTO.getUserId());

                    if (refreshToken != null && jwtUtil.validateRefreshToken(refreshToken)) {
                        log.info("Refresh token is valid. Generating new access token.");
                        String newAccessToken = jwtUtil.generateAccessToken(userDTO);
                        response.setHeader("Authorization", "Bearer " + newAccessToken);
                        securityService.saveUserInSecurityContext(newAccessToken);
                    } else {
                        log.warn("Refresh token is invalid or not found.");
                    }
                } catch (Exception e) {
                    log.error("Error processing tokens", e);
                    // 토큰에서 사용자 정보를 추출할 수 없는 경우 처리
                    SecurityContextHolder.clearContext();
                }
            }
        } else {
            log.info("Request without access token");
        }

        filterChain.doFilter(request, response);
//        try {
//            if (checkAccessTokenValid(request)) {
//                filterChain.doFilter(request, response);
//            }
//        } catch (Exception e) {
//            SecurityContextHolder.clearContext();
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
//        }
    }

    private boolean checkAccessTokenValid(HttpServletRequest request) {
        String accessToken = jwtUtil.extractTokenFromHeader(request);
        if (!jwtUtil.validateAccessToken(accessToken)) {
            securityService.saveUserInSecurityContext(accessToken);
            return false;
        }
        return false;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {
                "/api-docs/json",
                "/api-docs",
                "/api/u/v1/social-login",
                "/api/u/v1/token",
                "/swagger-ui/",
                "/swagger-config",
                "/error",
                "/api/n/v1/",
                "/api/u/v1/books/best-seller",
                "/api/u/v1/book/",
                "/v3/api-docs/**",
                /* swagger v2 */
                "/v2/api-docs",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**",
                /* swagger v3 */
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/v3/api-docs/swagger-config",
                "/v3/api-docs/common",
                "/v3/api-docs/user",
                "/v3/api-docs/post",
                "/v3/api-docs/comment",
        };
        String path = request.getRequestURI();
        boolean shouldNotFilter = Arrays.stream(excludePath).anyMatch(path::startsWith);

        return shouldNotFilter;
    }
}
