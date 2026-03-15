package com.myblog.backend.controller;

import com.myblog.backend.model.dto.request.CreateCommentRequest;
import com.myblog.backend.model.dto.request.CreatePostRequest;
import com.myblog.backend.model.dto.request.UpdatePostRequest;
import com.myblog.backend.model.dto.response.CommentResponse;
import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import com.myblog.backend.service.CommentService;
import com.myblog.backend.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping
    public PostsPageResponse getPosts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        return postService.getPosts(search, pageNumber, pageSize);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostPreviewResponse createPost(@RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable long id) {
        postService.deletePost(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostPreviewResponse> updatePost(
            @PathVariable long id,
            @RequestBody UpdatePostRequest request
    ) {
        return postService.updatePost(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Integer> addLike(@PathVariable long id, HttpServletRequest request) {
        int likesCount = postService.addLike(id, resolveUserKey(request));
        return ResponseEntity.ok(likesCount);
    }

    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Integer> removeLike(@PathVariable long id, HttpServletRequest request) {
        int likesCount = postService.removeLike(id, resolveUserKey(request));
        return ResponseEntity.ok(likesCount);
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Integer> getLikesCount(@PathVariable long id) {
        int likesCount = postService.getLikesCount(id);
        return ResponseEntity.ok(likesCount);
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) {
        if (!postService.exists(id)) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = postService.getImage(id);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(bytes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostPreviewResponse> getPostById(@PathVariable long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable long id) {
        return commentService.getByPostId(id);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable long id,
            @RequestBody CreateCommentRequest request
    ) {
        return commentService.add(id, request.getText());
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable long postId,
            @PathVariable long commentId,
            @RequestBody CreateCommentRequest request
    ) {
        CommentResponse updated = commentService.update(postId, commentId, request.getText());
        return updated == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable long postId,
            @PathVariable long commentId
    ) {
        boolean deleted = commentService.delete(postId, commentId);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private String resolveUserKey(HttpServletRequest request) {
        String explicitUser = request.getHeader("X-User-Id");
        if (explicitUser != null && !explicitUser.isBlank()) {
            return explicitUser.trim();
        }
        String remoteAddr = request.getRemoteAddr() == null ? "unknown-ip" : request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent") == null ? "unknown-agent" : request.getHeader("User-Agent");
        return remoteAddr + "|" + userAgent;
    }

    @RequestMapping(value = "/{id}/image", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<String> uploadImage(
            @PathVariable long id,
            @RequestParam("image") MultipartFile image
    ) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body("empty file");
        }
        boolean updated = postService.updateImage(id, image);
        return updated
                ? ResponseEntity.status(HttpStatus.CREATED).body("ok")
                : ResponseEntity.notFound().build();
    }

}
