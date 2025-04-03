package app.email.service;

import app.web.dto.PaymentNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceIntegrationTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @Test
    void sendPaymentNotification_ShouldSendEmailWithCorrectDetails() {

        LocalDateTime paymentDate = LocalDateTime.of(2023, 6, 15, 14, 30);
        String email = "test@abv.bg";
        double amount = 100.50;

        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(UUID.randomUUID())
                .email(email)
                .amount(new BigDecimal(amount))
                .paymentDate(paymentDate)
                .build();


        emailService.sendPaymentNotification(event);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(email, sentMessage.getTo()[0]);
        assertEquals("Payment Notification", sentMessage.getSubject());

        String expectedText =
                "Dear Motiv8 Customer,\n\n" +
                        "This is an email payment notification, aiming to confirm \n" +
                        "that you have made a successful deposit to your account!\n\n" +
                        "Deposit Amount: " + amount + " EUR.\n" +
                        "Deposit Date: " + paymentDate.format(FORMATTER) + "\n\n\n" +
                        "In case you need any further help, you can always contact us via email.\n" +
                        "Thank you for using our service!\n\n\n" +
                        "Best Regards\n" +
                        "Motiv8 Customer Support Team";

        assertEquals(expectedText, sentMessage.getText());
    }

    @Test
    void sendPaymentNotification_ShouldFormatDateCorrectly() {

        LocalDateTime paymentDate = LocalDateTime.of(2023, 12, 31, 23, 59);
        String email = "test@abv.bg";
        double amount = 200.0;

        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(UUID.randomUUID())
                .email(email)
                .amount(new BigDecimal(amount))
                .paymentDate(paymentDate)
                .build();

        emailService.sendPaymentNotification(event);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String emailText = sentMessage.getText();
        String expectedDate = "31 дек 2023, 23:59";

        System.out.println("Actual email text:\n" + emailText); // Debug output
        System.out.println("Looking for date: " + expectedDate); // Debug output

        assertTrue(emailText.contains("Deposit Date: " + expectedDate),
                String.format("Email text should contain '%s'. Full text:\n%s",
                        expectedDate, emailText));
    }
}
