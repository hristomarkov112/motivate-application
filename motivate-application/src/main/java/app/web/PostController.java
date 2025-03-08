package app.web;

import app.comment.model.Comment;
import app.comment.service.CommentService;
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

        //Comments
//        List<Comment> comments = commentService.getAllCommentsByPost(user);
        modelAndView.addObject("commentRequest", new CommentRequest());
//        modelAndView.addObject("comments", comments);

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
        List<Post> postsById = postService.getPostsByUserId(authenticationMetaData.getId());

        //My Posts
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("my-posts");
        modelAndView.addObject("posts", postsById);

        return modelAndView;
    }

    @PostMapping("/{id}/comment")
    public ModelAndView addComment(
            @PathVariable UUID id, @Valid CommentRequest commentRequest, BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("register");
        }
        // Fetch the current user
        User user = userService.getById(authenticationMetaData.getId());


        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("commentRequest", commentRequest);
        // Fetch the post to which the comment belongs
        Post post = postService.getById(id); // Use the post ID from the URL

        // Create the comment and associate it with the user and post
        Comment comment = commentService.createComment(commentRequest, user, post);

        // Add the comment to the post
        postService.addComment(post.getId(), comment);

        // Redirect to the post details page or home page
        return new ModelAndView("redirect:/posts/" + id); // Redirect to the post details page
    }

    @PutMapping("/{id}/likes")
    public ModelAndView addLike(@PathVariable UUID id) {

        postService.addLike(id);

        return new ModelAndView("redirect:/home");
    }
}
