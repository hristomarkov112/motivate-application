package app.web;

import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
    public ModelAndView getMembershipsPage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("memberships");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/get-premium")
    public ModelAndView getPremiumPage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("get-premium");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/history")
    public ModelAndView getHistoryPage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("membership-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
