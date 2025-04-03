package app.web;

import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.DepositRequest;
import app.web.dto.WithdrawalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class WalletControllerUTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationMetaData authenticationMetaData;

    @InjectMocks
    private WalletController walletController;

    @Mock
    private WalletService walletService;

    @Mock
    private Model model;

    @Mock
    private AuthenticationMetaData auth;

    @Mock
    private BindingResult bindingResult;

    private final UUID testUserId = UUID.randomUUID();
    private final UUID testWalletId = UUID.randomUUID();

    @Test
    void showDepositForm_ShouldReturnFormWithEmptyRequest() {

        String viewName = walletController.showDepositForm(model);

        assertEquals("deposit-form", viewName);
        verify(model).addAttribute(eq("depositRequest"),
                argThat(req ->
                        ((DepositRequest)req).getAmount().compareTo(BigDecimal.ZERO) == 0
                ));
    }

    @Test
    void getWalletsPage_ShouldReturnCorrectViewAndModel() {

        UUID userId = UUID.randomUUID();
        User testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testUser");

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);

        ModelAndView result = walletController.getWalletsPage(authenticationMetaData);

        assertNotNull(result);
        assertEquals("wallets", result.getViewName());
        assertTrue(result.getModel().containsKey("user"));
        assertEquals(testUser, result.getModel().get("user"));
    }

    @Test
    void getWalletsPage_ShouldThrowWhenUserNotFound() {

        UUID userId = UUID.randomUUID();
        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            walletController.getWalletsPage(authenticationMetaData);
        });
    }

    @Test
    void processDeposit_ShouldHandleFirstWallet() {
        User user = User.builder()
                .id(testUserId)
                .build();
        Wallet firstWallet = Wallet.builder()
                .id(testWalletId)
                .build();

        Wallet secondWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .build();
        user.setWallets(List.of(firstWallet, secondWallet));

        when(auth.getId()).thenReturn(testUserId);
        when(userService.getById(testUserId)).thenReturn(user);

        DepositRequest request = new DepositRequest(new BigDecimal("200.00"));

        String result = walletController.processDeposit(auth, request, model);

        verify(walletService).deposit(user, testWalletId, new BigDecimal("200.00"));
        assertEquals("deposit-result", result);
    }

    @Test
    void processDeposit_ShouldDepositAndRedirect() {

        User mockUser = User.builder()
                .id(testUserId)
                .build();
        Wallet mockWallet = Wallet.builder()
                .id(testWalletId)
                .build();
        mockUser.setWallets(List.of(mockWallet));

        DepositRequest request = new DepositRequest(new BigDecimal("100.00"));

        when(auth.getId()).thenReturn(testUserId);
        when(userService.getById(testUserId)).thenReturn(mockUser);


        String redirect = walletController.processDeposit(auth, request, model);


        assertEquals("deposit-result", redirect);
        verify(walletService).deposit(mockUser, testWalletId, new BigDecimal("100.00"));
        verify(model).addAttribute("depositRequest", request);
    }

    @Test
    void processDeposit_WhenUserNotFound_ShouldThrow() {

        DepositRequest request = new DepositRequest(new BigDecimal("50.00"));
        when(auth.getId()).thenReturn(testUserId);
        when(userService.getById(testUserId)).thenReturn(null);


        assertThrows(RuntimeException.class, () -> {
            walletController.processDeposit(auth, request, model);
        });
    }

    @Test
    void processDeposit_WhenUserHasNoWallets_ShouldThrow() {

        User mockUser = User.builder()
                .id(testUserId)
                .build();
        DepositRequest request = new DepositRequest(new BigDecimal("25.00"));

        when(auth.getId()).thenReturn(testUserId);
        when(userService.getById(testUserId)).thenReturn(mockUser);

        assertThrows(NullPointerException.class, () -> {
            walletController.processDeposit(auth, request, model);
        });
    }

    @Test
    void processWithdrawal_ShouldWithdrawAndReturnResultView() {

        User mockUser = User.builder()
                .id(testUserId)
                .build();
        Wallet mockWallet = Wallet.builder()
                .id(testWalletId)
                .build();
        mockUser.setWallets(List.of(mockWallet));

        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("100.00"));

        when(auth.getId()).thenReturn(testUserId);
        when(userService.getById(testUserId)).thenReturn(mockUser);

        String viewName = walletController.processWithdrawal(auth, request, model, bindingResult);

        assertEquals("withdrawal-result", viewName);
        verify(walletService).withdrawal(testWalletId, new BigDecimal("100.00"));
        verify(model).addAttribute("withdrawalRequest", request);
    }

    @Test
    void processWithdrawal_WhenInsufficientFunds_ShouldThrow() {

        User mockUser = User.builder()
                .id(testUserId)
                .build();
        Wallet mockWallet = Wallet.builder()
                .id(testWalletId)
                .build();
        mockUser.setWallets(List.of(mockWallet));

        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("1000.00"));

        when(auth.getId()).thenReturn(testUserId);
        when(userService.getById(testUserId)).thenReturn(mockUser);
        doThrow(new IllegalArgumentException("Insufficient funds"))
                .when(walletService).withdrawal(any(), any());

        assertThrows(IllegalArgumentException.class, () -> {
            walletController.processWithdrawal(auth, request, model, bindingResult);
        });
    }
}
