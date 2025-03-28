package app.web;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.exception.PostNotFoundException;
import app.exception.UnauthorizedPostAccessException;
import app.exception.UserNotFoundException;
import app.post.model.Post;
import app.post.service.PostService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentRequest;
import app.web.dto.PostRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/posts")
public class PostController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    @Autowired
    public PostController(UserService userService, PostService postService, CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping()
    public ModelAndView getPostsPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData, @RequestParam(value = "error", required = false) String errorParam) {

        User user = userService.getById(authenticationMetaData.getId());

        //User
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("posts");
        modelAndView.addObject("user", user);

        //Posts
        List<Post> posts = postService.getAllPosts();
        modelAndView.addObject("postRequest", new PostRequest());
        modelAndView.addObject("posts", posts);

        posts.forEach(post -> post.setContent(postService.formatPostContent(post.getContent())));

        //Comments
        modelAndView.addObject("commentRequest", new CommentRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "The text length must be less than or equal 4000 characters.") ;
        }

        return modelAndView;
    }

    @GetMapping("/new")
    public ModelAndView getCreatePostPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData, @RequestParam(value = "error", required = false) String errorParam) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(authenticationMetaData.getId());

        modelAndView.setViewName("new-post");
        modelAndView.addObject("postRequest", new PostRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "The text length must be less than or equal 4000 characters.") ;
        }

        return modelAndView;
    }

    @PostMapping("/new")
    public ModelAndView createPost(@Valid PostRequest postRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(authenticationMetaData.getId());
        modelAndView.addObject("postRequest", postRequest);

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("new-post");
        }

        postService.createPost(postRequest, user);

        return new ModelAndView("redirect:/posts");
    }

    @GetMapping("/my-posts")
    public ModelAndView getMyPostsPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());
        List<Post> posts = postService.getPostsByUserId(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("my-posts");
        modelAndView.addObject("posts", posts);

        return modelAndView;
    }

    @DeleteMapping("/{postId}/delete")
    public ModelAndView deletePost(@PathVariable UUID postId, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        Post post = postService.getById(postId);

        if (!post.getOwner().getId().equals(authenticationMetaData.getId())) {
            throw new UnauthorizedPostAccessException("You are not authorized to delete this post");
        }

        postService.deletePost(postId, post.getOwner().getId());
        List<Post> posts = postService.getPostsByUserId(authenticationMetaData.getId());

        //My Posts
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("my-posts");
        modelAndView.addObject("posts", posts);

        return modelAndView;
    }

    @GetMapping("/{id}/comments")
    public String getCommentsByPostId(@PathVariable UUID id, Model model) {
        Post post = postService.getById(id);
        List<Comment> comments = commentService.getCommentsByPostId(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("comments");

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentRequest", new CommentRequest());

        return "comments";
    }

    @PostMapping("/{id}/comments")
    public String addComment(
            @PathVariable UUID id,
            @Valid @ModelAttribute("commentRequest") CommentRequest commentRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData,
            Model model) {

        Post post = postService.getById(id);
        if (post == null) {
            throw new PostNotFoundException("Post not found");
        }
        model.addAttribute("post", post);

        if (bindingResult.hasErrors()) {
            User user = userService.getById(authenticationMetaData.getId());
            model.addAttribute("user", user);
            return "comments";
        }

        User user = userService.getById(authenticationMetaData.getId());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        Comment comment = commentService.createComment(commentRequest, user, post);
        postService.addComment(post.getId(), comment);

        return "redirect:/posts";
    }

    @PutMapping("/{id}/likes")
    public String addLike(@PathVariable UUID id) {

        Post post = postService.addLike(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("post", post);
        return "redirect:/posts";
    }
}
