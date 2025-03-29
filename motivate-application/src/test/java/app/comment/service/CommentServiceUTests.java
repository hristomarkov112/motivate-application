package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.post.model.Post;
import app.user.model.User;
import app.web.dto.CommentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceUTests {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private UUID commentId;
    private UUID postId;
    private User user;
    private Post post;
    private CommentRequest commentRequest;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        postId = UUID.randomUUID();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john_doe");

        post = new Post();
        post.setId(postId);

        commentRequest = new CommentRequest();
        commentRequest.setContent("This is a test comment.");

        comment = Comment.builder()
                .id(commentId)
                .owner(user)
                .username(user.getUsername())
                .post(post)
                .content(commentRequest.getContent())
                .createdAt(LocalDateTime.now())
                .likeCount(0)
                .build();
    }

    @Test
    void testCreateComment_Success() {

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.createComment(commentRequest, user, post);

        assertNotNull(result);
        assertEquals(comment.getContent(), result.getContent());
        assertEquals(user, result.getOwner());
        assertEquals(post, result.getPost());
        assertEquals(0, result.getLikeCount());

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testDeleteComment_Success() {

        commentService.deleteComment(commentId);

        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void testGetCommentsByPostId_Success() {

        List<Comment> comments = List.of(comment);
        when(commentRepository.findAllByPost_IdOrderByCreatedAtDesc(postId)).thenReturn(comments);

        List<Comment> result = commentService.getCommentsByPostId(postId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(comment, result.get(0));

        verify(commentRepository, times(1)).findAllByPost_IdOrderByCreatedAtDesc(postId);
    }
}
