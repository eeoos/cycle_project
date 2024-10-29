package capstone.cycle.post.entity;

import capstone.cycle.file.entity.FileGroup;
import capstone.cycle.like.entity.Like;
import capstone.cycle.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
//@BatchSize(size = 100)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "content_image_group_id")
    private FileGroup contentImageGroup;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
//    @BatchSize(size = 100)
    public List<Like> likes = new ArrayList<>();

//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
//    public List<Comment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;



    // 정적 팩토리 메서드
    public static Post createPost(String title, String content, User author, PostCategory category) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .viewCount(0L)
                .build();
    }

    // 포스트 업데이트를 위한 메서드
    public Post updateContent(String newTitle, String newContent, PostCategory newCategory) {
        return Post.builder()
                .id(this.id)
                .title(newTitle)
                .content(newContent)
                .author(this.author)
                .contentImageGroup(this.contentImageGroup)
                .category(newCategory)
                .viewCount(this.viewCount)
                .likes(this.likes)
                .createdAt(this.createdAt)
                .build();
    }

    // 조회수 증가를 위한 메서드
    public Post incrementViewCount() {
        return Post.builder()
                .id(this.id)
                .title(this.title)
                .content(this.content)
                .author(this.author)
                .contentImageGroup(this.contentImageGroup)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .viewCount(this.viewCount + 1)
                .likes(this.likes)
                .category(this.category)
                .build();
    }

    // FileGroup 설정을 위한 메서드
    public Post withContentImageGroup(FileGroup fileGroup) {
        return Post.builder()
                .id(this.id)
                .title(this.title)
                .content(this.content)
                .author(this.author)
                .contentImageGroup(fileGroup)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .viewCount(this.viewCount)
                .likes(this.likes)
                .category(this.category)
                .build();
    }

    // 좋아요 관련 메서드들
    public Post addLike(Like like) {
        List<Like> newLikes = new ArrayList<>(this.likes);
        newLikes.add(like);
        return toBuilder().likes(newLikes).build();
    }

    public Post removeLike(Like like) {
        List<Like> newLikes = new ArrayList<>(this.likes);
        newLikes.remove(like);

        return toBuilder().likes(newLikes).build();
    }

    public int getLikeCount() {
        return this.likes.size();
    }

    // 불변 리스트 반환
    public List<Like> getLikes() {
        return Collections.unmodifiableList(likes);
    }


    // Builder 패턴 재사용을 위한 메서드
    private Post.PostBuilder toBuilder() {
        return Post.builder()
                .id(this.id)
                .title(this.title)
                .content(this.content)
                .author(this.author)
                .contentImageGroup(this.contentImageGroup)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .viewCount(this.viewCount)
                .category(this.category);
    }
}
