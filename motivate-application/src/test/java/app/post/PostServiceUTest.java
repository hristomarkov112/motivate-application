package app.post;

import app.comment.model.Comment;
import app.exception.DomainException;
import app.membership.model.Membership;
import app.post.model.Post;
import app.post.repository.PostRepository;
import app.post.service.PostService;
import app.user.model.Country;
import app.user.model.User;
import app.user.model.UserRole;
import app.wallet.model.Wallet;
import app.web.dto.PostRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;


import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceUTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_WithValidRequest_ReturnsSavedPost() {

        User user = User.builder()
                .username("gosho123")
                .build();

        PostRequest request = new PostRequest(user, "Test post content", LocalDateTime.now());

        Post expectedPost = Post.builder()
                .owner(user)
                .username(user.getUsername())
                .profilePicture(user.getProfilePictureUrl())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .showCommentInput(true)
                .likeCount(0)
                .commentCount(0)
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(expectedPost);

        Post result = postService.createPost(request, user);

        assertThat(result).isNotNull();
        assertThat(result.getOwner()).isEqualTo(user);
        assertThat(result.getContent()).isEqualTo(request.getContent());
        assertThat(result.getLikeCount()).isZero();
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_WithNullUser_ThrowsException() {
        User user = User.builder()
                .username("gosho123")
                .build();
        PostRequest request = new PostRequest(user, "Test post content", LocalDateTime.now());
        String expectedMessage = "User must not be null";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.createPost(request, null)
        );

        assertThat(exception.getMessage()).contains(expectedMessage);

        verifyNoInteractions(postRepository);
    }

    @Test
    void createPost_WithNullRequest_ThrowsException() {
        User user = User.builder()
                .username("gosho123")
                .build();

        assertThatThrownBy(() -> postService.createPost(null, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PostRequest must not be null");

        verifyNoInteractions(postRepository);
    }

    @Test
    void createPost_WithTooLongContent_ThrowsException() {
        User user = User.builder()
                .username("gosho123")
                .build();
        String longContent = "a".repeat(4000 + 1);
        PostRequest request = new PostRequest(user, longContent, LocalDateTime.now());

        assertThatThrownBy(() -> postService.createPost(request, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum length");
    }

    @Test
    void createPost_WithEmptyContent_ThrowsException() {

        User user = User.builder()
                .username("gosho123")
                .build();
        PostRequest emptyRequest = new PostRequest(user, "", LocalDateTime.now());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> postService.createPost(emptyRequest, user)
        );

        assertThat(exception.getMessage()).contains("Post content must not be empty");
    }

    @Test
    void getPostsByUserId_WithValidId_ReturnsPostsInCorrectOrder() {

        User user = User.builder()
                .username("gosho123")
                .build();
        UUID userId = user.getId();

        Post olderPost = Post.builder()
                .owner(user)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Post newerPost = Post.builder()
                .owner(user)
                .createdAt(LocalDateTime.now())
                .build();

        List<Post> mockPosts = Arrays.asList(newerPost, olderPost);

        when(postRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId))
                .thenReturn(mockPosts);

        List<Post> result = postService.getPostsByUserId(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(newerPost);
        assertThat(result.get(1)).isEqualTo(olderPost);
        verify(postRepository).findAllByOwnerIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getPostsByUserId_WithNoPosts_ReturnsEmptyList() {
        UUID userId = UUID.randomUUID();
        when(postRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.emptyList());

        List<Post> result = postService.getPostsByUserId(userId);

        assertThat(result).isEmpty();
        verify(postRepository).findAllByOwnerIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getAllPosts_ReturnsPostsInCorrectOrder() {

        User user1 = User.builder()
                .username("gosho123")
                .build();

        User user2 = User.builder()
                .username("tosho123")
                .build();

        Post post1 = Post.builder().owner(user1).createdAt(LocalDateTime.now().minusHours(1)).build();
        Post post2 = Post.builder().owner(user2).createdAt(LocalDateTime.now()).build();

        List<Post> mockPosts = Arrays.asList(post2, post1);
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(mockPosts);

        List<Post> result = postService.getAllPosts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(post2); // Newest first
        assertThat(result.get(1)).isEqualTo(post1);
        verify(postRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getAllPosts_WithNoPosts_ReturnsEmptyList() {
        when(postRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        List<Post> result = postService.getAllPosts();

        assertThat(result).isEmpty();
        verify(postRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getById_WithExistingPost_ReturnsPost() {

        UUID postId = UUID.randomUUID();
        Post expectedPost = Post.builder()
                .id(postId)
                .content("Test content")
                .build();

        when(postRepository.findPostById(postId)).thenReturn(Optional.of(expectedPost));

        Post result = postService.getById(postId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(postId);
        assertThat(result.getContent()).isEqualTo("Test content");
        verify(postRepository).findPostById(postId);
    }

    @Test
    void getById_WithNonExistingPost_ThrowsDomainException() {

        UUID nonExistingId = UUID.randomUUID();
        String expectedMessage = "Post with id " + nonExistingId + " has not been found";

        when(postRepository.findPostById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getById(nonExistingId))
                .isInstanceOf(DomainException.class)
                .hasMessage(expectedMessage);

        verify(postRepository).findPostById(nonExistingId);
    }

    @Test
    void getById_WithNullId_ThrowsIllegalArgumentException() {

        String expectedMessage = "Post ID must not be null";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.getById(null)
        );

        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void addLike_WithExistingPost_IncrementsLikeCount() {

        UUID postId = UUID.randomUUID();
        Post existingPost = Post.builder()
                .id(postId)
                .likeCount(5)
                .build();

        when(postRepository.findPostById(postId)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.addLike(postId);

        assertThat(result.getLikeCount()).isEqualTo(6);
        verify(postRepository).findPostById(postId);
        verify(postRepository).save(existingPost);
    }

    @Test
    void addLike_WithNonExistingPost_ThrowsDomainException() {
        UUID nonExistingId = UUID.randomUUID();
        when(postRepository.findPostById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.addLike(nonExistingId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Post with id " + nonExistingId + " has not been found");

        verify(postRepository, never()).save(any());
    }

    @Test
    void addLike_WithNullId_ThrowsIllegalArgumentException() {

        assertThatThrownBy(() -> postService.addLike(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post ID must not be null");

        verifyNoInteractions(postRepository);
    }

    @Test
    void getById_WithEmptyUUIDString_ThrowsException() {

        assertThatThrownBy(() -> postService.getById(UUID.fromString("00000000-0000-0000-0000-000000000000")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void getById_VerifyRepositoryCalledExactlyOnce() {

        UUID postId = UUID.randomUUID();
        when(postRepository.findPostById(postId)).thenReturn(Optional.of(new Post()));

        postService.getById(postId);

        verify(postRepository, times(1)).findPostById(postId);
    }

    @Test
    void addLike_WithMaxIntegerValue_HandlesOverflowGracefully() {

        UUID postId = UUID.randomUUID();
        Post existingPost = Post.builder()
                .id(postId)
                .likeCount(Integer.MAX_VALUE)
                .build();

        when(postRepository.findPostById(postId)).thenReturn(Optional.of(existingPost));

        assertThatThrownBy(() -> postService.addLike(postId))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Like count overflow - cannot exceed 2147483647");

        verify(postRepository).findPostById(postId);
        verify(postRepository, never()).save(any());
    }

    @Test
    void addComment_ToExistingPost_AddsCommentAndIncrementsCount() {
        User user = User.builder()
                .username("gosho123")
                .build();

        Post post = Post.builder()
                .content("Test content")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        UUID postId = UUID.randomUUID();
        Comment newComment = new Comment(user.getId(), user, user.getUsername(), post, "pic.jpg", "Test comment content", LocalDateTime.now(), 0);
        Post existingPost = Post.builder()
                .id(postId)
                .comments(new ArrayList<>())
                .commentCount(2)
                .build();

        when(postRepository.findPostById(postId)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(existingPost)).thenReturn(existingPost);

        postService.addComment(postId, newComment);

        assertThat(existingPost.getComments()).hasSize(1);
        assertThat(existingPost.getComments().get(0)).isEqualTo(newComment);
        assertThat(existingPost.getCommentCount()).isEqualTo(3);
        verify(postRepository).save(existingPost);
    }

    @Test
    void addComment_ToNonExistingPost_ThrowsException() {
        User user = User.builder()
                .username("gosho123")
                .build();

        Post post = Post.builder()
                .content("Test content")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        UUID nonExistingId = UUID.randomUUID();
        Comment  comment = new Comment(user.getId(), user, user.getUsername(), post, "pic.jpg", "Test comment content", LocalDateTime.now(), 0);
        when(postRepository.findPostById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.addComment(nonExistingId, comment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");

        verify(postRepository, never()).save(any());
    }

    @Test
    void addComment_WithNullPostId_ThrowsException() {
        User user = User.builder()
                .username("gosho123")
                .build();

        Post post = Post.builder()
                .content("Test content")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        Comment  validComment = new Comment(user.getId(), user, user.getUsername(), post, "pic.jpg", "Test comment content", LocalDateTime.now(), 0);

        String expectedMessage = "Post ID must not be null";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.addComment(null, validComment)
        );

        assertThat(exception.getMessage()).isEqualTo(expectedMessage);

        verifyNoInteractions(postRepository);
    }

    @Test
    void addComment_WithNullComment_ThrowsException() {
        UUID postId = UUID.randomUUID();

        assertThatThrownBy(() -> postService.addComment(postId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Comment must not be null");

        verifyNoInteractions(postRepository);
    }

    @Test
    void addComment_ToPostWithNullCommentsList_InitializesList() {

        User user = User.builder()
                .username("gosho123")
                .build();

        UUID postId = UUID.randomUUID();
        Post post = Post.builder()
                .id(postId)
                .comments(null)
                .commentCount(0)
                .build();
        Comment  comment = new Comment(user.getId(), user, user.getUsername(), post, "pic.jpg", "Test comment content", LocalDateTime.now(), 0);

        when(postRepository.findPostById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        postService.addComment(postId, comment);

        assertThat(post.getComments()).isNotNull().hasSize(1);
        assertThat(post.getCommentCount()).isEqualTo(1);
    }

    @Test
    void deletePost_WithValidOwner_DeletesPost() {
        // Arrange
        UUID postId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        List<Post> posts = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        List<Membership> memberships = new ArrayList<>();
        List<Wallet> wallets = new ArrayList<>();
        User owner = new User(ownerId, "gosho123", "Georgi", "Georgiev", "pic.jpg", "Bio",
                "mail@abv.bg",  "password", UserRole.USER, Country.BULGARIA, true, LocalDateTime.now(), LocalDateTime.now(), memberships, wallets, posts, comments);
        Post post = Post.builder()
                .id(postId)
                .owner(owner)
                .build();

        // Make sure to mock the repository call
        when(postRepository.findPostById(postId)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).deletePostById(postId);

        // Act
        postService.deletePost(postId, ownerId);

        // Assert
        verify(postRepository).findPostById(postId);
        verify(postRepository).deletePostById(postId);
    }

    @Test
    void deletePost_WithNonExistingPost_ThrowsException() {

        UUID nonExistingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(postRepository.findPostById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(nonExistingId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");

        verify(postRepository, never()).deletePostById(any());
    }

    @Test
    void deletePost_WithUnauthorizedUser_ThrowsException() {

        UUID postId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User owner = User.builder()
                .id(ownerId)
                .username("gosho123")
                .build();
        Post post = Post.builder()
                .id(postId)
                .owner(owner)
                .build();

        when(postRepository.findPostById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(postId, otherUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You are not authorized to delete this post");

        verify(postRepository, never()).deletePostById(any());
    }

    @Test
    void deletePost_WithNullPostId_ThrowsException() {

        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> postService.deletePost(null, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Post ID must not be null");

        verifyNoInteractions(postRepository);
    }

    @Test
    void deletePost_WithNullUserId_ThrowsException() {

        UUID postId = UUID.randomUUID();

        assertThatThrownBy(() -> postService.deletePost(postId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID must not be null");

        verifyNoInteractions(postRepository);
    }

    @Test
    void deletePost_WithNullPostOwner_ThrowsException() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Post post = Post.builder()
                .id(postId)
                .owner(null)
                .build();

        when(postRepository.findPostById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(postId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post has no owner");

        verify(postRepository, never()).deletePostById(any());
    }

    @Test
    void testFormatPostContent_WithNullContent() {
        String formatted = postService.formatPostContent(null);
        assertEquals("", formatted);
    }

    @Test
    void testFormatPostContent_WithNewLines() {
        String content = "Hello\nWorld!";
        String formatted = postService.formatPostContent(content);
        assertEquals("Hello<br>World!", formatted);
    }

    @Test
    void testFormatPostContent_WithHyperlink() {
        String content = "Check this out: https://example.com";
        String formatted = postService.formatPostContent(content);
        assertEquals("Check this out: <a href='https://example.com' target='_blank'>https://example.com</a>", formatted);
    }

    @Test
    void testFormatPostContent_WithImageUrl() {
        String content = "Look at this image: https://example.com/image.jpg";
        String formatted = postService.formatPostContent(content);
        assertEquals("Look at this image: <img src='https://example.com/image.jpg' alt='Image' style='max-width:100%; border-radius:10px;'>", formatted);
    }

    @Test
    void testFormatPostContent_WithYouTubeUrl() {
        String content = "Watch this video: https://www.youtube.com/watch?v=abc123";
        String formatted = postService.formatPostContent(content);
        assertEquals("Watch this video: <iframe width='560' height='315' src='https://www.youtube.com/embed/abc123' frameborder='0' allowfullscreen></iframe>", formatted);
    }

    @Test
    void testFormatPostContent_WithMp4Video() {
        String content = "Watch this: https://example.com/video.mp4";
        String formatted = postService.formatPostContent(content);
        assertEquals("Watch this: <video controls style='max-width:100%; border-radius:10px;'><source src='https://example.com/video.mp4' type='video/mp4'>Your browser does not support the video tag.</video>", formatted);
    }

    @Test
    void testFormatPostContent_WithMultipleLinks() {
        String content = "Check out https://site1.com and this image https://site2.com/image.png";
        String formatted = postService.formatPostContent(content);
        assertEquals("Check out <a href='https://site1.com' target='_blank'>https://site1.com</a> and this image <img src='https://site2.com/image.png' alt='Image' style='max-width:100%; border-radius:10px;'>", formatted);
    }
}
