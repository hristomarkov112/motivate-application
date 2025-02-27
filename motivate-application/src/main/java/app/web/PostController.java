package app.web;

import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final UserService userService;

    @Autowired
    public PostController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/posts")
    public String getMyPostsPage() {

        return "posts";
    }
}
