package capstone.cycle.user.repository;

import capstone.cycle.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.socialId = :socialId AND u.socialProvider = :socialProvider")
    User findBySocialIdAndSocialProvider(@Param("socialId") String socialId, @Param("socialProvider") String socialProvider);

    public User findByNickname(String nickname);

    public List<User> findByNicknameContaining(String nickname);

    public User findByRefreshToken(String refreshToken);

    public void deleteById(Long userId);

    public User findBySocialId(String socialId);

    /*Long save(UserInfo userInfo);

    User findByEmail(String email);


    User findById(Long userId);

    void update( domain);

    void delete(Long userId);*/


}
