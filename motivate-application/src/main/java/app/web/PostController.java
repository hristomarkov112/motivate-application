package app.web;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.post.model.Post;
import app.post.service.PostService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PutMapping("/{id}/likes")
    public ModelAndView addLike(@PathVariable UUID id) {

        postService.addLike(id);

        return new ModelAndView("redirect:/home");
    }


}
