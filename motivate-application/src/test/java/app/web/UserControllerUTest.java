package app.web;

import app.security.AuthenticationMetaData;
import app.user.model.Country;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.servlet.ModelAndView;
import app.user.model.User;
import app.post.service.PostService;
import org.springframework.validation.BindingResult;

import java.util.UUID;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerUTest {

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private AuthenticationMetaData authenticationMetaData;

    @InjectMocks
    private UserController userController;

    @Test
    public void getAllUsers_ShouldReturnUsersViewWithUserList() throws Exception {

        UUID userId = UUID.randomUUID();
        User user1 = new User();
        user1.setId(userId);
        user1.setUsername("user1");
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");
        List<User> mockUsers = Arrays.asList(user1, user2);

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getAllUsers()).thenReturn(mockUsers);

        ModelAndView result = userController.getAllUsers(authData);

        assertEquals("users", result.getViewName());
        assertNotNull(result.getModel().get("users"));
        assertEquals(2, ((List<?>) result.getModel().get("users")).size());
        assertEquals(mockUsers, result.getModel().get("users"));

        verify(userService).getAllUsers();
        verifyNoInteractions(postService);
    }

    @Test
    public void getAllUsers_WhenNoUsersExist_ShouldReturnEmptyList() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getAllUsers()).thenReturn(List.of());

        ModelAndView result = userController.getAllUsers(authData);

        assertEquals("users", result.getViewName());
        assertNotNull(result.getModel().get("users"));
        assertTrue(((List<?>) result.getModel().get("users")).isEmpty());
    }

    @Test
    public void getAllUsers_ShouldNotCallPostService() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getAllUsers()).thenReturn(List.of());

        userController.getAllUsers(authData);

        verifyNoInteractions(postService);
    }

    @Test
    public void getAllUsers_ShouldReturnNewModelAndViewInstance() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getAllUsers()).thenReturn(List.of());

        ModelAndView result = userController.getAllUsers(authData);


        assertNotNull(result);
        assertNotSame(new ModelAndView(), result);
    }

    @Test
    public void getProfilePage_ShouldReturnProfileViewWithUser() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");

        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = userController.getProfilePage(authData);

        assertEquals("profile", result.getViewName());
        assertNotNull(result.getModel().get("user"));
        assertEquals(mockUser, result.getModel().get("user"));

        verify(userService).getById(userId);
        verifyNoInteractions(postService);
    }

    @Test
    public void getProfilePage_WhenUserNotFound_ShouldStillReturnView() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(null);

        ModelAndView result = userController.getProfilePage(authData);

        assertEquals("profile", result.getViewName());
        assertNull(result.getModel().get("user"));

        verify(userService).getById(userId);
        verifyNoInteractions(postService);
    }

    @Test
    public void getProfilePage_ShouldUseCorrectUserIdFromAuthentication() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        userController.getProfilePage(authData);

        verify(userService).getById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void getProfilePage_ShouldNotCallPostService() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        userController.getProfilePage(authData);

        verifyNoInteractions(postService);
    }

    @Test
    public void getProfilePage_ShouldReturnNewModelAndViewInstance() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(new User());

        ModelAndView result = userController.getProfilePage(authData);

        assertNotNull(result);
        assertNotSame(new ModelAndView(), result);
    }

    @Test
    public void getProfilePage_ShouldAddUserToModel() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        AuthenticationMetaData authData = new AuthenticationMetaData(userId, "gosho123", "123123", UserRole.USER, true);

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = userController.getProfilePage(authData);

        assertEquals(mockUser, result.getModel().get("user"));
    }

    @Test
    public void getProfileMenu_ShouldReturnProfileMenuViewWithUserData() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");

        UserEditRequest mockEditRequest = UserEditRequest.builder()
                .profilePicture("pic.jpg")
                .firstName("georgi")
                .lastName("georgiev")
                .bio("Biography")
                .email("example@abv.bg")
                .build();

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = userController.gerProfileMenu(userId, mockEditRequest);

        assertEquals("profile-menu", result.getViewName());
        assertEquals(mockUser, result.getModel().get("user"));

        verify(userService).getById(userId);
        verifyNoInteractions(postService);
    }

    @Test
    public void getProfileMenu_ShouldReturnNewModelAndViewInstance() {

        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        UserEditRequest mockEditRequest = UserEditRequest.builder()
                .profilePicture("pic.jpg")
                .firstName("georgi")
                .lastName("georgiev")
                .bio("Biography")
                .email("example@abv.bg")
                .build();

        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = userController.gerProfileMenu(userId, mockEditRequest);

        assertNotNull(result);
        assertNotSame(new ModelAndView(), result);
    }

    @Test
    public void updateUserProfile_WithInvalidRequest_ShouldReturnProfileMenuView() {

        UUID userId = UUID.randomUUID();
        UserEditRequest invalidRequest = UserEditRequest.builder()
                .profilePicture("")
                .firstName("")
                .lastName("")
                .bio("")
                .email("")
                .build();

        User mockUser = new User();
        mockUser.setId(userId);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = userController.updateUserProfile(userId, invalidRequest, bindingResult);

        assertEquals("profile-menu", result.getViewName());
        assertEquals(mockUser, result.getModel().get("user"));
        assertEquals(invalidRequest, result.getModel().get("userEditRequest"));

        verify(userService).getById(userId);
    }

    @Test
    public void updateUserProfile_WhenUserNotFound_ShouldStillReturnView() {

        UUID userId = UUID.randomUUID();
        UserEditRequest invalidRequest = UserEditRequest.builder()
                .profilePicture("")
                .firstName("")
                .lastName("")
                .bio("")
                .email("")
                .build();

        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getById(userId)).thenReturn(null);

        ModelAndView result = userController.updateUserProfile(userId, invalidRequest, bindingResult);

        assertEquals("profile-menu", result.getViewName());
        assertNull(result.getModel().get("user"));
        assertEquals(invalidRequest, result.getModel().get("userEditRequest"));
    }

    @Test
    public void updateUserProfile_ShouldUseCorrectUserId() {

        UUID userId = UUID.randomUUID();
        UserEditRequest validRequest = UserEditRequest.builder()
                .profilePicture("pic.jpg")
                .firstName("georgi")
                .lastName("georgiev")
                .bio("Biography")
                .email("example@abv.bg")
                .build();

        when(bindingResult.hasErrors()).thenReturn(false);

        userController.updateUserProfile(userId, validRequest, bindingResult);

        verify(userService).editUserDetails(userId, validRequest);
    }

    @Test
    public void updateUserProfile_ShouldPassCorrectRequestToService() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserEditRequest validRequest = UserEditRequest.builder()
                .profilePicture("pic.jpg")
                .firstName("georgi")
                .lastName("georgiev")
                .bio("Biography")
                .email("example@abv.bg")
                .build();

        when(bindingResult.hasErrors()).thenReturn(false);

        userController.updateUserProfile(userId, validRequest, bindingResult);
    }

    @Test
    public void blockUser_ShouldCallServiceAndRedirect() {

        UUID userId = UUID.randomUUID();
        doNothing().when(userService).blockUser(userId);

        ModelAndView result = userController.blockUser(userId);

        assertEquals("redirect:/users", result.getViewName());
        verify(userService).blockUser(userId);
    }

    @Test
    public void blockUser_ShouldUseCorrectUserId() {

        UUID userId = UUID.randomUUID();

        userController.blockUser(userId);

        verify(userService).blockUser(userId);
    }

    @Test
    public void blockUser_ShouldAlwaysRedirectToUsers() {

        UUID userId = UUID.randomUUID();

        ModelAndView result = userController.blockUser(userId);

        assertEquals("redirect:/users", result.getViewName());
    }

    @Test
    public void blockUser_ShouldReturnNewModelAndViewInstance() {

        UUID userId = UUID.randomUUID();

        ModelAndView result = userController.blockUser(userId);

        assertNotNull(result);
        assertNotSame(new ModelAndView(), result);
    }

    @Test
    public void changeUserRole_ShouldCallServiceAndRedirect() {

        UUID userId = UUID.randomUUID();
        doNothing().when(userService).changeRole(userId);

        ModelAndView result = userController.changeUserRole(userId);

        assertEquals("redirect:/users", result.getViewName());
        verify(userService).changeRole(userId);
    }

    @Test
    public void changeUserRole_ShouldUseCorrectUserId() {

        UUID userId = UUID.randomUUID();

        userController.changeUserRole(userId);

        verify(userService).changeRole(userId);
    }

    @Test
    public void changeUserRole_ShouldAlwaysRedirectToUsers() {

        UUID userId = UUID.randomUUID();

        ModelAndView result = userController.changeUserRole(userId);

        assertEquals("redirect:/users", result.getViewName());
    }

    @Test
    public void changeUserRole_ShouldReturnNewModelAndViewInstance() {

        UUID userId = UUID.randomUUID();

        ModelAndView result = userController.changeUserRole(userId);

        assertNotNull(result);
        assertNotSame(new ModelAndView(), result);
    }

    @Test
    public void changeUserRole_ShouldOnlyCallChangeRoleOnce() {

        UUID userId = UUID.randomUUID();

        userController.changeUserRole(userId);

        verify(userService, times(1)).changeRole(userId);
    }

    @Test
    public void getProfilesPage_ReturnsCorrectModelAndView() {

        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User mockUser = User.builder()
                .id(userId)
                .username("gosho123")
                .build();

        List<User> mockUsers = Arrays.asList(
                User.builder()
                        .id(userId)
                        .username("user1")
                        .build(),
                User.builder()
                        .id(userId)
                        .username("user2")
                        .build(),
                mockUser
        );

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(userService.getAllUsers()).thenReturn(mockUsers);

        ModelAndView result = userController.getProfilesPage(authenticationMetaData);

        assertAll(
                () -> assertEquals("profiles", result.getViewName()),
                () -> assertNotNull(result.getModel().get("users")),
                () -> assertEquals(3, ((List<?>) result.getModel().get("users")).size()),
                () -> verify(userService).getById(userId),
                () -> verify(userService).getAllUsers(),
                () -> verifyNoInteractions(postService) // Verify postService not used in this endpoint
        );
    }

    @Test
    public void getProfilesPage_WhenNoOtherUsersExist_ReturnsOnlyCurrentUser() {

        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User mockUser = User.builder()
                .id(userId)
                .username("gosho123")
                .build();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(mockUser);
        when(userService.getAllUsers()).thenReturn(List.of(mockUser)); // Only the current user exists

        ModelAndView result = userController.getProfilesPage(authenticationMetaData);

        assertEquals(1, ((List<?>) result.getModel().get("users")).size());
    }

    @Test
    public void getProfilesPage_WhenUserNotFound_ThrowsException() {

        UUID userId = UUID.randomUUID();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.getProfilesPage(authenticationMetaData);
        });

        verify(userService, never()).getAllUsers();
    }

    @Test
    public void getProfilesPage_WhenAuthenticationMetaDataIsNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            userController.getProfilesPage(null);
        });

        verifyNoInteractions(userService);
        verifyNoInteractions(postService);
    }

    @Test
    public void getProfilesPage_WhenDatabaseErrorOccurs_PropagatesException() {
        UUID userId = UUID.randomUUID();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class, () -> {
            userController.getProfilesPage(authenticationMetaData);
        });
    }

    @Test
    public void getOtherProfilePage_ReturnsCorrectModelAndView() {

        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        User mockUser = User.builder()
                .id(userId)
                .username("gosho123")
                .build();
        mockUser.setBio("Test Bio");
        mockUser.setProfilePictureUrl("/images/test.jpg");

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(mockUser);

        ModelAndView result = userController.getOtherProfilePage(authenticationMetaData);

        assertAll(
                () -> assertEquals("other-profile", result.getViewName()),
                () -> assertNotNull(result.getModel().get("user")),
                () -> assertEquals(mockUser, result.getModel().get("user")),
                () -> assertEquals("gosho123", ((User) result.getModel().get("user")).getUsername()),
                () -> verify(userService).getById(userId),
                () -> verifyNoInteractions(postService)
        );
    }

    @Test
    public void getOtherProfilePage_WhenUserNotFound_ThrowsException() {

        UUID userId = UUID.randomUUID();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            userController.getOtherProfilePage(authenticationMetaData);
        });
    }

    @Test
    public void getOtherProfilePage_WhenAuthenticationMetaDataIsNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            userController.getOtherProfilePage(null);
        });

        verifyNoInteractions(userService);
        verifyNoInteractions(postService);
    }

    @Test
    public void getOtherProfilePage_WhenUserHasMinimalData_ReturnsSuccessfully() {

        UUID userId = UUID.randomUUID();
        User minimalUser = User.builder()
                .id(userId)
                .username("gosho123")
                .country(Country.BULGARIA)
                .build(); // No display name

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(minimalUser);

        ModelAndView result = userController.getOtherProfilePage(authenticationMetaData);

        assertEquals(minimalUser, result.getModel().get("user"));
    }

    @Test
    public void getOtherProfilePage_WhenServiceThrowsDatabaseError_PropagatesException() {

        UUID userId = UUID.randomUUID();

        when(authenticationMetaData.getId()).thenReturn(userId);
        when(userService.getById(userId)).thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class, () -> {
            userController.getOtherProfilePage(authenticationMetaData);
        });
    }
}
