package app.payment.service;

import app.payment.model.Payment;
import app.payment.model.PaymentStatus;
import app.payment.model.PaymentType;
import app.payment.repository.PaymentRepository;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;


@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createNewPayment(User owner, String sender, String recipient, BigDecimal amount, BigDecimal balanceLeft, Currency currency, PaymentType type, PaymentStatus status, String paymentDescription, String failureReason) {

        return Payment.builder()
                .owner(owner)
                .sender(sender)
                .recipient(recipient)
                .amount(amount)
                .balanceLeft(balanceLeft)
                .currency(currency)
                .type(type)
                .status(status)
                .description(paymentDescription)
                .failureReason(failureReason)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
