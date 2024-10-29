package capstone.cycle.post.service;

import capstone.cycle.common.security.role.UserRole;
import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.file.dto.FileGroupDTO;
import capstone.cycle.file.entity.File;
import capstone.cycle.file.entity.FileGroup;
import capstone.cycle.file.error.FileErrorResult;
import capstone.cycle.file.error.FileException;
import capstone.cycle.file.repository.FileGroupRepository;
import capstone.cycle.file.repository.FileRepository;
import capstone.cycle.file.service.FileService;
import capstone.cycle.like.dto.LikeStatus;
import capstone.cycle.like.entity.Like;
import capstone.cycle.like.repository.LikeRepository;
import capstone.cycle.post.dto.PostCreateDTO;
import capstone.cycle.post.dto.PostResponseDTO;
import capstone.cycle.post.dto.PostUpdateDTO;
import capstone.cycle.post.dto.SimplePostResponseDTO;
import capstone.cycle.post.entity.Post;
import capstone.cycle.post.entity.PostCategory;
import capstone.cycle.post.error.PostErrorResult;
import capstone.cycle.post.error.PostException;
import capstone.cycle.post.repository.PostRepository;
import capstone.cycle.user.entity.User;
import capstone.cycle.user.error.UserErrorResult;
import capstone.cycle.user.error.UserException;
import capstone.cycle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FileService fileService;
    private final FileGroupRepository fileGroupRepository;

    private static final long POPULAR_POST_LIKE_THRESHOLD = 10;


    @Transactional
    public Long createPost(PostCreateDTO postCreateDTO, List<MultipartFile> images, Long userId) {
        validatePostCreation(postCreateDTO, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        Post post = Post.createPost(
                postCreateDTO.getTitle(),
                postCreateDTO.getContent(),
                user,
                postCreateDTO.getCategory()
        );

        if (images != null && !images.isEmpty()) {
            FileGroup fileGroup = handleImageUpload(images);
            post = post.withContentImageGroup(fileGroup);
        }

        return postRepository.save(post).getId();
    }

    @Transactional
    public PostResponseDTO getPost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostException(PostErrorResult.POST_NOT_EXIST));

        // FileGroup의 files 정보 조회 및 설정
        setFileGroupWithFiles(post);

        incrementViewCountAsync(post);

        LikeStatus likeStatus = getLikeStatus(post.getId(), userId);
        return new PostResponseDTO(post, likeStatus);
    }

    @Transactional(readOnly = true)
    public Slice<SimplePostResponseDTO> getPostsByCategory(
            PostCategory category,
            Long lastPostId,
            Long userId,
            int size
    ) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Post> posts = fetchPosts(category, lastPostId, pageable);

        // FileGroup의 files 정보 조회 및 설정
        setFileGroupsWithFiles(posts.getContent());

        return posts.map(post -> {
            boolean isLiked = likeRepository.existsByPost_IdAndUser_Id(post.getId(), userId);
            return SimplePostResponseDTO.fromPost(post, isLiked);
        });
    }

    @Transactional
    public PostResponseDTO updatePost(Long id, PostUpdateDTO postUpdateDTO,
                                      List<MultipartFile> newImages, Long userId) {
        Post post = postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new PostException(PostErrorResult.POST_NOT_EXIST));

        User user = userRepository.getReferenceById(userId);
        validateAuthorization(post, user, PostErrorResult.UNAUTHORIZED_MODIFICATION);

        Post updatedPost = updatePostContent(post, postUpdateDTO);
        updatedPost = updatePostImages(updatedPost, postUpdateDTO.getDeletedImageIds(), newImages);

        Post savedPost = postRepository.save(updatedPost);
        setFileGroupWithFiles(savedPost);

        return new PostResponseDTO(savedPost, getLikeStatus(savedPost.getId(), userId));
    }

    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findByIdWithImages(id)
                .orElseThrow(() -> new PostException(PostErrorResult.POST_NOT_EXIST));

        User user = userRepository.getReferenceById(userId);
        validateAuthorization(post, user, PostErrorResult.UNAUTHORIZED_DELETION);

        if (post.getContentImageGroup() != null) {
            fileService.deleteFileGroup(post.getContentImageGroup().getId());
        }

        postRepository.delete(post);
    }

    @Transactional
    public PostResponseDTO toggleLike(Long postId, Long userId) {
        Post post = postRepository.findByIdWithLikes(postId)
                .orElseThrow(() -> new PostException(PostErrorResult.POST_NOT_EXIST));

        User user = userRepository.getReferenceById(userId);

        // 현재 사용자의 좋아요 찾기
        Optional<Like> existingLike = post.getLikes().stream()
                .filter(like -> like.getUser().getId().equals(userId))
                .findFirst();

        Post updatedPost;
        LikeStatus likeStatus;

        if (existingLike.isPresent()) {
            // 좋아요 취소
            Like likeToRemove = existingLike.get();
            likeRepository.delete(likeToRemove);
            updatedPost = Post.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .author(post.getAuthor())
                    .contentImageGroup(post.getContentImageGroup())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .viewCount(post.getViewCount())
                    .category(post.getCategory())
                    .likes(post.getLikes().stream()
                            .filter(like -> !like.getId().equals(likeToRemove.getId()))
                            .collect(Collectors.toList()))
                    .build();
            likeStatus = LikeStatus.UNLIKE;
        } else {
            // 좋아요 추가
            Like newLike = Like.createLike(post, user);
            Like savedLike = likeRepository.save(newLike);
            List<Like> updatedLikes = new ArrayList<>(post.getLikes());
            updatedLikes.add(savedLike);
            updatedPost = Post.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .author(post.getAuthor())
                    .contentImageGroup(post.getContentImageGroup())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .viewCount(post.getViewCount())
                    .category(post.getCategory())
                    .likes(updatedLikes)
                    .build();
            likeStatus = LikeStatus.LIKE;
        }

        Post savedPost = postRepository.save(updatedPost);
        return new PostResponseDTO(savedPost, likeStatus);
    }

    private Slice<Post> fetchPosts(PostCategory category, Long lastPostId, Pageable pageable) {
        if (lastPostId == null) {
            return switch (category) {
                case ALL -> postRepository.findFirstPage(pageable);
                case POPULAR -> postRepository.findFirstPagePopularPosts(POPULAR_POST_LIKE_THRESHOLD, pageable);
                default -> postRepository.findFirstPageByCategory(category, pageable);
            };
        } else {
            return switch (category) {
                case ALL -> postRepository.findAllForInfiniteScroll(lastPostId, pageable);
                case POPULAR -> postRepository.findPopularPostsForInfiniteScroll(
                        POPULAR_POST_LIKE_THRESHOLD,
                        lastPostId,
                        pageable
                );
                default -> postRepository.findByCategoryForInfiniteScroll(category, lastPostId, pageable);
            };
        }
    }

    private void setFileGroupsWithFiles(List<Post> posts) {
        List<Long> fileGroupIds = posts.stream()
                .map(Post::getContentImageGroup)
                .filter(fg -> fg != null)
                .map(FileGroup::getId)
                .collect(Collectors.toList());

        if (!fileGroupIds.isEmpty()) {
            List<FileGroup> fileGroups = fileGroupRepository.findFileGroupsWithFiles(fileGroupIds);
            Map<Long, FileGroup> fileGroupMap = fileGroups.stream()
                    .collect(Collectors.toMap(FileGroup::getId, fg -> fg));

            posts.forEach(post -> {
                if (post.getContentImageGroup() != null) {
                    FileGroup fileGroup = fileGroupMap.get(post.getContentImageGroup().getId());
                    if (fileGroup != null) {
                        post.withContentImageGroup(fileGroup);
                    }
                }
            });
        }
    }
    private void setFileGroupWithFiles(Post post) {
        if (post.getContentImageGroup() != null) {
            fileGroupRepository.findFileGroupWithFiles(post.getContentImageGroup().getId())
                    .ifPresent(fileGroup -> post.withContentImageGroup(fileGroup));
        }
    }
    private void validatePostCreation(PostCreateDTO postCreateDTO, Long userId) {
        if (!PostCategory.isValidForCreation(postCreateDTO.getCategory())) {
            throw new PostException(PostErrorResult.INVALID_CATEGORY);
        }

        if (postCreateDTO.getCategory() == PostCategory.NOTICE) {
            User user = userRepository.getReferenceById(userId);
            if (!isAdmin(user)) {
                throw new PostException(PostErrorResult.UNAUTHORIZED_ACTION);
            }
        }
    }

    private FileGroup handleImageUpload(List<MultipartFile> images) {
        try {
            if (images == null || images.isEmpty()) {
                return null;
            }
            FileGroupDTO fileGroupDTO = fileService.uploadFiles(images, "post_");
            return fileService.getFileGroup(fileGroupDTO.getId());
        } catch (Exception e) {
            log.error("Failed to upload images", e);
            throw new PostException(PostErrorResult.FILE_UPLOAD_ERROR);
        }
    }

    @Async
    @Transactional
    protected void incrementViewCountAsync(Post post) {
        // 엔티티 전체를 업데이트하지 않고 조회수만 업데이트
        postRepository.incrementViewCount(post.getId());
    }


    private Post updatePostContent(Post post, PostUpdateDTO postUpdateDTO) {
        return post.updateContent(
                postUpdateDTO.getTitle(),
                postUpdateDTO.getContent(),
                postUpdateDTO.getCategory()
        );
    }

    private Post updatePostImages(Post post, List<Long> deletedImageIds, List<MultipartFile> newImages) {
        FileGroup currentFileGroup = post.getContentImageGroup();

        // 1. 삭제할 이미지 처리
        if (shouldDeleteImages(deletedImageIds)) {
            post = handleDeletedImages(post, deletedImageIds);
            currentFileGroup = post.getContentImageGroup(); // 삭제 후 현재 FileGroup 상태 갱신
        }

        // 2. 새로운 이미지 추가
        if (shouldAddNewImages(newImages)) {
            post = handleNewImages(post, newImages);
        }

        return post;
    }

    private boolean shouldDeleteImages(List<Long> deletedImageIds) {
        return deletedImageIds != null && !deletedImageIds.isEmpty();
    }

    private boolean shouldAddNewImages(List<MultipartFile> newImages) {
        return newImages != null && !newImages.isEmpty();
    }


    private Post handleDeletedImages(Post post, List<Long> deletedImageIds) {
        FileGroup currentFileGroup = post.getContentImageGroup();
        if (currentFileGroup == null) {
            return post;
        }

        // 삭제 대상이 아닌 이미지들만 필터링
        List<File> remainingFiles = currentFileGroup.getFiles().stream()
                .filter(file -> !deletedImageIds.contains(file.getId()))
                .collect(Collectors.toList());

        // 삭제할 이미지들 실제 삭제 처리
        deletedImageIds.forEach(fileService::deleteFile);

        // 남은 파일들로 FileGroup 업데이트
        FileGroup updatedFileGroup = currentFileGroup.withFiles(remainingFiles);
        updatedFileGroup = fileGroupRepository.save(updatedFileGroup);

        return post.withContentImageGroup(updatedFileGroup);
    }

    private Post handleNewImages(Post post, List<MultipartFile> newImages) {
        FileGroup currentFileGroup = post.getContentImageGroup();

        if (currentFileGroup == null) {
            // 기존 FileGroup이 없는 경우 새로 생성
            FileGroupDTO fileGroupDTO = fileService.uploadFiles(newImages, "post_");
            FileGroup newFileGroup = fileService.getFileGroup(fileGroupDTO.getId());
            return post.withContentImageGroup(newFileGroup);
        } else {
            // 기존 FileGroup이 있는 경우 파일 추가
            List<File> newFiles = uploadNewFiles(newImages);

            // 기존 파일들과 새 파일들 합치기
            List<File> allFiles = new ArrayList<>(currentFileGroup.getFiles());
            allFiles.addAll(newFiles);

            // FileGroup 업데이트
            FileGroup updatedFileGroup = currentFileGroup.withFiles(allFiles);
            updatedFileGroup = fileGroupRepository.save(updatedFileGroup);

            return post.withContentImageGroup(updatedFileGroup);
        }
    }

    private List<File> uploadNewFiles(List<MultipartFile> newImages) {
        return newImages.stream()
                .map(image -> {
                    try {
                        FileDTO fileDTO = fileService.uploadFile(image, "post_");
                        return fileService.getFile(fileDTO.getId());
                    } catch (Exception e) {
                        throw new PostException(PostErrorResult.FILE_UPLOAD_ERROR);
                    }
                })
                .collect(Collectors.toList());
    }

    private void validateAuthorization(Post post, User user, PostErrorResult errorResult) {
        // 공지사항 권한 체크
        if (post.getCategory() == PostCategory.NOTICE && !isAdmin(user)) {
            throw new PostException(PostErrorResult.UNAUTHORIZED_ACTION);
        }

        // 수정/삭제 권한 체크
        if (!isAuthorizedToModify(post, user)) {
            throw new PostException(errorResult);
        }
    }

    private boolean isAuthorizedToModify(Post post, User user) {
        return post.getAuthor().getId().equals(user.getId()) ||
                UserRole.ADMIN.getRole().equals(user.getRole());
    }

    private boolean isAdmin(User user) {
        return UserRole.ADMIN.getRole().equals(user.getRole());
    }

    private LikeStatus getLikeStatus(Long postId, Long userId) {
        return likeRepository.existsByPost_IdAndUser_Id(postId, userId)
                ? LikeStatus.LIKE : LikeStatus.UNLIKE;
    }

    /*private List<Post> getPopularPosts() {
        return postRepository.findPopularPosts(POPULAR_POST_LIKE_THRESHOLD);
    }*/

}
