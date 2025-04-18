package app.web;

import app.membership.model.MembershipPeriod;
import app.membership.model.MembershipType;
import app.membership.service.MembershipService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.PremiumRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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

    @GetMapping()
    public ModelAndView getPremiumPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("premium");
        modelAndView.addObject("user", user);
        modelAndView.addObject("premiumRequest", PremiumRequest.builder().build());

        return modelAndView;
    }

    @PostMapping()
    public String getPremiumPage(@RequestParam("membership-type") MembershipType membershipType, PremiumRequest premiumRequest, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        membershipService.getPremium(user, membershipType, premiumRequest);

        return "redirect:/payments/result";
    }

    @GetMapping("/details")
    public ModelAndView getMembershipsPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("memberships");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PutMapping("/renewal")
    public String updateMembershipRenewal(@RequestParam boolean renewalAllowed,
                                          @RequestParam MembershipPeriod period,
                                          @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());
        if (user == null) {
            return "redirect:/memberships/details";
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("memberships");

        membershipService.updateMembershipRenewal(user, renewalAllowed, period);
        return "redirect:/memberships/details";
    }

    @GetMapping("/history")
    public ModelAndView getHistoryPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("membership-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
