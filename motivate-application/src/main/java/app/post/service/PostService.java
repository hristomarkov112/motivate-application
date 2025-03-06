package app.post.service;

import app.exception.DomainException;
import app.post.model.Post;
import app.post.repository.PostRepository;
import app.user.model.User;
import app.web.dto.PostRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
        return postRepository.findAll(Sort.by("createdAt"));
    }

    public Post likePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikeCount(post.getLikeCount() + 1);
        return postRepository.save(post);
    }

    public Post getByOwner(User owner) {
        return postRepository.getByOwner(owner);
    }

    public Post getById(UUID postId) {
        return postRepository.findById(postId).orElseThrow(() -> new DomainException("Post with id %s has not been found".formatted(postId)));
    }

    public void addLike(UUID postId) {
        Post post = getById(postId);
        post.setLikeCount(post.getLikeCount() + 1);
    }

//    public void addComment(UUID postId, String username, String content) {
//        Post post = postRepository.getById(postId);
//
//        Comment comment = Comment.builder()
//                .owner(post.getOwner())
//                .username(username)
//                .post(post)
//                .profilePicture(post.getProfilePicture())
//                .content(content)
//                .createdAt(LocalDateTime.now())
//                .likeCount(0)
//                .build();
//        post.getComments().add(comment);
//        post.setCommentCount(post.getCommentCount() + 1); // Increment comment count
//        return postRepository.save(post);
//    }

//    public List<Post> getAllPosts() {
//        return postRepository.findAllOrderByCreatedAt();
//    }
//
//    public Optional<Post> getPostById(UUID id) {
//        return postRepository.findById(id);
//    }
//
//    public void deletePost(UUID id) {
//        postRepository.deleteById(id);
//    }
}
