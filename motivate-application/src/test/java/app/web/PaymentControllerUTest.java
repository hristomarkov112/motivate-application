package app.web;

import app.payment.model.Payment;
import app.payment.model.PaymentStatus;
import app.payment.model.PaymentType;
import app.payment.service.PaymentService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerUTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private AuthenticationMetaData authenticationMetaData;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    public void getAllPayments_ReturnsPaymentsViewWithPayments() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                 .id(userId)
                .username("gosho123")
                .password("password")
                .build();
        List<Payment> mockPayments = Arrays.asList(
                Payment.builder()
                        .id(UUID.randomUUID())
                        .owner(user)
                        .sender(user.getUsername())
                        .recipient(user.getUsername())
                        .amount(new BigDecimal("100.00"))
                        .currency(Currency.getInstance("EUR"))
                        .type(PaymentType.PURCHASE)
                        .status(PaymentStatus.SUCCESSFUL)
                        .description("test")
                        .createdAt(LocalDateTime.now())
                        .build(),

                Payment.builder()
                        .id(UUID.randomUUID())
                        .owner(user)
                        .sender(user.getUsername())
                        .recipient(user.getUsername())
                        .amount(new BigDecimal("100.00"))
                        .currency(Currency.getInstance("EUR"))
                        .type(PaymentType.PURCHASE)
                        .status(PaymentStatus.PENDING)
                        .description("test")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(paymentService.getAllByOwnerId(userId)).thenReturn(mockPayments);

        ModelAndView result = paymentController.getAllPayments(authenticationMetaData);

        assertAll(
                () -> assertEquals("payments", result.getViewName()),
                () -> assertNotNull(result.getModel().get("payments")),
                () -> assertEquals(2, ((List<?>) result.getModel().get("payments")).size()),
                () -> verify(paymentService).getAllByOwnerId(userId)
        );
    }

    @Test
    public void getAllPayments_WhenNoPaymentsExist_ReturnsEmptyList() {

        UUID userId = UUID.randomUUID();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(paymentService.getAllByOwnerId(userId)).thenReturn(Collections.emptyList());

        ModelAndView result = paymentController.getAllPayments(authenticationMetaData);

        assertTrue(((List<?>) result.getModel().get("payments")).isEmpty());
    }

    @Test
    public void getPremiumResultPage_ReturnsFirstPayment() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("gosho123")
                .password("password")
                .build();

        Payment mockPayment = Payment.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .sender(user.getUsername())
                .recipient(user.getUsername())
                .amount(new BigDecimal("100.00"))
                .currency(Currency.getInstance("EUR"))
                .type(PaymentType.PURCHASE)
                .status(PaymentStatus.SUCCESSFUL)
                .description("test")
                .createdAt(LocalDateTime.now())
                .build();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(paymentService.getAllByOwnerId(userId)).thenReturn(List.of(mockPayment));

        ModelAndView result = paymentController.getPremiumResultPage(authenticationMetaData);

        assertAll(
                () -> assertEquals("premium-result", result.getViewName()),
                () -> assertEquals(mockPayment, result.getModel().get("payment")),
                () -> verify(paymentService).getAllByOwnerId(userId)
        );
    }

    @Test
    public void getPremiumResultPage_WhenNoPaymentsExist_ThrowsIndexOutOfBoundsException() {

        UUID userId = UUID.randomUUID();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(paymentService.getAllByOwnerId(userId)).thenReturn(Collections.emptyList());

        assertThrows(IndexOutOfBoundsException.class, () -> {
            paymentController.getPremiumResultPage(authenticationMetaData);
        });
    }

    @Test
    public void getAllPayments_WhenAuthenticationMetaDataIsNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            paymentController.getAllPayments(null);
        });

        verifyNoInteractions(paymentService);
    }

    @Test
    public void getPremiumResultPage_WhenAuthenticationMetaDataIsNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            paymentController.getPremiumResultPage(null);
        });

        verifyNoInteractions(paymentService);
    }

    @Test
    public void getPremiumResultPage_WhenPaymentServiceThrowsException_PropagatesException() {

        UUID userId = UUID.randomUUID();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(paymentService.getAllByOwnerId(userId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            paymentController.getPremiumResultPage(authenticationMetaData);
        });
    }
}
