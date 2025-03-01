package app.web;

import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final UserService userService;

    @Autowired
    public PostController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/my-posts")
    public String getMyPostsPage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        return "posts";
    }
}
