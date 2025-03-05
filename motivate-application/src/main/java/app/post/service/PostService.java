package app.post.service;

import app.exception.DomainException;
import app.post.model.Post;
import app.post.repository.PostRepository;
import app.user.model.User;
import app.user.repository.UserRepository;

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
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
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
