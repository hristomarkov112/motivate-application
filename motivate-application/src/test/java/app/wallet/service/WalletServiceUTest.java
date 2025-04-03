package app.wallet.service;

import app.exception.DomainException;
import app.exception.InsufficientFundsException;
import app.exception.WalletNotFoundException;
import app.payment.model.Payment;
import app.payment.service.PaymentService;
import app.user.model.Country;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceUTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private WalletService walletService;

    private final UUID testWalletId = UUID.randomUUID();
    private final UUID testOwnerId = UUID.randomUUID();
    private final BigDecimal initialBalance = new BigDecimal("1000.00");


    @Test
    void createNewWallet_WhenUserProvided_CallsSaveWithInitializedWallet() {

        User user = User.builder()
                .username("gosho123")
                .password("password")
                .country(Country.BULGARIA)
                .build();

        Wallet expectedWallet = new Wallet();
        expectedWallet.setOwner(user);
        expectedWallet.setBalance(new BigDecimal("0.00"));

        when(walletRepository.save(any(Wallet.class))).thenReturn(expectedWallet);

        walletService.createNewWallet(user);

        verify(walletRepository).save(any(Wallet.class));
    }


    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100.00));
        wallet.setCurrency(Currency.getInstance("EUR"));
    }

    @Test
    void testGetWalletById_WalletExists() {

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Wallet foundWallet = walletService.getWalletById(walletId);

        assertNotNull(foundWallet);
        assertEquals(walletId, foundWallet.getId());
        assertEquals(BigDecimal.valueOf(100.00), foundWallet.getBalance());
        assertEquals(Currency.getInstance("EUR"), foundWallet.getCurrency());
    }

    @Test
    void testGetWalletById_WalletDoesNotExist() {

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> walletService.getWalletById(walletId));

        assertEquals("Wallet with id [%s] does not exist.".formatted(walletId), exception.getMessage());
    }

    @Test
    void testInitializeWallet_Success() {

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("gosho123");

        when(walletRepository.findAllByOwnerUsername(user.getUsername())).thenReturn(Collections.emptyList());

        Wallet newWallet = walletService.initializeWallet(user);

        assertNotNull(newWallet);
        assertEquals(user, newWallet.getOwner());
        assertEquals(new BigDecimal("0.00"), newWallet.getBalance());
        assertEquals(Currency.getInstance("EUR"), newWallet.getCurrency());
        assertNotNull(newWallet.getCreatedAt());
        assertNotNull(newWallet.getUpdatedAt());
    }

    @Test
    void testInitializeWallet_FailsWhenUserAlreadyHasWallet() {

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("gosho123");

        Wallet existingWallet = new Wallet();
        existingWallet.setOwner(user);

        when(walletRepository.findAllByOwnerUsername(user.getUsername())).thenReturn(List.of(existingWallet));

        DomainException exception = assertThrows(DomainException.class, () -> walletService.initializeWallet(user));

        assertEquals("Username has already an existing wallet. Cannot create more than one.", exception.getMessage());
    }

    @Test
    void deposit_ShouldProcessSuccessfully() {
        BigDecimal depositAmount = new BigDecimal("100.00");
        Wallet mockWallet = createTestWallet(initialBalance);
        Payment mockPayment = new Payment();

        when(walletRepository.findById(testWalletId)).thenReturn(java.util.Optional.of(mockWallet));

        when(paymentService.createNewPayment(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPayment);

        Payment result = walletService.deposit(testWalletId, depositAmount);

        assertNotNull(result);

        verify(walletRepository).save(mockWallet);
    }

    @Test
    void deposit_WhenWalletIdNull_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.deposit(null, new BigDecimal("100.00"));
        }, "Wallet ID must not be null");
    }

    @Test
    void deposit_WhenAmountNull_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.deposit(testWalletId, null);
        }, "Amount must not be null");
    }

    @Test
    void deposit_WhenAmountZeroOrNegative_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.deposit(testWalletId, BigDecimal.ZERO);
        }, "Deposit amount must be positive");

        assertThrows(IllegalArgumentException.class, () -> {
            walletService.deposit(testWalletId, new BigDecimal("-100.00"));
        }, "Deposit amount must be positive");
    }

    @Test
    void deposit_WhenWalletNotFound_ShouldThrow() {
        when(walletRepository.findById(testWalletId)).thenReturn(java.util.Optional.empty());

        WalletNotFoundException exception = assertThrows(WalletNotFoundException.class, () -> {
            walletService.deposit(testWalletId, new BigDecimal("100.00"));
        });

        assertTrue(exception.getMessage().contains(testWalletId.toString()));
    }

    @Test
    void deposit_ShouldFormatCurrencyCorrectly() {

        BigDecimal depositAmount = new BigDecimal("123.45");
        Wallet mockWallet = createTestWallet(initialBalance);
        mockWallet.setCurrency(Currency.getInstance("USD"));

        when(walletRepository.findById(testWalletId)).thenReturn(java.util.Optional.of(mockWallet));

        walletService.deposit(testWalletId, depositAmount);
    }


    @Test
    void withdrawal_ShouldProcessSuccessfully() {
        User user = User.builder()
                .id(testOwnerId)
                .build();

        Wallet wallet = new Wallet();
        wallet.setId(testWalletId);
        wallet.setOwner(user);
        wallet.setBalance(initialBalance);
        wallet.setCurrency(Currency.getInstance("EUR"));
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());

        BigDecimal withdrawalAmount = new BigDecimal("100.00");
        Wallet mockWallet = createTestWallet(initialBalance);

        when(walletRepository.findById(testWalletId)).thenReturn(java.util.Optional.of(mockWallet));
        when(paymentService.createNewPayment(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Payment());

        Payment result = walletService.withdrawal(testWalletId, withdrawalAmount);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("900.00").compareTo(mockWallet.getBalance()));
        verify(walletRepository).save(mockWallet);
    }

    @Test
    void withdrawal_WhenWalletIdNull_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdrawal(null, new BigDecimal("100.00"));
        });
    }

    @Test
    void withdrawal_WhenAmountNull_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdrawal(testWalletId, null);
        });
    }

    @Test
    void withdrawal_WhenAmountZeroOrNegative_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdrawal(testWalletId, BigDecimal.ZERO);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdrawal(testWalletId, new BigDecimal("-100.00"));
        });
    }

    @Test
    void withdrawal_WhenInsufficientFunds_ShouldThrow() {

        Wallet mockWallet = createTestWallet(new BigDecimal("100.00"));
        when(walletRepository.findById(testWalletId)).thenReturn(java.util.Optional.of(mockWallet));

        assertThrows(InsufficientFundsException.class, () -> {
            walletService.withdrawal(testWalletId, new BigDecimal("200.00"));
        });

        assertEquals(0, new BigDecimal("100.00").compareTo(mockWallet.getBalance()));
        verify(walletRepository, never()).save(any());
    }

    @Test
    void withdrawal_WhenWalletNotFound_ShouldThrow() {
        when(walletRepository.findById(testWalletId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            walletService.withdrawal(testWalletId, new BigDecimal("100.00"));
        });
    }

    private Wallet createTestWallet(BigDecimal initialBalance) {
        User user = User.builder()
                .id(testOwnerId)
                .build();

        Wallet wallet = new Wallet();
        wallet.setId(testWalletId);
        wallet.setOwner(user);
        wallet.setBalance(initialBalance);
        wallet.setCurrency(Currency.getInstance("EUR"));
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        return wallet;
    }
}


