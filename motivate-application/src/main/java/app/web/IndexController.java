package app.web;

import app.comment.service.CommentService;
import app.post.model.Post;
import app.post.service.PostService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
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

    @Autowired
    public IndexController(UserService userService,
                           PostService postService,
                           CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
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
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
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
        posts.forEach(post -> post.setContent(postService.formatPostContent(post.getContent())));

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "The text length must be less than or equal 4000 characters.") ;
        }

        return modelAndView;
    }
}
