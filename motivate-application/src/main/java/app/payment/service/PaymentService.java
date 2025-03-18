package app.payment.service;

import app.payment.model.Payment;
import app.payment.model.PaymentStatus;
import app.payment.model.PaymentType;
import app.payment.repository.PaymentRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    public Payment createNewPayment(User owner, String walletId, String recipient, BigDecimal amount, BigDecimal balanceLeft, Currency currency, PaymentType type, PaymentStatus status, String paymentDescription, String failureReason) {

        Payment payment = Payment.builder()
                .owner(owner)
                .sender(walletId)
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

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllByOwnerId(UUID ownerId) {

        return paymentRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }
}
