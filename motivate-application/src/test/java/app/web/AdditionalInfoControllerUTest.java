package app.web;

import app.additionalinfo.client.dto.AdditionalInfo;
import app.additionalinfo.service.AdditionalInfoService;
import app.security.AuthenticationMetaData;
import app.user.model.Country;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdditionalInfoControllerUTest {

    @Mock
    private AdditionalInfoService additionalInfoService;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationMetaData authenticationMetaData;

    @InjectMocks
    private AdditionalInfoController additionalInfoController;

    @Test
    void getAdditionalInfoPage_ShouldReturnCorrectModelAndView() {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .country(Country.BULGARIA)
                .username("gosho123")
                .build();
        AdditionalInfo mockInfo = AdditionalInfo.builder()
                .gender("MALE")
                .phoneNumber("0888854232")
                .secondEmail("mail@abv.bg")
                .build();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(additionalInfoService.getAdditionalInfo(userId)).thenReturn(mockInfo);

        ModelAndView mav = additionalInfoController.getAdditionalInfoPage(authenticationMetaData);

        assertAll(
                () -> assertEquals("additional-info", mav.getViewName()),
                () -> assertEquals(mockUser, mav.getModel().get("user")),
                () -> assertEquals(mockInfo, mav.getModel().get("additionalInfo")),
                () -> verify(userService).getById(userId),
                () -> verify(additionalInfoService).getAdditionalInfo(userId)
        );
    }

    @Test
    void getAdditionalInfoPage_WhenUserNotFound_ShouldThrowException() {

        UUID userId = UUID.randomUUID();
        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            additionalInfoController.getAdditionalInfoPage(authenticationMetaData);
        });
    }

    @Test
    void getAdditionalInfoPage_WhenAdditionalInfoNotFound_ShouldHandleNull() {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .country(Country.BULGARIA)
                .username("gosho123")
                .build();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(additionalInfoService.getAdditionalInfo(userId)).thenReturn(null);

        ModelAndView mav = additionalInfoController.getAdditionalInfoPage(authenticationMetaData);

        assertNull(mav.getModel().get("additionalInfo"));
    }

    @Test
    void getAdditionalInfoPage_WhenAuthDataIsNull_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            additionalInfoController.getAdditionalInfoPage(null);
        });
    }

    @Test
    void getProfileMenu_ShouldReturnCorrectModelAndView() {

        UUID pathId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(authUserId)
                .country(Country.BULGARIA)
                .username("gosho123")
                .build();
        AdditionalInfo mockInfo = AdditionalInfo.builder()
                .gender("MALE")
                .phoneNumber("0888854232")
                .secondEmail("mail@abv.bg")
                .build();

        when(authenticationMetaData.getId()).thenReturn(authUserId);
        when(userService.getById(authUserId)).thenReturn(mockUser);
        when(additionalInfoService.getAdditionalInfo(authUserId)).thenReturn(mockInfo);

        ModelAndView mav = additionalInfoController.getProfileMenu(pathId, authenticationMetaData);

        assertAll(
                () -> assertEquals("additional-info-menu", mav.getViewName()),
                () -> assertEquals(mockUser, mav.getModel().get("user")),
                () -> assertEquals(mockInfo, mav.getModel().get("additionalInfo")),
                () -> verify(userService).getById(authUserId),
                () -> verify(additionalInfoService).getAdditionalInfo(authUserId)
        );
    }

    @Test
    void getProfileMenu_WhenPathIdDifferentFromAuthId_ShouldUseAuthId() {
        UUID pathId = UUID.randomUUID(); // Different from authUserId
        UUID authUserId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(authUserId)
                .country(Country.BULGARIA)
                .username("gosho123")
                .build();

        when(authenticationMetaData.getId()).thenReturn(authUserId);
        when(userService.getById(authUserId)).thenReturn(mockUser);
        when(additionalInfoService.getAdditionalInfo(authUserId)).thenReturn(AdditionalInfo.builder()
                .gender("")
                .phoneNumber("")
                .secondEmail("")
                .build());

        ModelAndView mav = additionalInfoController.getProfileMenu(pathId, authenticationMetaData);

        verify(additionalInfoService).getAdditionalInfo(authUserId);
    }

    @Test
    void getProfileMenu_WhenUserNotFound_ShouldThrowException() {

        UUID userId = UUID.randomUUID();
        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            additionalInfoController.getProfileMenu(UUID.randomUUID(), authenticationMetaData);
        });
    }

    @Test
    void getProfileMenu_WhenAuthDataIsNull_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            additionalInfoController.getProfileMenu(UUID.randomUUID(), null);
        });
    }

    @Test
    void getProfileMenu_WhenAdditionalInfoNull_ShouldStillReturnView() {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .country(Country.BULGARIA)
                .username("gosho123")
                .build();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(additionalInfoService.getAdditionalInfo(userId)).thenReturn(null);

        ModelAndView mav = additionalInfoController.getProfileMenu(UUID.randomUUID(), authenticationMetaData);

        assertNull(mav.getModel().get("additionalInfo"));
        assertEquals("additional-info-menu", mav.getViewName());
    }

    @Test
    void submitAdditionalInfoForm_ShouldSaveAndRedirect() {

        UUID pathId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(authUserId)
                .country(Country.BULGARIA)
                .username("gosho123")
                .build();
        AdditionalInfo formData = AdditionalInfo.builder()
                .gender("MALE")
                .phoneNumber("0888854232")
                .secondEmail("mail@abv.bg")
                .build();
        formData.setGender("Male");
        formData.setPhoneNumber("123456789");
        formData.setSecondEmail("secondary@example.com");

        when(authenticationMetaData.getId()).thenReturn(authUserId);
        when(userService.getById(authUserId)).thenReturn(mockUser);

        ModelAndView mav = additionalInfoController.submitAdditionalInfoForm(
                pathId, formData, authenticationMetaData
        );

        // Assert
        assertAll(
                () -> assertEquals("redirect:/additional-info", mav.getViewName()),
                () -> verify(additionalInfoService).saveAdditionalInfo(
                        pathId,
                        "Male",
                        "123456789",
                        "secondary@example.com"
                ),
                () -> verify(userService).getById(authUserId)
        );
    }

    @Test
    void submitAdditionalInfoForm_ShouldUsePathIdForSaving() {

        UUID pathId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        when(authenticationMetaData.getId()).thenReturn(authUserId);
        when(userService.getById(authUserId)).thenReturn(new User());

        AdditionalInfo formData = AdditionalInfo.builder()
                .gender("MALE")
                .phoneNumber("0888854232")
                .secondEmail("mail@abv.bg")
                .build();
        formData.setGender("Female");
        formData.setPhoneNumber("987654321");

        additionalInfoController.submitAdditionalInfoForm(pathId, formData, authenticationMetaData);
    }

    @Test
    void submitAdditionalInfoForm_WhenUserNotFound_ShouldThrowException() {

        UUID userId = UUID.randomUUID();
        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            additionalInfoController.submitAdditionalInfoForm(
                    UUID.randomUUID(),
                    AdditionalInfo.builder()
                            .gender("MALE")
                            .phoneNumber("0888854232")
                            .secondEmail("mail@abv.bg")
                            .build(),
                    authenticationMetaData
            );
        });
    }

    @Test
    void submitAdditionalInfoForm_WhenAuthDataIsNull_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            additionalInfoController.submitAdditionalInfoForm(
                    UUID.randomUUID(),
                    AdditionalInfo.builder()
                            .gender("MALE")
                            .phoneNumber("0888854232")
                            .secondEmail("mail@abv.bg")
                            .build(),
                    null
            );
        });
    }

    @Test
    void submitAdditionalInfoForm_WhenPartialData_ShouldStillSave() {

        UUID pathId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        when(authenticationMetaData.getId()).thenReturn(authUserId);
        when(userService.getById(authUserId)).thenReturn(new User());

        AdditionalInfo formData = AdditionalInfo.builder()
                .gender("MALE")
                .phoneNumber("0888854232")
                .secondEmail("mail@abv.bg")
                .build();
        formData.setPhoneNumber("555555555"); // Only phone number provided

        ModelAndView mav = additionalInfoController.submitAdditionalInfoForm(
                pathId, formData, authenticationMetaData
        );

        assertEquals("redirect:/additional-info", mav.getViewName());
    }

    @Test
    void submitAdditionalInfoForm_WhenSaveFails_ShouldPropagateException() {

        UUID pathId = UUID.randomUUID();
        UUID authUserId = UUID.randomUUID();
        when(authenticationMetaData.getId()).thenReturn(authUserId);
        when(userService.getById(authUserId)).thenReturn(new User());

        AdditionalInfo formData = AdditionalInfo.builder()
                .gender("MALE")
                .phoneNumber("0888854232")
                .secondEmail("mail@abv.bg")
                .build();
        formData.setGender("Other");

        doThrow(new RuntimeException("Save failed"))
                .when(additionalInfoService)
                .saveAdditionalInfo(any(), any(), any(), any());

        assertThrows(RuntimeException.class, () -> {
            additionalInfoController.submitAdditionalInfoForm(
                    pathId, formData, authenticationMetaData
            );
        });
    }
}
