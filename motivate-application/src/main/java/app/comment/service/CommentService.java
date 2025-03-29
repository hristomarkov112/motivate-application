package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.post.model.Post;
import app.post.service.PostService;
import app.user.model.User;
import app.web.dto.CommentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment createComment(CommentRequest commentRequest, User user, Post post) {


        return commentRepository.save(initializeComment(commentRequest, user, post));

    }

    private Comment initializeComment(CommentRequest commentRequest, User owner, Post post) {

        return Comment.builder()
                .owner(owner)
                .username(owner.getUsername())
                .post(post)
                .content(commentRequest.getContent())
                .createdAt(LocalDateTime.now())
                .likeCount(0)
                .build();
    }

    public void deleteComment(UUID id) {

        commentRepository.deleteById(id);
    }

    public List<Comment> getCommentsByPostId(UUID postId) {
        return commentRepository.findAllByPost_IdOrderByCreatedAtDesc(postId);
    }
}
