package app.web;

import app.membership.model.MembershipPeriod;
import app.membership.model.MembershipType;
import app.membership.service.MembershipService;
import app.security.AuthenticationMetaData;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.PremiumRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import app.user.model.User;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipControllerUTest {

    @Mock
    private UserService userService;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private MembershipController membershipController;

    @Test
    public void getPremiumPage_ShouldReturnPremiumViewWithAttributes() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = membershipController.getPremiumPage(authData);

        assertEquals("premium", result.getViewName());
        assertNotNull(result.getModel().get("user"));
        assertEquals(mockUser, result.getModel().get("user"));
        assertNotNull(result.getModel().get("premiumRequest"));
        assertTrue(result.getModel().get("premiumRequest") instanceof PremiumRequest);

        verify(userService).getById(userId);
        verifyNoInteractions(membershipService);
    }

    @Test
    public void getPremiumPage_WhenUserNotFound_ShouldStillReturnView() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(null);

        ModelAndView result = membershipController.getPremiumPage(authData);

        assertEquals("premium", result.getViewName());
        assertNull(result.getModel().get("user"));
        assertNotNull(result.getModel().get("premiumRequest"));

        verify(userService).getById(userId);
        verifyNoInteractions(membershipService);
    }

    @Test
    public void getPremiumPage_ShouldCreateNewPremiumRequest() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = membershipController.getPremiumPage(authData);

        PremiumRequest premiumRequest = (PremiumRequest) result.getModel().get("premiumRequest");
        assertNotNull(premiumRequest);
    }

    @Test
    public void getPremiumPage_ShouldUseCorrectUserIdFromAuthentication() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        membershipController.getPremiumPage(authData);

        verify(userService).getById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void getPremiumPage_ShouldProcessPremiumRequestAndRedirect() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        MembershipType membershipType = MembershipType.PREMIUM;
        PremiumRequest premiumRequest = PremiumRequest.builder()
                .membershipPeriod(MembershipPeriod.MONTHLY)
                .walletId(UUID.randomUUID())
                .build();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        String result = membershipController.getPremiumPage(
                membershipType,
                premiumRequest,
                authData
        );

        assertEquals("redirect:/payments/result", result);

    }

    @Test
    public void getPremiumPage_WhenUserNotFound_ShouldStillRedirect() {

        UUID userId = UUID.randomUUID();
        MembershipType membershipType = MembershipType.PREMIUM;
        PremiumRequest premiumRequest = PremiumRequest.builder()
                .membershipPeriod(MembershipPeriod.MONTHLY)
                .walletId(UUID.randomUUID())
                .build();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(null);

        String result = membershipController.getPremiumPage(
                membershipType,
                premiumRequest,
                authData
        );

        assertEquals("redirect:/payments/result", result);
    }

    @Test
    public void getPremiumPage_ShouldUseCorrectMembershipType() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        PremiumRequest premiumRequest = PremiumRequest.builder()
                .membershipPeriod(MembershipPeriod.MONTHLY)
                .walletId(UUID.randomUUID())
                .build();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        for (MembershipType type : MembershipType.values()) {

            membershipController.getPremiumPage(
                    type,
                    premiumRequest,
                    authData
            );

            verify(membershipService).getPremium(mockUser, type, premiumRequest);
            reset(membershipService);
        }
    }

    @Test
    public void getPremiumPage_ShouldPassPremiumRequestToService() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        MembershipType membershipType = MembershipType.PREMIUM;
        PremiumRequest premiumRequest = PremiumRequest.builder()
                .membershipPeriod(MembershipPeriod.MONTHLY)
                .walletId(UUID.randomUUID())
                .build();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        membershipController.getPremiumPage(
                membershipType,
                premiumRequest,
                authData
        );
    }

    @Test
    public void getPremiumPage_ShouldNotProcessWhenUserNull() {

        UUID userId = UUID.randomUUID();
        MembershipType membershipType = MembershipType.PREMIUM;
        PremiumRequest premiumRequest = PremiumRequest.builder()
                .membershipPeriod(MembershipPeriod.MONTHLY)
                .walletId(UUID.randomUUID())
                .build();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(null);

        String result = membershipController.getPremiumPage(
                membershipType,
                premiumRequest,
                authData
        );

        assertEquals("redirect:/payments/result", result);
    }

    @Test
    public void getMembershipsPage_ShouldReturnMembershipsViewWithUser() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = membershipController.getMembershipsPage(authData);

        assertEquals("memberships", result.getViewName());
        assertNotNull(result.getModel().get("user"));
        assertEquals(mockUser, result.getModel().get("user"));

        verify(userService).getById(userId);
        verifyNoInteractions(membershipService);
    }

    @Test
    public void getMembershipsPage_WhenUserNotFound_ShouldStillReturnView() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(null);

        ModelAndView result = membershipController.getMembershipsPage(authData);

        assertEquals("memberships", result.getViewName());
        assertNull(result.getModel().get("user"));

        verify(userService).getById(userId);
        verifyNoInteractions(membershipService);
    }

    @Test
    public void getMembershipsPage_ShouldUseCorrectUserIdFromAuthentication() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        membershipController.getMembershipsPage(authData);

        verify(userService).getById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void getMembershipsPage_ShouldNotCallMembershipService() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        membershipController.getMembershipsPage(authData);

        verifyNoInteractions(membershipService);
    }

    @Test
    public void updateMembershipRenewal_WhenUserNotFound_ShouldStillRedirect() {

        UUID userId = UUID.randomUUID();
        boolean renewalAllowed = false;
        MembershipPeriod period = MembershipPeriod.MONTHLY;
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(null);

        String result = membershipController.updateMembershipRenewal(
                renewalAllowed,
                period,
                authData
        );

        assertEquals("redirect:/memberships/details", result);
        verify(userService).getById(userId);
        verifyNoInteractions(membershipService);
    }

    @Test
    public void updateMembershipRenewal_ShouldHandleAllPeriodTypes() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        for (MembershipPeriod period : MembershipPeriod.values()) {

            membershipController.updateMembershipRenewal(
                    true,
                    period,
                    authData
            );


            verify(membershipService).updateMembershipRenewal(mockUser, true, period);
            reset(membershipService);
        }
    }

    @Test
    public void updateMembershipRenewal_ShouldPassCorrectRenewalFlag() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        MembershipPeriod period = MembershipPeriod.ANNUAL;
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        membershipController.updateMembershipRenewal(true, period, authData);
        membershipController.updateMembershipRenewal(false, period, authData);

        verify(membershipService).updateMembershipRenewal(mockUser, true, period);
        verify(membershipService).updateMembershipRenewal(mockUser, false, period);
    }

    @Test
    public void updateMembershipRenewal_ShouldNotUseModelAndView() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        String result = membershipController.updateMembershipRenewal(
                true,
                MembershipPeriod.ANNUAL,
                authData
        );

        assertEquals("redirect:/memberships/details", result);
    }

    @Test
    public void getHistoryPage_ShouldReturnHistoryViewWithUser() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = membershipController.getHistoryPage(authData);

        assertEquals("membership-history", result.getViewName());
        assertNotNull(result.getModel().get("user"));
        assertEquals(mockUser, result.getModel().get("user"));
        verify(userService).getById(userId);
        verifyNoInteractions(membershipService);
    }

    @Test
    public void getHistoryPage_WhenUserNotFound_ShouldStillReturnView() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(null);

        ModelAndView result = membershipController.getHistoryPage(authData);

        assertEquals("membership-history", result.getViewName());
        assertNull(result.getModel().get("user"));
        verify(userService).getById(userId);
        verifyNoInteractions(membershipService);
    }

    @Test
    public void getHistoryPage_ShouldUseCorrectUserIdFromAuthentication() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        membershipController.getHistoryPage(authData);

        verify(userService).getById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void getHistoryPage_ShouldNotCallMembershipService() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        membershipController.getHistoryPage(authData);

        verifyNoInteractions(membershipService);
    }

    @Test
    public void getHistoryPage_ShouldReturnNewModelAndViewInstance() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        ModelAndView result = membershipController.getHistoryPage(authData);

        assertNotNull(result);
        assertNotSame(new ModelAndView(), result);
    }
}
