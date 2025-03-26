package app.web;

import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/wallets")
public class WalletController {

    private final UserService userService;

    @Autowired
    public WalletController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ModelAndView getWalletsPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallets");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
