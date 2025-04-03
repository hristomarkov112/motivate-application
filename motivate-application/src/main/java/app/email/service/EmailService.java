package app.email.service;

import app.web.dto.PaymentNotificationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @EventListener
    public void sendPaymentNotification(PaymentNotificationEvent event) {

        String formattedDate = event.getPaymentDate().format(FORMATTER);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getEmail());
        message.setSubject("Payment Notification");
        message.setText(
                "Dear Motiv8 Customer,\n\n" +
                "This is an email payment notification, aiming to confirm \n" +
                        "that you have made a successful deposit to your account!\n\n" +
                        "Deposit Amount: " + event.getAmount() + " EUR.\n" +
                        "Deposit Date: " + formattedDate + "\n\n\n" +
                        "In case you need any further help, you can always contact us on via email.\n" +
                        "Thank you for using our service!\n\n\n" +
                        "Best Regards\n" +
                        "Motiv8 Customer Support Team"
        );

        mailSender.send(message);
    }

    @Async
    @EventListener
    public void sendEmailWhenChargeHappen(PaymentNotificationEvent event) {

        System.out.printf("Thread [%s]: Charge happened for user with id [%s].\n", Thread.currentThread().getName(), event.getUserId());
    }
}
