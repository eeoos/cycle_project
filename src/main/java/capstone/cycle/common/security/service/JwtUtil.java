package capstone.cycle.common.security.service;

import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.user.dto.UserDTO;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtUtil {

    private final UserRepository userRepository;

    @Value("${jwt.secret-key}")
    private String secretKeyPlain;

    private Key secretKey;

    private static final long ACCESS_TOKEN_VALIDITY = 30 * 60 * 1000; // 30 minutes
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days

    @PostConstruct
    void init() {
        byte[] keyBytes = secretKeyPlain.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDTO userDTO) {
        log.info("Generating access token for user: {}", userDTO.getUserId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDTO.getSocialId());
        claims.put("socialProvider", userDTO.getSocialProvider());
        claims.put("role", userDTO.getRole());  // role 정보 추가
        return createToken(claims, userDTO.getSocialId(), ACCESS_TOKEN_VALIDITY);
    }

    public String generateRefreshToken(UserDTO userDTO) {
        Map<String, Object> claims = Map.of("sub", userDTO.getSocialId());
        return createToken(claims, userDTO.getSocialId(), REFRESH_TOKEN_VALIDITY);
    }

    private String createToken(Map<String, Object> claims, String subject, long validity) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extracting claim from token");
        try {
            final Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            T result = claimsResolver.apply(claims);
            log.debug("Successfully extracted claim from token");
            return result;
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired while extracting claim");
            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
        } catch (Exception e) {
            log.error("Error extracting claim from token", e);
            throw new TokenException(TokenErrorResult.INVALID_TOKEN);
        }
    }
    public String extractSocialIdFromToken(String token) {
        String socialId = extractClaim(token, Claims::getSubject);
        log.debug("Extracted socialId from token: {}", socialId);
        return socialId;
    }

    public String extractSocialProviderFromToken(String token) {
        String socialProvider = extractClaim(token, claims -> claims.get("socialProvider", String.class));
        log.debug("Extracted socialProvider from token: {}", socialProvider);
        return socialProvider;
    }

    public boolean validateAccessToken(String accessToken) {
        log.debug("Validating access token");
        if (accessToken == null || accessToken.isEmpty()) {
            log.warn("Access token is null or empty");
            throw new TokenException(TokenErrorResult.ACCESS_TOKEN_NEED);
        }

        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(accessToken);
            log.info("Access token is valid");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Access token has expired");
            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
        } catch (Exception e) {
            log.error("Error validating access token", e);
            return false;
        }
    }

    public Boolean validateRefreshToken(String refreshToken) {
        log.debug("Validating refresh token");
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Refresh token is null or empty");
            throw new TokenException(TokenErrorResult.REFRESH_TOKEN_NEED);
        }

        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(refreshToken);
            log.info("Refresh token is valid");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Refresh token has expired");
            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
        } catch (Exception e) {
            log.error("Error validating refresh token", e);
            return false;
        }
    }

    public boolean checkTokenExpired(String token) {
        log.debug("Checking if token is expired");
        try {
            Date expirationDate = extractClaim(token, Claims::getExpiration);
            boolean isTokenExpired = expirationDate.before(new Date());
            log.debug(isTokenExpired ? "Token is expired" : "Token is not expired");
            return isTokenExpired;
        } catch (TokenException e) {
            log.warn("Token exception while checking expiration", e);
            return true;  // TokenException이 발생하면 토큰이 만료된 것으로 간주
        }
    }

    public Map<String, String> initToken(UserDTO saveOrFindUser) {
        Map<String, String> tokenMap = new HashMap<>();
        String accessToken = generateAccessToken(saveOrFindUser);
        String refreshToken = generateRefreshToken(saveOrFindUser);

        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);

        updRefreshTokenInDB(refreshToken, saveOrFindUser);

        return tokenMap;
    }

    public Map<String, String> refreshingAccessToken(UserDTO userDTO, String refreshToken) {
        Map<String, String> tokenMap = new HashMap<>();
        String accessToken = generateAccessToken(userDTO);

        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);

        return tokenMap;
    }

    private void updRefreshTokenInDB(String refreshToken, UserDTO savedOrFindUser) {
        UserDTO updatedUserDTO = savedOrFindUser.withRefreshToken(refreshToken);
        userRepository.save(updatedUserDTO.toEntity());
    }

    public String extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        } else {
            throw new TokenException(TokenErrorResult.ACCESS_TOKEN_NEED);
        }
    }

    public UserDTO extractUserFromExpiredToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            log.info("Successfully extracted user info from token");
            return createUserDTOFromClaims(claims);
        } catch (ExpiredJwtException e) {
            log.info("Extracted user info from expired token");
            // 만료된 토큰에서도 Claims를 추출할 수 있음
            return createUserDTOFromClaims(e.getClaims());
        } catch (Exception e) {
            log.error("Error extracting user from token", e);
            throw e;
        }
    }

    public LocalDateTime extractTokenCreationTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 'iat' 클레임에서 생성 시간을 추출
            Long issuedAt = claims.getIssuedAt().getTime();

            // Unix 타임스탬프를 LocalDateTime으로 변환
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(issuedAt), ZoneId.systemDefault());
        } catch (Exception e) {
            // 토큰 파싱 중 오류 발생 시 처리
            throw new RuntimeException("Failed to extract token creation time", e);
        }
    }

    private UserDTO createUserDTOFromClaims(Claims claims) {
        Long userId = Long.parseLong(claims.get("userId", String.class));
        String socialId = claims.getSubject();
        String socialProvider = claims.get("socialProvider", String.class);

        return new UserDTO(userId, socialId, socialProvider);
    }
}
