package capstone.cycle.post.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorResult {
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "필수값이 누락되거나 부적절한 값이 반환되었습니다."),
    POST_NOT_EXIST(HttpStatus.BAD_REQUEST, "존재하지 않는 게시글입니다."),
    UNAUTHORIZED_MODIFICATION(HttpStatus.FORBIDDEN, "게시글을 수정할 권한이 없습니다."),
    UNAUTHORIZED_DELETION(HttpStatus.FORBIDDEN, "게시글을 삭제할 권한이 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 이미지를 찾을 수 없습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다."),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 중 오류가 발생했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}
