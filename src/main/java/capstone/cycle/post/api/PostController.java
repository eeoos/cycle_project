package capstone.cycle.post.api;

import capstone.cycle.common.security.dto.UserDetailsImpl;
import capstone.cycle.post.dto.PostCreateDTO;
import capstone.cycle.post.dto.PostResponseDTO;
import capstone.cycle.post.dto.PostUpdateDTO;
import capstone.cycle.post.dto.SimplePostResponseDTO;
import capstone.cycle.post.entity.PostCategory;
import capstone.cycle.post.service.PostService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/p/v1")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createPost(
            @RequestPart("postCreateDTO") /*@Parameter(schema =@Schema(type = "string", format = "binary"))*/ PostCreateDTO postCreateDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long createdPostId = postService.createPost(postCreateDTO, images, userDetails.getUser().getId());
        return ResponseEntity.ok(createdPostId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        PostResponseDTO post = postService.getPost(id, userDetails.getUser().getId());
        return ResponseEntity.ok(post);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long id,
            @RequestPart("postUpdateDTO") PostUpdateDTO postUpdateDTO,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {

        PostResponseDTO updatedPost = postService.updatePost(id, postUpdateDTO, newImages, userDetails.getUser().getId());
        return ResponseEntity.ok(updatedPost);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        postService.deletePost(id, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/like") //like toggle
    public ResponseEntity<PostResponseDTO> toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PostResponseDTO updatedPost = postService.toggleLike(id, userDetails.getUser().getId());
        return ResponseEntity.ok(updatedPost);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/category/{category}")
    public ResponseEntity<Slice<SimplePostResponseDTO>> getPostsByCategory(
            @PathVariable PostCategory category,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Slice<SimplePostResponseDTO> posts = postService.getPostsByCategory(
                category,
                lastPostId,
                userDetails.getUser().getId(),
                size
        );
        return ResponseEntity.ok(posts);
    }
}
