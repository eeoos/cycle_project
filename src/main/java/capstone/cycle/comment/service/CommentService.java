package capstone.cycle.comment.service;

import capstone.cycle.comment.dto.CommentCreateDTO;
import capstone.cycle.comment.dto.CommentResponseDTO;
import capstone.cycle.comment.entity.Comment;
import capstone.cycle.comment.entity.CommentLike;
import capstone.cycle.comment.error.CommentErrorResult;
import capstone.cycle.comment.error.CommentException;
import capstone.cycle.comment.repository.CommentRepository;
import capstone.cycle.post.entity.Post;
import capstone.cycle.post.repository.PostRepository;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    @Transactional
    public CommentResponseDTO createComment(Long postId, Long userId, CommentCreateDTO commentCreateDTO) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CommentException(CommentErrorResult.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommentException(CommentErrorResult.USER_NOT_FOUND));

        if (commentCreateDTO.getContent() == null || commentCreateDTO.getContent().trim().isEmpty()) {
            throw new CommentException(CommentErrorResult.INVALID_COMMENT_CONTENT);
        }

        Comment comment;
        if (commentCreateDTO.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentCreateDTO.getParentId())
                    .orElseThrow(() -> new CommentException(CommentErrorResult.INVALID_PARENT_COMMENT));

            if (parentComment.isReply()) {
                throw new CommentException(CommentErrorResult.NESTED_REPLY_NOT_ALLOWED);
            }
            comment = Comment.createReply(commentCreateDTO.getContent(), post, user, parentComment);
        } else {
            comment = Comment.createComment(commentCreateDTO.getContent(), post, user);
        }

        Comment savedComment = commentRepository.save(comment);
        return CommentResponseDTO.from(savedComment, userId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByPostId(Long postId, Long currentUserId) {
        if (!postRepository.existsById(postId)) {
            throw new CommentException(CommentErrorResult.POST_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findByPostIdAndParentIsNull(postId);
        return comments.stream()
                .map(comment -> CommentResponseDTO.from(comment, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDTO updateComment(Long postId, Long commentId, CommentCreateDTO commentCreateDTO) {
        Comment comment = findCommentWithValidation(postId, commentId);

        if (commentCreateDTO.getContent() == null || commentCreateDTO.getContent().trim().isEmpty()) {
            throw new CommentException(CommentErrorResult.INVALID_COMMENT_CONTENT);
        }

        Comment updatedComment = comment.updateContent(commentCreateDTO.getContent());
        Comment savedComment = commentRepository.save(updatedComment);
        return CommentResponseDTO.from(savedComment, comment.getAuthor().getId());
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = findCommentWithValidation(postId, commentId);
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponseDTO toggleLike(Long postId, Long commentId, Long userId) {
        Comment comment = findCommentWithValidation(postId, commentId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommentException(CommentErrorResult.USER_NOT_FOUND));

        boolean hasLiked = comment.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(userId));

        Comment updatedComment;
        if (hasLiked) {
            CommentLike existingLike = comment.getLikes().stream()
                    .filter(like -> like.getUser().getId().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> new CommentException(CommentErrorResult.COMMENT_NOT_FOUND));
            updatedComment = comment.removeLike(existingLike);
        } else {
            CommentLike newLike = CommentLike.createCommentLike(comment, user);
            updatedComment = comment.addLike(newLike);
        }

        Comment savedComment = commentRepository.save(updatedComment);
        return CommentResponseDTO.from(savedComment, userId);
    }

    private Comment findCommentWithValidation(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorResult.COMMENT_NOT_FOUND));

        if (!comment.getPost().getId().equals(postId)) {
            throw new CommentException(CommentErrorResult.COMMENT_NOT_FOUND);
        }

        return comment;
    }
}
