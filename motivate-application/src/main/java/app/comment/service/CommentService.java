package app.comment.service;

import app.comment.model.Comment;
import app.comment.repository.CommentRepository;
import app.post.model.Post;
import app.post.repository.PostRepository;
import app.user.model.User;
import app.web.dto.CommentRequest;
import app.web.dto.PostRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public Comment createComment(CommentRequest commentRequest, User user) {

        return commentRepository.save(initializeComment(commentRequest, user));

    }

    private Comment initializeComment(CommentRequest commentRequest, User owner) {

        Post post = postRepository.getByOwner(owner);

        return Comment.builder()
                .owner(owner)
                .username(owner.getUsername())
                .post(post)
                .content(commentRequest.getContent())
                .createdAt(LocalDateTime.now())
                .likeCount(0)
                .build();


    }

    public List<Comment> getAllCommentsByPost(User owner) {
        Post post = postRepository.getByOwner(owner);

        List<Comment> commentsByPost = commentRepository.findByPost(post);

        return commentsByPost;
    }


//    public Post addComment(UUID postId, String username, String content) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new RuntimeException("Post not found"));
//        Comment comment = Comment.builder()
//                .owner(post.getOwner())
//                .username(username)
//                .post(post)
//                .profilePicture(post.getProfilePicture())
//                .content(content)
//                .createdAt(LocalDateTime.now())
//                .likeCount(0)
//                .build();
//        post.addComment(comment);
//        post.setCommentCount(post.getCommentCount() + 1); // Increment comment count
//        return postRepository.save(post);
//    }

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
