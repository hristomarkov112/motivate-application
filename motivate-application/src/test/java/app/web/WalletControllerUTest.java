package app.web;

import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

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
}
