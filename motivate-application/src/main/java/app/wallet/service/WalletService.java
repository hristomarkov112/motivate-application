package app.wallet.service;

import app.exception.DomainException;
import app.exception.InsufficientFundsException;
import app.exception.WalletNotFoundException;
import app.payment.model.Payment;
import app.payment.model.PaymentStatus;
import app.payment.model.PaymentType;
import app.payment.service.PaymentService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;

import app.web.dto.PaymentNotificationEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Currency;

@Slf4j
@Service
public class WalletService {

    private static final String MASTERCARD = "MasterCard";
    private static final String MOTIV8_LIMITED = "Motiv8 Limited";
    private final WalletRepository walletRepository;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public WalletService(WalletRepository walletRepository,
                         PaymentService paymentService,
                         ApplicationEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.paymentService = paymentService;
        this.eventPublisher = eventPublisher;
    }

    public void createNewWallet(User user) {

        Wallet wallet = walletRepository.save(initializeWallet(user));

        log.info("Successfully created new wallet with id [%s] and balance [%.2f].".formatted(wallet.getId(), wallet.getBalance()));

    }

    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new WalletNotFoundException("Wallet with id [%s] does not exist.".formatted(walletId)));
    }

    @Transactional
    public Payment charge(User user, UUID walletId, BigDecimal amount, String description) {

        Wallet wallet = getWalletById(walletId);

        String failureReason = "Not sufficient funds for this transaction";

        if (wallet.getBalance().compareTo(amount) < 0) {
            return paymentService.createNewPayment(
                    user,
                    wallet.getId().toString(),
                    MOTIV8_LIMITED,
                    amount,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    PaymentType.WITHDRAWAL,
                    PaymentStatus.FAILED,
                    description,
                    failureReason
            );
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

        System.out.printf("Thread [%s]: Code in WalletService.class\n", Thread.currentThread().getName());

        return paymentService.createNewPayment(
                user,
                wallet.getId().toString(),
                MOTIV8_LIMITED,
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                PaymentType.WITHDRAWAL,
                PaymentStatus.SUCCESSFUL,
                description,
                ""
        );
    }

    private void publishPaymentNotification(User user, BigDecimal amount) {
        try {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                        .userId(user.getId())
                        .paymentDate(LocalDateTime.now())
                        .email(user.getEmail())
                        .amount(amount)
                        .build();

                eventPublisher.publishEvent(event);
            }
        } catch (Exception e) {
            log.error("Failed to publish payment event for user {}", user.getId(), e);
        }
    }

    protected Wallet initializeWallet(User user) {

        LocalDateTime now = LocalDateTime.now();

        List<Wallet> userWallets = walletRepository.findAllByOwnerUsername(user.getUsername());
        if(userWallets.size() == 1) {
            throw new DomainException("Username has already an existing wallet. Cannot create more than one.");
        }

        return Wallet.builder()
                .owner(user)
                .balance(new BigDecimal("0.00"))
                .currency(Currency.getInstance("EUR"))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Transactional
    public Payment deposit(User user, UUID walletId, BigDecimal amount) {

        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        Wallet wallet = getWalletById(walletId);
        String paymentDescription = "Top up with %.2f EUR".formatted(amount.doubleValue());

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

        publishPaymentNotification(user, amount);

        return paymentService.createNewPayment(wallet.getOwner(),
                MASTERCARD,
                walletId.toString(),
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                PaymentType.DEPOSIT,
                PaymentStatus.SUCCESSFUL,
                paymentDescription,
                null);
    }

    @Transactional
    public Payment withdrawal(UUID walletId, BigDecimal amount) {

        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        Wallet wallet = getWalletById(walletId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Current balance: %.2f, Attempted withdrawal: %.2f",
                            wallet.getBalance().doubleValue(),
                            amount.doubleValue())
            );
        }
        String paymentDescription = "Withdrawal to card of %.2f EUR".formatted(amount.doubleValue());

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

        return paymentService.createNewPayment(wallet.getOwner(),
                walletId.toString(),
                MASTERCARD,
                amount,
                wallet.getBalance(),
                wallet.getCurrency(),
                PaymentType.WITHDRAWAL,
                PaymentStatus.SUCCESSFUL,
                paymentDescription,
                null);
    }
}
