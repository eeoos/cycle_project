package capstone.cycle.comment.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorResult {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_MODIFICATION(HttpStatus.FORBIDDEN, "댓글을 수정할 권한이 없습니다."),
    UNAUTHORIZED_DELETION(HttpStatus.FORBIDDEN, "댓글을 삭제할 권한이 없습니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "존재하지 않는 부모 댓글입니다."),
    NESTED_REPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "대댓글에는 답글을 달 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 게시글을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "댓글 내용이 유효하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
