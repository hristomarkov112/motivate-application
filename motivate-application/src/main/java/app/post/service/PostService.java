package app.post.service;

import app.comment.model.Comment;
import app.exception.DomainException;
import app.post.model.Post;
import app.post.repository.PostRepository;
import app.user.model.User;
import app.web.dto.PostRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this post");
        }

        postRepository.deletePostById(postId);
    }

    public String formatPostContent(String content) {
        if (content == null) return "";

        // Convert newlines to <br> for better formatting
        content = content.replace("\n", "<br>");

        // Regex for detecting URLs
        String urlRegex = "(https?://[^\\s]+)";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(content);

        StringBuffer formattedContent = new StringBuffer();

        while (matcher.find()) {
            String url = matcher.group(1);

            // Check if the URL is an image
            if (url.matches(".*\\.(png|jpg|jpeg|gif)$")) {
                matcher.appendReplacement(formattedContent, "<img src='" + url + "' alt='Image' style='max-width:100%; border-radius:10px;'>");
            }
            // Check if the URL is a YouTube video (embedded player)
            else if (url.contains("youtube.com") || url.contains("youtu.be")) {
                String videoEmbed = "<iframe width='560' height='315' src='" + convertYoutubeLink(url) + "' frameborder='0' allowfullscreen></iframe>";
                matcher.appendReplacement(formattedContent, videoEmbed);
            }
            // Check if it's a direct MP4 video link
            else if (url.matches(".*\\.(mp4|webm|ogg)$")) {
                matcher.appendReplacement(formattedContent, "<video controls style='max-width:100%; border-radius:10px;'><source src='" + url + "' type='video/mp4'>Your browser does not support the video tag.</video>");
            }
            // Otherwise, make it a clickable hyperlink
            else {
                matcher.appendReplacement(formattedContent, "<a href='" + url + "' target='_blank'>" + url + "</a>");
            }
        }

        matcher.appendTail(formattedContent);
        return formattedContent.toString();
    }

    // Converts YouTube URLs into embeddable format
    private String convertYoutubeLink(String url) {
        return url.replace("watch?v=", "embed/");
    }


//    public List<Post> getAllPosts() {
//        return postRepository.findAllOrderByCreatedAt();
//    }
//
//    public Optional<Post> getPostById(UUID id) {
//        return postRepository.findById(id);
//    }

}
