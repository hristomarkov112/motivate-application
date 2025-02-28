package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getProfilePage() {

        User user = userService.getById(UUID.fromString("3693af92-fdfc-467d-bf5d-46f604e4eff2"));


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("/{id}/profile-menu")
    public ModelAndView gerProfileMenu(@PathVariable UUID id) {

        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile-menu");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage() {

        User user = userService.getById(UUID.fromString("3693af92-fdfc-467d-bf5d-46f604e4eff2"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/edit-profile")
    public String getEditProfilePage() {

        return "profile-menu";
    }

    @GetMapping("/admin-panel")
    public ModelAndView getAdminPanelPage() {

        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin-panel");
        modelAndView.addObject("users", users);

        return modelAndView;
    }
}
