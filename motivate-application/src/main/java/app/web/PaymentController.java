package app.web;

import app.payment.model.Payment;
import app.payment.service.PaymentService;
import app.security.AuthenticationMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/history")
    public ModelAndView getAllPayments(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        List<Payment> payments = paymentService.getAllByOwnerId(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payments");
        modelAndView.addObject("payments", payments);

        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getPremiumResultPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        Payment payment = paymentService.getAllByOwnerId(authenticationMetaData.getId()).get(0);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("premium-result");
        modelAndView.addObject("payment", payment);

        return modelAndView;
    }
}
