package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;


@Controller
public class IndexController {

    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public IndexController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public String getLoginPage() {

        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {

        return "register";
    }

    @GetMapping("/home")
    public ModelAndView getHomePage() {

        User user = userService.getById(UUID.fromString("3693af92-fdfc-467d-bf5d-46f604e4eff2"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);

        return modelAndView;
    }







}
