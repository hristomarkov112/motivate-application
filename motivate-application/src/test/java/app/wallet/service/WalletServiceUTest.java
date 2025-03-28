package app.wallet.service;

import app.exception.DomainException;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceUTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;


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
        assertEquals(new BigDecimal("100.00"), newWallet.getBalance());
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
}
