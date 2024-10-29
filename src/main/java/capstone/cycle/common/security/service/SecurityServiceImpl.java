package capstone.cycle.common.security.service;

import capstone.cycle.common.security.error.TokenErrorResult;
import capstone.cycle.common.security.error.TokenException;
import capstone.cycle.common.security.dto.UserDetailsImpl;
import capstone.cycle.user.dto.UserDTO;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService{

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @Override
    public void saveUserInSecurityContext(UserDTO userDTO) {
        String socialId = userDTO.getSocialId();
        String socialProvider = userDTO.getSocialProvider();
        saveUserInSecurityContext(socialId, socialProvider);
    }

    public void saveUserInSecurityContext(String accessToken) {
        String socialId = jwtUtil.extractSocialIdFromToken(accessToken);
        String socialProvider = jwtUtil.extractSocialProviderFromToken(accessToken);
        log.debug("Attempting to load user with socialId: {} and socialProvider: {}", socialId, socialProvider);
        saveUserInSecurityContext(socialId, socialProvider);
    }

    private void saveUserInSecurityContext(String socialId, String socialProvider) {
        UserDetails userDetails = loadUserBySocialIdAndSocialProvider(socialId, socialProvider);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        if(authentication != null) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
    }

    @Override
    public UserDTO getUserInfoSecurityContext() {
        UserDetails userDetails = getUserDetailsInSecurityContext();
        if(userDetails instanceof UserDetailsImpl) {
            User user = ((UserDetailsImpl) userDetails).getUser();
            return user.toDTO();
        }
        return null;
    }

    private UserDetails getUserDetailsInSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetails) authentication.getPrincipal();
    }


    private UserDetails loadUserBySocialIdAndSocialProvider(String socialId, String socialProvider) {
        log.debug("Loading user by socialId: {} and socialProvider: {}", socialId, socialProvider);
        User user = userRepository.findBySocialIdAndSocialProvider(socialId, socialProvider);

        if(user == null) {
            throw new TokenException(TokenErrorResult.TOKEN_EXPIRED);
        }

        return UserDetailsImpl.from(user);  // 정적 팩토리 메서드 사용

    }
}
