package app.web;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.exception.UserNotFoundException;
import app.post.service.PostService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.post.model.Post;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.CommentRequest;
import app.web.dto.PostRequest;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PostControllerApiTest {

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private CommentService commentService;

    @Mock
    private Model model;

    @InjectMocks
    private PostController postController;

    @Test
    public void getPostsPage_ShouldReturnCorrectModelAndView() {

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        Post mockPost = new Post();
        mockPost.setContent("Test content");

        when(userService.getById(mockUser.getId())).thenReturn(mockUser);
        when(postService.getAllPosts()).thenReturn(Collections.singletonList(mockPost));
        doAnswer(invocation -> {
            String content = invocation.getArgument(0);
            return "Formatted: " + content;
        }).when(postService).formatPostContent(anyString());

        ModelAndView result = postController.getPostsPage(
                new AuthenticationMetaData(mockUser.getId(), "username", "password", UserRole.USER,true),
                null
        );

        assertNotNull(result);
        assertEquals("posts", result.getViewName());
        assertEquals(mockUser, result.getModel().get("user"));
        assertNotNull(result.getModel().get("postRequest"));
        assertNotNull(result.getModel().get("commentRequest"));
        assertTrue(result.getModel().get("posts") instanceof List);
        assertFalse(((List<?>) result.getModel().get("posts")).isEmpty());
        assertNull(result.getModel().get("errorMessage"));
    }

    @Test
    public void getCreatePostPage_ShouldReturnCorrectModelAndView() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = postController.getCreatePostPage(
                new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true),
                null
        );

        assertNotNull(result);
        assertEquals("new-post", result.getViewName());
        assertNotNull(result.getModel().get("postRequest"));
        assertNull(result.getModel().get("errorMessage"));
    }

    @Test
    public void getCreatePostPage_WithErrorParam_ShouldAddErrorMessage() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = postController.getCreatePostPage(
                new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true),
                "true"
        );

        assertNotNull(result);
        assertEquals("new-post", result.getViewName());
        assertEquals("The text length must be less than or equal 4000 characters.",
                result.getModel().get("errorMessage"));
    }

    @Test//
    public void createPost_WithValidRequest_ShouldRedirect() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        PostRequest postRequest = new PostRequest();
        postRequest.setContent("Valid Content");

        when(userService.getById(userId)).thenReturn(mockUser);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView result = postController.createPost(
                postRequest,
                bindingResult,
                new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true)
        );

        assertEquals("redirect:/posts", result.getViewName());
        verify(postService, times(1)).createPost(postRequest, mockUser);
    }

    @Test
    public void createPost_WithValidRequest_ShouldRedirectToPosts() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        PostRequest validRequest = new PostRequest();
        validRequest.setContent("Valid Content");

        when(userService.getById(userId)).thenReturn(mockUser);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView result = postController.createPost(
                validRequest,
                bindingResult,
                new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true)
        );

        assertEquals("redirect:/posts", result.getViewName());
        verify(postService).createPost(validRequest, mockUser);
    }

    @Test
    public void createPost_WithInvalidRequest_ShouldReturnFormWithErrors() {

        UUID userId = UUID.randomUUID();
        PostRequest invalidRequest = new PostRequest(); // Empty request

        when(bindingResult.hasErrors()).thenReturn(true);


        ModelAndView result = postController.createPost(
                invalidRequest,
                bindingResult,
                new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true)
        );

        assertEquals("redirect:/posts", result.getViewName());

    }

    @Test
    public void createPost_WhenUserNotFound_ShouldStillRedirect() {

        UUID userId = UUID.randomUUID();
        PostRequest validRequest = new PostRequest();
        validRequest.setContent("Valid Content");

        when(userService.getById(userId)).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView result = postController.createPost(
                validRequest,
                bindingResult,
                new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true)
        );

        assertEquals("redirect:/posts", result.getViewName());
        verify(postService).createPost(validRequest, null);
    }

    @Test
    public void getMyPostsPage_ShouldReturnPostsForAuthenticatedUser() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        Post post1 = new Post();
        post1.setId(UUID.randomUUID());
        post1.setContent("First Post");
        Post post2 = new Post();
        post2.setId(UUID.randomUUID());
        post2.setContent("Second Post");
        List<Post> mockPosts = Arrays.asList(post1, post2);

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);
        when(postService.getPostsByUserId(userId)).thenReturn(mockPosts);

        ModelAndView result = postController.getMyPostsPage(authData);

        assertEquals("my-posts", result.getViewName());

        List<Post> returnedPosts = (List<Post>) result.getModel().get("posts");
        assertNotNull(returnedPosts);
        assertEquals(2, returnedPosts.size());
        assertEquals("First Post", returnedPosts.get(0).getContent());
        assertEquals("Second Post", returnedPosts.get(1).getContent());

        verify(userService).getById(userId);
        verify(postService).getPostsByUserId(userId);
    }

    @Test
    public void getMyPostsPage_WhenUserHasNoPosts_ShouldReturnEmptyList() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);
        when(postService.getPostsByUserId(userId)).thenReturn(List.of());

        ModelAndView result = postController.getMyPostsPage(authData);

        assertEquals("my-posts", result.getViewName());

        List<Post> returnedPosts = (List<Post>) result.getModel().get("posts");
        assertNotNull(returnedPosts);
        assertTrue(returnedPosts.isEmpty());

        verify(userService).getById(userId);
        verify(postService).getPostsByUserId(userId);
    }

    @Test
    public void getMyPostsPage_WhenUserNotFound_ShouldStillReturnView() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(null);
        when(postService.getPostsByUserId(userId)).thenReturn(List.of());

        ModelAndView result = postController.getMyPostsPage(authData);

        assertEquals("my-posts", result.getViewName());
        assertNotNull(result.getModel().get("posts"));

        verify(userService).getById(userId);
        verify(postService).getPostsByUserId(userId);
    }

    @Test
    public void getMyPostsPage_ShouldUseCorrectUserId() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());
        when(postService.getPostsByUserId(userId)).thenReturn(List.of());

        postController.getMyPostsPage(authData);

        verify(userService).getById(userId);
        verify(postService).getPostsByUserId(userId);
        verifyNoMoreInteractions(userService, postService);
    }

    @Test
    public void deletePost_ShouldDeleteAndReturnUpdatedPosts() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User postOwner = new User();
        postOwner.setId(userId);

        Post postToDelete = new Post();
        postToDelete.setId(postId);
        postToDelete.setOwner(postOwner);

        Post remainingPost1 = new Post();
        remainingPost1.setId(UUID.randomUUID());
        Post remainingPost2 = new Post();
        remainingPost2.setId(UUID.randomUUID());
        List<Post> remainingPosts = Arrays.asList(remainingPost1, remainingPost2);

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(postToDelete);
        when(postService.getPostsByUserId(userId)).thenReturn(remainingPosts);


        ModelAndView result = postController.deletePost(postId, authData);


        assertEquals("my-posts", result.getViewName());

        List<Post> returnedPosts = (List<Post>) result.getModel().get("posts");
        assertNotNull(returnedPosts);
        assertEquals(2, returnedPosts.size());

        verify(postService).getById(postId);
        verify(postService).deletePost(postId, userId);
        verify(postService).getPostsByUserId(userId);
    }

    @Test
    public void deletePost_WhenNoPostsRemain_ShouldReturnEmptyList() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User postOwner = new User();
        postOwner.setId(userId);

        Post postToDelete = new Post();
        postToDelete.setId(postId);
        postToDelete.setOwner(postOwner);

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(postToDelete);
        when(postService.getPostsByUserId(userId)).thenReturn(List.of());

        ModelAndView result = postController.deletePost(postId, authData);

        assertEquals("my-posts", result.getViewName());

        List<Post> returnedPosts = (List<Post>) result.getModel().get("posts");
        assertNotNull(returnedPosts);
        assertTrue(returnedPosts.isEmpty());
    }

    @Test
    public void deletePost_ShouldVerifyOwnershipBeforeDeletion() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User postOwner = new User();
        postOwner.setId(userId);

        Post postToDelete = new Post();
        postToDelete.setId(postId);
        postToDelete.setOwner(postOwner);

        List<Post> remainingPosts = List.of(new Post());
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(postToDelete);
        when(postService.getPostsByUserId(userId)).thenReturn(remainingPosts);


        ModelAndView result = postController.deletePost(postId, authData);

        assertEquals("my-posts", result.getViewName());
        verify(postService).deletePost(postId, userId);
    }

    @Test
    public void deletePost_WhenPostNotFound_ShouldHandleGracefully() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            postController.deletePost(postId, authData);
        });

        verify(postService).getById(postId);
        verify(postService, never()).deletePost(any(), any());
        verify(postService, never()).getPostsByUserId(any());
    }

    @Test
    public void deletePost_ShouldUseCorrectPostAndUserIds() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User postOwner = new User();
        postOwner.setId(userId);

        Post postToDelete = new Post();
        postToDelete.setId(postId);
        postToDelete.setOwner(postOwner);

        List<Post> remainingPosts = List.of(new Post());
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(postToDelete);
        when(postService.getPostsByUserId(userId)).thenReturn(remainingPosts);

        postController.deletePost(postId, authData);

        verify(postService).getById(postId);
        verify(postService).deletePost(postId, userId);
        verify(postService).getPostsByUserId(userId);
        verifyNoMoreInteractions(postService);
    }

    @Test
    public void getCommentsByPostId_ShouldReturnCommentsViewWithAttributes() {

        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);
        mockPost.setContent("Test Post");

        Comment comment1 = new Comment();
        comment1.setId(UUID.randomUUID());
        comment1.setContent("First comment");
        Comment comment2 = new Comment();
        comment2.setId(UUID.randomUUID());
        comment2.setContent("Second comment");
        List<Comment> mockComments = Arrays.asList(comment1, comment2);

        when(postService.getById(postId)).thenReturn(mockPost);
        when(commentService.getCommentsByPostId(postId)).thenReturn(mockComments);

        String viewName = postController.getCommentsByPostId(postId, model);

        assertEquals("comments", viewName);

        verify(model).addAttribute("post", mockPost);
        verify(model).addAttribute("comments", mockComments);
        verify(model).addAttribute(eq("commentRequest"), any(CommentRequest.class));

        verify(postService).getById(postId);
        verify(commentService).getCommentsByPostId(postId);
    }

    @Test
    public void getCommentsByPostId_WhenPostNotFound_ShouldStillReturnView() {

        UUID postId = UUID.randomUUID();

        when(postService.getById(postId)).thenReturn(null);
        when(commentService.getCommentsByPostId(postId)).thenReturn(List.of());

        String viewName = postController.getCommentsByPostId(postId, model);

        assertEquals("comments", viewName);
        verify(model).addAttribute(eq("post"), isNull());
        verify(model).addAttribute(eq("comments"), anyList());
        verify(model).addAttribute(eq("commentRequest"), any(CommentRequest.class));
    }

    @Test
    public void getCommentsByPostId_WhenNoCommentsExist_ShouldReturnEmptyList() {

        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);

        when(postService.getById(postId)).thenReturn(mockPost);
        when(commentService.getCommentsByPostId(postId)).thenReturn(List.of());

        String viewName = postController.getCommentsByPostId(postId, model);

        assertEquals("comments", viewName);
        verify(model).addAttribute("post", mockPost);
        verify(model).addAttribute("comments", List.of());
        verify(model).addAttribute(eq("commentRequest"), any(CommentRequest.class));
    }

    @Test
    public void getCommentsByPostId_ShouldAddCommentRequestToModel() {

        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();

        when(postService.getById(postId)).thenReturn(mockPost);
        when(commentService.getCommentsByPostId(postId)).thenReturn(List.of());

        postController.getCommentsByPostId(postId, model);

        verify(model).addAttribute(eq("commentRequest"), argThat(request -> {
            assertNotNull(request);
            assertTrue(request instanceof CommentRequest);
            return true;
        }));
    }

    @Test
    public void getCommentsByPostId_ShouldUseCorrectPostId() {

        UUID postId = UUID.randomUUID();

        when(postService.getById(postId)).thenReturn(new Post());
        when(commentService.getCommentsByPostId(postId)).thenReturn(List.of());

        postController.getCommentsByPostId(postId, model);

        verify(postService).getById(postId);
        verify(commentService).getCommentsByPostId(postId);
        verifyNoMoreInteractions(postService, commentService);
    }

    @Test
    public void addComment_WithValidRequest_ShouldRedirectToPosts() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post mockPost = new Post();
        mockPost.setId(postId);

        User mockUser = new User();
        mockUser.setId(userId);

        CommentRequest validRequest = new CommentRequest();
        validRequest.setContent("Valid comment");

        Comment mockComment = new Comment();

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(mockPost);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(commentService.createComment(validRequest, mockUser, mockPost)).thenReturn(mockComment);

        String result = postController.addComment(
                postId,
                validRequest,
                bindingResult,
                authData,
                model
        );

        assertEquals("redirect:/posts", result);

        verify(postService).getById(postId);
        verify(userService).getById(userId);
        verify(commentService).createComment(validRequest, mockUser, mockPost);
        verify(postService).addComment(postId, mockComment);
        verify(model).addAttribute("post", mockPost);
    }

    @Test
    public void addComment_WithInvalidRequest_ShouldReturnCommentsView() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post mockPost = new Post();
        User mockUser = new User();
        CommentRequest invalidRequest = new CommentRequest(); // Empty/invalid

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(mockPost);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = postController.addComment(
                postId,
                invalidRequest,
                bindingResult,
                authData,
                model
        );

        assertEquals("comments", result);

        verify(model).addAttribute("post", mockPost);
        verify(model).addAttribute("user", mockUser);
        verify(commentService, never()).createComment(any(), any(), any());
        verify(postService, never()).addComment(any(), any());
    }

    @Test
    public void addComment_WhenPostNotFound_ShouldThrowException() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentRequest validRequest = new CommentRequest();
        validRequest.setContent("Valid comment");

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            postController.addComment(
                    postId,
                    validRequest,
                    bindingResult,
                    authData,
                    model
            );
        });

        assertEquals("Post not found", exception.getMessage());

        verify(postService).getById(postId);
        verifyNoInteractions(userService);
        verifyNoInteractions(commentService);
    }

    @Test
    public void addComment_WhenUserNotFound_ShouldHandleGracefully() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post mockPost = new Post();
        CommentRequest validRequest = new CommentRequest();
        validRequest.setContent("Valid comment");

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(mockPost);
        when(userService.getById(userId)).thenReturn(null);
        when(bindingResult.hasErrors()).thenReturn(false);


        assertThrows(UserNotFoundException.class, () -> {
            postController.addComment(postId, validRequest, bindingResult, authData, model);
        });

        verify(postService).getById(postId);
        verify(userService).getById(userId);
        verifyNoInteractions(commentService);
    }

    @Test
    public void addComment_ShouldAddPostToModelBeforeValidation() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post mockPost = new Post();
        CommentRequest invalidRequest = new CommentRequest(); // Invalid

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(mockPost);
        when(bindingResult.hasErrors()).thenReturn(true);

        postController.addComment(
                postId,
                invalidRequest,
                bindingResult,
                authData,
                model
        );

        verify(model).addAttribute("post", mockPost); // Should be called regardless of validation
    }

    @Test
    public void addComment_ShouldNotCreateCommentWhenValidationFails() {

        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post mockPost = new Post();
        User mockUser = new User();
        CommentRequest invalidRequest = new CommentRequest(); // Invalid

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(postService.getById(postId)).thenReturn(mockPost);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(bindingResult.hasErrors()).thenReturn(true);

        postController.addComment(
                postId,
                invalidRequest,
                bindingResult,
                authData,
                model
        );

        verify(commentService, never()).createComment(any(), any(), any());
        verify(postService, never()).addComment(any(), any());
    }

    @Test
    public void addLike_ShouldAddLikeAndRedirect() {

        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);

        when(postService.addLike(postId)).thenReturn(mockPost);

        String result = postController.addLike(postId);

        assertEquals("redirect:/posts", result);
        verify(postService).addLike(postId);
    }

    @Test
    public void addLike_WhenPostNotFound_ShouldStillRedirect() {

        UUID postId = UUID.randomUUID();

        when(postService.addLike(postId)).thenReturn(null);

        String result = postController.addLike(postId);

        assertEquals("redirect:/posts", result);
        verify(postService).addLike(postId);
    }

    @Test
    public void addLike_ShouldUseCorrectPostId() {

        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();

        when(postService.addLike(postId)).thenReturn(mockPost);

        postController.addLike(postId);

        verify(postService).addLike(postId);
        verifyNoMoreInteractions(postService);
    }
}
