package app.web;

import app.membership.model.Membership;
import app.membership.model.MembershipPeriod;
import app.membership.model.MembershipType;
import app.membership.service.MembershipService;
import app.payment.model.Payment;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.GetPremiumRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/memberships")
public class MembershipController {

    private final UserService userService;
    private final MembershipService membershipService;

    @Autowired
    public MembershipController(UserService userService, MembershipService membershipService) {
        this.userService = userService;
        this.membershipService = membershipService;
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
        modelAndView.addObject("getPremiumRequest", GetPremiumRequest.builder().build());

        return modelAndView;
    }

    @PostMapping("/get-premium")
    public String getPremium(@RequestParam("membership-type") MembershipType membershipType, GetPremiumRequest getPremiumRequest, HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        Payment getPremiumResult = membershipService.getPremium(user, membershipType, getPremiumRequest);

        return "redirect:/payments/" + getPremiumResult.getId();
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
