package app.web;

import app.comment.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerUTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @Test
    void showCommentForm_ShouldReturnCorrectViewAndModel() {

        String viewName = commentController.showCommentForm();

        assertEquals("/posts", viewName);
    }

    @Test
    void deleteComment_ShouldCallServiceAndRedirect() {

        UUID commentId = UUID.randomUUID();
        doNothing().when(commentService).deleteComment(commentId);

        String redirectUrl = commentController.deleteComment(commentId);

        assertEquals("redirect:/posts", redirectUrl);
        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    void deleteComment_ShouldVerifyServiceInteraction() {

        UUID commentId = UUID.randomUUID();

        commentController.deleteComment(commentId);

        verify(commentService).deleteComment(commentId);
    }

    @Test
    void showCommentForm_ShouldReturnModelAndView() {

        String result = commentController.showCommentForm();

        assertEquals("/posts", result);
    }

    @Test
    void deleteComment_ShouldHandleServiceException() {

        UUID commentId = UUID.randomUUID();
        doThrow(new RuntimeException("Delete failed")).when(commentService).deleteComment(commentId);

        assertThrows(RuntimeException.class, () -> {
            commentController.deleteComment(commentId);
        });
    }
}
