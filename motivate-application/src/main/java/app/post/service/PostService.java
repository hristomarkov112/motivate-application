package app.post.service;

import app.comment.model.Comment;
import app.exception.DomainException;
import app.post.model.Post;
import app.post.repository.PostRepository;
import app.user.model.User;
import app.web.dto.PostRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(PostRequest postRequest, User user) {

        Post post = Post.builder()
                .owner(user)
                .username(user.getUsername())
                .profilePicture(user.getProfilePictureUrl())
                .content(postRequest.getContent())
                .createdAt(LocalDateTime.now())
                .showCommentInput(true)
                .likeCount(0)
                .commentCount(0)
                .build();

        return postRepository.save(post);
    }

    public List<Post> getPostsByUserId(UUID ownerId) {
        return postRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public void likePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    public Post getByOwner(User owner) {
        return postRepository.getByOwner(owner);
    }

    public Post getById(UUID postId) {
        return postRepository.findPostById(postId).orElseThrow(() -> new DomainException("Post with id %s has not been found".formatted(postId)));
    }

    public Post addLike(UUID postId) {
        Post post = getById(postId);

        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        return post;
    }

    public void addComment(UUID id, Comment comment) {
        Post post = postRepository.findPostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.getComments().add(comment);
        postRepository.save(post);
    }

    public void deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this post");
        }

        postRepository.deletePostById(postId);
    }

//    public List<Post> getAllPosts() {
//        return postRepository.findAllOrderByCreatedAt();
//    }
//
//    public Optional<Post> getPostById(UUID id) {
//        return postRepository.findById(id);
//    }
//

}
