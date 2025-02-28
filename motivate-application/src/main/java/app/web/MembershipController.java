package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/memberships")
public class MembershipController {

    private UserService userService;

    @Autowired
    public MembershipController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/details")
    public ModelAndView getMembershipsPage() {

        User user = userService.getById(UUID.fromString("69374c84-e26a-425c-b1ee-dbe813046475"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("memberships");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/get-premium")
    public ModelAndView getPremiumPage(Model model) {

        User user = userService.getById(UUID.fromString("3693af92-fdfc-467d-bf5d-46f604e4eff2"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("get-premium");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/history")
    public ModelAndView getHistoryPage() {

        User user = userService.getById(UUID.fromString("3693af92-fdfc-467d-bf5d-46f604e4eff2"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("membership-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
