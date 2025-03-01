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
@RequestMapping("/follow")
public class FollowController {

    private final UserService userService;

    @Autowired
    public FollowController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/following")
    public String getFollowingPage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        return "following";
    }

    @GetMapping("/followers")
    public String getFollowersPage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        return "followers";
    }
}
