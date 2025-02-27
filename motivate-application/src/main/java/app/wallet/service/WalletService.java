package app.wallet.service;

import app.payment.model.Payment;
import app.payment.model.PaymentStatus;
import app.payment.model.PaymentType;
import app.payment.service.PaymentService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {

    private static final String MASTERCARD = "MasterCard";
    private final WalletRepository walletRepository;
    private final PaymentService paymentService;

    @Autowired
    public WalletService(WalletRepository walletRepository, PaymentService paymentService) {
        this.walletRepository = walletRepository;
        this.paymentService = paymentService;
    }

    public void createNewWallet(User user) {

        Wallet wallet = walletRepository.save(initializeWallet(user));

        log.info("Successfully created new wallet with id [%s] and balance [%.2f].".formatted(wallet.getId(), wallet.getBalance()));

    }

    @Transactional
    public Payment topUp(UUID walletId, BigDecimal amount) {

        Wallet wallet = getWalletById(walletId);
        String paymentDescription = "Top up with %.2f EUR".formatted(amount.doubleValue());

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

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

    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet with id [%s] does not exist.".formatted(walletId)));
    }



    private Wallet initializeWallet(User user) {

        LocalDateTime now = LocalDateTime.now();

        return Wallet.builder()
                .owner(user)
                .balance(new BigDecimal("1000.00"))
                .currency(Currency.getInstance("EUR"))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
