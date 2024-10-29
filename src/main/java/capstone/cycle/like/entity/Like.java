package capstone.cycle.like.entity;

import capstone.cycle.post.entity.Post;
import capstone.cycle.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 정적 팩토리 메서드
    public static Like createLike(Post post, User user) {
        return Like.builder()
                .post(post)
                .user(user)
                .build();
    }

    // 연관관계 변경을 위한 메서드
    public Like withPost(Post newPost) {
        return Like.builder()
                .id(this.id)
                .post(newPost)
                .user(this.user)
                .build();
    }

    public Like withUser(User newUser) {
        return Like.builder()
                .id(this.id)
                .post(this.post)
                .user(newUser)
                .build();
    }

}
