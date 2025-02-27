package app.web;

import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/follow")
public class FollowController {

    private final UserService userService;

    @Autowired
    public FollowController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/following")
    public String getFollowingPage() {

        return "following";
    }

    @GetMapping("/followers")
    public String getFollowersPage() {

        return "followers";
    }
}
