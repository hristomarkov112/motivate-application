package app.user.service;

import app.additionalinfo.service.AdditionalInfoService;
import app.exception.DomainException;
import app.exception.UsernameAlreadyExistsException;
import app.membership.service.MembershipService;
import app.user.model.Country;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WalletService walletService;
    @Mock
    private MembershipService membershipService;
    @Mock
    private AdditionalInfoService additionalInfoService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_ShouldSaveUser_WhenUsernameIsUnique() {

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("Gosho123");
        registerRequest.setPassword("password");
        registerRequest.setCountry(Country.BULGARIA);

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("Gosho123")
                .password("encodedPassword")
                .role(UserRole.USER)
                .isActive(true)
                .country(registerRequest.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        when(userRepository.findByUsername("Gosho123")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User registeredUser = userService.register(registerRequest);


        assertNotNull(registeredUser);
        assertEquals("Gosho123", registeredUser.getUsername());
        assertEquals("encodedPassword", registeredUser.getPassword());
        assertEquals(UserRole.USER, registeredUser.getRole());
        assertTrue(registeredUser.isActive());
        assertEquals(Country.BULGARIA, registeredUser.getCountry());
        assertNotNull(registeredUser.getCreatedAt());
        assertNotNull(registeredUser.getUpdatedAt());

        verify(userRepository, times(1)).findByUsername("Gosho123");
        verify(passwordEncoder, times(1)).encode("password");
        verify(membershipService, times(1)).createFreeMembership(savedUser);
        verify(walletService, times(1)).createNewWallet(savedUser);
        verify(additionalInfoService, times(1)).saveAdditionalInfo(savedUser.getId(), "UNSELECTED", "Enter phone", "Enter second email");
    }

    @Test
    void register_ShouldThrowUsernameAlreadyExistsException_WhenUsernameExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("gosho123");
        registerRequest.setPassword("password");
        registerRequest.setCountry(Country.BULGARIA);

        User user = new User();
        user.setUsername("gosho123");

        when(userRepository.findByUsername("gosho123")).thenReturn(Optional.of(user));

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.register(registerRequest));

        verify(userRepository, times(1)).findByUsername("gosho123");
    }

    @Test
    void editUserDetails_ShouldUpdateUserDetails_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        UserEditRequest userEditRequest = UserEditRequest.builder()
                .firstName("Georgi")
                .lastName("Georgiev")
                .email("georgiev@gmail.com")
                .profilePicture("new.jpg")
                .bio("Passionate traveller, aiming to show you the good part of the world.")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .firstName("Petar")
                .lastName("Petrov")
                .email("petrov@gmail.com")
                .profilePictureUrl("pic.jpg")
                .bio("Biography")
                .build();

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(existingUser));

        userService.editUserDetails(userId, userEditRequest);

        assertEquals("Georgi", existingUser.getFirstName());
        assertEquals("Georgiev", existingUser.getLastName());
        assertEquals("georgiev@gmail.com", existingUser.getEmail());
        assertEquals("new.jpg", existingUser.getProfilePictureUrl());
        assertEquals("Passionate traveller, aiming to show you the good part of the world.", existingUser.getBio());
        assertNotNull(existingUser.getUpdatedAt());

        verify(userRepository, times(1)).findUserById(userId);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void register_ShouldInitializeUserCorrectly_WhenUsernameIsUnique() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("gosho123")
                .password("password")
                .country(Country.BULGARIA)
                .build();

        User registeredUser = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .country(registerRequest.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertNotNull(registeredUser);
        assertEquals(registerRequest.getUsername(), registeredUser.getUsername());
        assertEquals(passwordEncoder.encode(registerRequest.getPassword()), registeredUser.getPassword());
        assertEquals(UserRole.USER, registeredUser.getRole());
        assertTrue(registeredUser.isActive());
        assertEquals(Country.BULGARIA, registeredUser.getCountry());
        assertNotNull(registeredUser.getCreatedAt());
        assertNotNull(registeredUser.getUpdatedAt());
    }

    @Test
    void blockUser_ShouldToggleUserActiveStatus_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("gosho123")
                .isActive(true) // Initially active
                .build();

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));

        userService.blockUser(userId);

        assertFalse(user.isActive()); // Verify the user is now inactive (blocked)
        verify(userRepository, times(1)).save(user); // Verify the user was saved
        verify(userRepository, times(1)).findUserById(userId); // Verify the user was fetched
    }

    @Test
    void blockUser_ShouldThrowDomainException_WhenUserDoesNotExist() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findUserById(userId)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> userService.blockUser(userId));

        verify(userRepository, times(1)).findUserById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeRole_ShouldChangeRoleFromUserToAdmin_WhenUserIsUser() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("gosho123")
                .role(UserRole.USER)
                .build();

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId);

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).findUserById(userId);
    }

    @Test
    void changeRole_ShouldChangeRoleFromAdminToUser_WhenUserIsAdmin() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("gosho123")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId);

        assertEquals(UserRole.USER, user.getRole());
        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).findUserById(userId);
    }

    @Test
    void changeRole_ShouldThrowDomainException_WhenUserDoesNotExist() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findUserById(userId)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> userService.changeRole(userId));

        verify(userRepository, times(1)).findUserById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {

        String username = "gosho123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password("password")
                .role(UserRole.USER)
                .isActive(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_ShouldThrowDomainException_WhenUserDoesNotExist() {

        String username = "petar123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> userService.loadUserByUsername(username));

        verify(userRepository, times(1)).findByUsername(username);
    }
}
