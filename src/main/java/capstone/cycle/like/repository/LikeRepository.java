package capstone.cycle.like.repository;

import capstone.cycle.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    // 특정 게시글의 특정 사용자의 좋아요 찾기
    Optional<Like> findByPost_IdAndUser_Id(Long postId, Long userId);

    // 특정 게시글의 특정 사용자의 좋아요 존재 여부 확인
    boolean existsByPost_IdAndUser_Id(Long postId, Long userId);

    // 특정 게시글의 좋아요 수 카운트
    long countByPost_Id(Long postId);

    // 특정 게시글의 특정 사용자의 좋아요 삭제
    void deleteByPost_IdAndUser_Id(Long postId, Long userId);
}
