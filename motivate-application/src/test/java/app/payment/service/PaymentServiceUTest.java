package app.payment.service;

import app.payment.model.Payment;
import app.payment.model.PaymentStatus;
import app.payment.model.PaymentType;
import app.payment.repository.PaymentRepository;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createNewPayment_WithValidParameters_ReturnsSavedPayment() {

        User owner = User.builder()
                .username("gosho123")
                .build();
        String walletId = "wallet-123";
        String recipient = "petar123";
        BigDecimal amount = new BigDecimal("100.50");
        BigDecimal balanceLeft = new BigDecimal("500.00");
        Currency currency = Currency.getInstance("EUR");
        PaymentType type = PaymentType.PURCHASE;
        PaymentStatus status = PaymentStatus.SUCCESSFUL;
        String description = "Test payment";
        String failureReason = null;

        Payment expectedPayment = Payment.builder()
                .owner(owner)
                .sender(walletId)
                .recipient(recipient)
                .amount(amount)
                .balanceLeft(balanceLeft)
                .currency(currency)
                .type(type)
                .status(status)
                .description(description)
                .failureReason(failureReason)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);

        Payment result = paymentService.createNewPayment(
                owner, walletId, recipient, amount, balanceLeft,
                currency, type, status, description, failureReason
        );

        assertThat(result).isNotNull();
        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getAmount()).isEqualByComparingTo(amount);
        assertThat(result.getStatus()).isEqualTo(status);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createNewPayment_WithNullOwner_ThrowsException() {

        String expectedMessage = "Owner must not be null";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createNewPayment(
                        null,
                        "wallet-123",
                        "petar123",
                        new BigDecimal("100.00"),
                        new BigDecimal("500.00"),
                        Currency.getInstance("USD"),
                        PaymentType.PURCHASE,
                        PaymentStatus.SUCCESSFUL,
                        "Test payment",
                        null
                )
        );

        assertThat(exception.getMessage()).contains(expectedMessage);

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void getAllByOwnerId_WithValidId_ReturnsPayments() {

        UUID ownerId = UUID.randomUUID();
        User owner = User.builder()
                .username("gosho123")
                .build();

        Payment payment1 = Payment.builder()
                .owner(owner)
                .amount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Payment payment2 = Payment.builder()
                .owner(owner)
                .amount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();

        List<Payment> expectedPayments = Arrays.asList(payment2, payment1);

        when(paymentRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId))
                .thenReturn(expectedPayments);

        List<Payment> result = paymentService.getAllByOwnerId(ownerId);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedPayments);
        assertThat(result.get(0).equals(payment2)).isTrue();
        verify(paymentRepository).findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Test
    void getAllByOwnerId_WithNoPayments_ReturnsEmptyList() {

        UUID ownerId = UUID.randomUUID();
        when(paymentRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId))
                .thenReturn(Collections.emptyList());

        List<Payment> result = paymentService.getAllByOwnerId(ownerId);

        assertThat(result).isEmpty();
        verify(paymentRepository).findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }
}
