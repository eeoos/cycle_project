package capstone.cycle.post.repository;

import capstone.cycle.post.entity.Post;
import capstone.cycle.post.entity.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {


    // 단일 게시글 상세 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.id = :id")
    Optional<Post> findById(@Param("id") Long id);

    // 카테고리별 게시글 목록 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.category = :category")
    Page<Post> findByCategory(@Param("category") PostCategory category, Pageable pageable);

    // 전체 게시글 목록 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes")
    Page<Post> findAll(Pageable pageable);

    // 인기 게시글 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN p.likes l " +
            "GROUP BY p " +
            "HAVING COUNT(l) >= :likeThreshold " +
            "ORDER BY COUNT(l) DESC")
    Page<Post> findPopularPosts(@Param("likeThreshold") long likeThreshold, Pageable pageable);




    // 전체 게시글 다음 페이지 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.createdAt < " +
            "(SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findAllForInfiniteScroll(
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    // 카테고리별 첫 페이지 조회

    // 카테고리별 다음 페이지 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.category = :category " +
            "AND p.createdAt < " +
            "(SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findByCategoryForInfiniteScroll(
            @Param("category") PostCategory category,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    // 인기 게시글 첫 페이지 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN p.likes l " +
            "GROUP BY p " +
            "HAVING COUNT(l) >= :likeThreshold " +
            "ORDER BY SIZE(p.likes) DESC, p.createdAt DESC")
    Slice<Post> findFirstPagePopularPosts(
            @Param("likeThreshold") long likeThreshold,
            Pageable pageable);

    // 인기 게시글 다음 페이지 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN p.likes l " +
            "WHERE p.createdAt < " +
            "(SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "GROUP BY p " +
            "HAVING COUNT(l) >= :likeThreshold " +
            "ORDER BY SIZE(p.likes) DESC, p.createdAt DESC")
    Slice<Post> findPopularPostsForInfiniteScroll(
            @Param("likeThreshold") long likeThreshold,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    // 게시글 삭제를 위한 조회
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithImages(@Param("id") Long id);

    // 게시글 수정을 위한 조회
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.author " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    // 특정 사용자의 게시글 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE a.id = :userId " +
            "AND p.createdAt < " +
            "(SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findByUserIdForInfiniteScroll(
            @Param("userId") Long userId,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    // 검색 기능
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) " +
            "AND p.createdAt < " +
            "(SELECT sub.createdAt FROM Post sub WHERE sub.id = :lastPostId) " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> searchPostsForInfiniteScroll(
            @Param("keyword") String keyword,
            @Param("lastPostId") Long lastPostId,
            Pageable pageable);

    // 조회수 증가
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    @Modifying
    void incrementViewCount(@Param("id") Long id);

    // 좋아요 정보만 조회
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithLikes(@Param("id") Long id);

    // 전체 게시글 첫 페이지 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findFirstPage(Pageable pageable);

    // 카테고리별 첫 페이지 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.author a " +
            "LEFT JOIN FETCH a.profileImage " +
            "LEFT JOIN FETCH p.contentImageGroup " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.category = :category " +
            "ORDER BY p.createdAt DESC")
    Slice<Post> findFirstPageByCategory(
            @Param("category") PostCategory category,
            Pageable pageable);
    /////////

    /*@Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.author.profileImage")
    List<Post> findAllWithAuthor();

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.author.profileImage WHERE p.category = :category")
    List<Post> findByCategoryWithAuthor(@Param("category") PostCategory category);

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.author.profileImage LEFT JOIN p.likes l GROUP BY p HAVING COUNT(l) >= :likeThreshold ORDER BY p.createdAt DESC")
    List<Post> findPopularPosts(@Param("likeThreshold") long likeThreshold);*/
}
