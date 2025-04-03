package app.post.service;

import app.comment.model.Comment;
import app.exception.PostHasNoOwnerException;
import app.exception.PostMustNotBeEmpty;
import app.exception.PostNotFoundException;
import app.post.model.Post;
import app.post.repository.PostRepository;
import app.user.model.User;
import app.web.dto.PostRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        if (postRequest == null) {
            throw new IllegalArgumentException("PostRequest must not be null");
        }

        if (postRequest.getContent() == null || postRequest.getContent().trim().isEmpty()) {
            throw new PostMustNotBeEmpty("Post content must not be empty");
        }

        if (postRequest.getContent().length() > 4000) {
            throw new PostMustNotBeEmpty("Post content exceeds maximum length");
        }

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
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Post getById(UUID postId) {

        if (postId == null) {
            throw new IllegalArgumentException("Post ID must not be null");
        }

        return postRepository.findPostById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));
    }

    public Post addLike(UUID postId) {

        if (postId == null) {
            throw new IllegalArgumentException("Post ID must not be null");
        }

        Post post = getById(postId);

        try {
            post.setLikeCount(Math.addExact(post.getLikeCount(), 1));
        } catch (ArithmeticException e) {
            throw new ArithmeticException("Like count overflow - cannot exceed " + Integer.MAX_VALUE);
        }

        return postRepository.save(post);
    }

    @Transactional
    public void addComment(UUID id, Comment comment) {

        if (id == null) {
            throw new IllegalArgumentException("Post ID must not be null");
        }
        if (comment == null) {
            throw new IllegalArgumentException("Comment must not be null");
        }

        Post post = postRepository.findPostById(id)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (post.getComments() == null) {
            post.setComments(new ArrayList<>());
        }

        post.getComments().add(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {

        if (postId == null) {
            throw new IllegalArgumentException("Post ID must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        if (post.getOwner() == null) {
            throw new PostHasNoOwnerException("Post has no owner");
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
}
