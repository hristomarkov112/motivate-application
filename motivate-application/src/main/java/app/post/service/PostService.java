package app.post.service;

import app.post.model.Post;
import app.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }
//    public List<Post> getAllPosts() {
//        return postRepository.findAll();
//    }
//
//    public Optional<Post> getPostById(UUID id) {
//        return postRepository.findById(id);
//    }
//
//    public List<Post> getPostsByUserId(UUID userId) {
//        return postRepository.findByUserId(userId);
//    }
//
//    public void deletePost(UUID id) {
//        postRepository.deleteById(id);
//    }
}
