package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final UserService userService;

    @Autowired
    public PaymentController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/history")
    public ModelAndView getPaymentsPage() {

        User user = userService.getById(UUID.fromString("3693af92-fdfc-467d-bf5d-46f604e4eff2"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payments-history");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
