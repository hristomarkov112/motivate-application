package app.web;

import app.comment.model.Comment;
import app.comment.service.CommentService;
import app.post.model.Post;
import app.post.service.PostService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CommentRequest;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class IndexController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    @Autowired
    public IndexController(UserService userService,
                           PostService postService,
                           CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password!") ;
        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(@RequestParam(value = "error", required = false) String errorParam) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Username has been already registered!") ;
        }

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData, @RequestParam(value = "error", required = false) String errorParam) {

        User user = userService.getById(authenticationMetaData.getId());

        //User
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);

        //Posts
        List<Post> posts = postService.getAllPosts();
        modelAndView.addObject("posts", posts);

        //Comments
//        List<Comment> comments = commentService.getAllCommentsByPost(post);
//        modelAndView.addObject("commentRequest", new CommentRequest());
//        modelAndView.addObject("comments", comments);

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "The text length must be less than or equal 4000 characters.") ;
        }

        return modelAndView;
    }

//    @PostMapping("/home/comment")
//    public ModelAndView createCommentPage(@Valid CommentRequest commentRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {
//
//        ModelAndView modelAndView = new ModelAndView();
//        User user = userService.getById(authenticationMetaData.getId());
//
//        if (bindingResult.hasErrors()) {
//            modelAndView.setViewName("home");
//        }
//
//        commentService.createComment(commentRequest, user);
//
//        return new ModelAndView("redirect:/home");
//    }

}
