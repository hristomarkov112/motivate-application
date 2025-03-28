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
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createNewPayment(User owner, String walletId, String recipient, BigDecimal amount, BigDecimal balanceLeft, Currency currency, PaymentType type, PaymentStatus status, String paymentDescription, String failureReason) {

        if (owner == null) {
            throw new IllegalArgumentException("Owner must not be null");
        }
        if (walletId == null || walletId.isBlank()) {
            throw new IllegalArgumentException("Wallet ID must not be empty");
        }

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
