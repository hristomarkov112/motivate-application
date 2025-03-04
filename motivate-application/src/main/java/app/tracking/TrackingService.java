package app.tracking;

import app.web.dto.PaymentNotificationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TrackingService {

    @Async
    @EventListener
    public void trackNewPayment(PaymentNotificationEvent event) {

        System.out.printf("New payment has been made for %s's membership plan.\n", event.getUserId());
    }
}
