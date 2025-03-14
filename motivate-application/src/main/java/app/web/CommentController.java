package app.web;

import app.comment.service.CommentService;
import app.post.model.Post;
import app.post.service.PostService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;


@Controller
@RequestMapping("/comments")
public class CommentController {

    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public CommentController(PostService postService, CommentService commentService, UserService userService) {
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping("/new")
    public String showCommentForm() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("comments");
        modelAndView.addObject("comment", new CommentRequest());
        return "/posts";
    }
//
//    @PostMapping("/new")
//    public ModelAndView createComment(@Valid CommentRequest commentRequest, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {
//
//        User user = userService.getById(authenticationMetaData.getId());
//
//        ModelAndView modelAndView = new ModelAndView();
//
//        UUID postId = commentRequest.getPostId();
//        Post post = postService.getById(postId);
//        commentService.createComment(commentRequest, user, post);
//        modelAndView.addObject("commentRequest", commentRequest);
//        modelAndView.addObject("post", post);
//        modelAndView.setViewName("redirect:/comments/new");
//
//        return modelAndView;
//    }

    @GetMapping("/delete/{id}")
    public String deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return "redirect:/posts"; // Redirect to the list of comments
    }
}
