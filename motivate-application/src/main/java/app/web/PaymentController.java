package app.web;

import app.payment.model.Payment;
import app.payment.service.PaymentService;
import app.user.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/history")
    public ModelAndView getAllPayments(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        List<Payment> payments = paymentService.getAllByOwnerId(userId);


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payments");
        modelAndView.addObject("payments", payments);

        return modelAndView;
    }
}
