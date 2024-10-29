package capstone.cycle.like.service;

import capstone.cycle.like.dto.LikeStatus;
import capstone.cycle.like.entity.Like;
import capstone.cycle.like.error.LikeErrorResult;
import capstone.cycle.like.error.LikeException;
import capstone.cycle.like.repository.LikeRepository;
import capstone.cycle.post.entity.Post;
import capstone.cycle.post.error.PostErrorResult;
import capstone.cycle.post.error.PostException;
import capstone.cycle.post.repository.PostRepository;
import capstone.cycle.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    @Transactional
    public LikeStatus flipLike(Post post, User user) {
        if (post == null) {
            throw new LikeException(LikeErrorResult.POST_NOT_FOUND);
        }
        if (user == null) {
            throw new LikeException(LikeErrorResult.USER_NOT_FOUND);
        }

        boolean hasLiked = likeRepository.existsByPost_IdAndUser_Id(post.getId(), user.getId());

        if (hasLiked) {
            Like existingLike = likeRepository.findByPost_IdAndUser_Id(post.getId(), user.getId())
                    .orElseThrow(() -> new LikeException(LikeErrorResult.LIKE_NOT_FOUND));

            likeRepository.delete(existingLike);
            post.removeLike(existingLike);
            return LikeStatus.UNLIKE;
        } else {
            Like newLike = Like.createLike(post, user);
            likeRepository.save(newLike);
            post.addLike(newLike);
            return LikeStatus.LIKE;
        }
    }

    @Transactional(readOnly = true)
    public boolean hasUserLikedPost(Long postId, Long userId) {
        return likeRepository.existsByPost_IdAndUser_Id(postId, userId);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new LikeException(LikeErrorResult.POST_NOT_FOUND);
        }
        return likeRepository.countByPost_Id(postId);
    }

}
