package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment createComment(Comment comment) {


        return commentRepository.save(comment);
    }


//    public List<Comment> getAllComments() {
//        return commentRepository.findAll();
//    }
//
//    public Optional<Comment> getCommentById(UUID id) {
//        return commentRepository.findById(id);
//    }
//
//    public List<Comment> getCommentsByPostId(UUID postId) {
//        return commentRepository.findByPostId(postId);
//    }
//
//    public void deleteComment(UUID id) {
//        commentRepository.deleteById(id);
//    }
}
