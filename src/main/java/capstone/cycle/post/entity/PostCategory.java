package capstone.cycle.post.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {
    ALL("전체게시판", false),
    POPULAR("인기게시판", false),
    FREE_BOARD("자유게시판", true),
    QUESTION_BOARD("질문게시판", true),
    NOTICE("공지사항", true),
    CLUB_RECRUITMENT("동호회 모집게시판", true);

    private final String displayName;
    private final boolean selectable;
    public static boolean isValidForCreation(PostCategory category) {
        return category != null && category.isSelectable();
    }
}
