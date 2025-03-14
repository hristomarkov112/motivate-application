package app.user.service;

//import app.additionalinfo.service.AdditionalInfoService;
import app.exception.DomainException;
import app.membership.service.MembershipService;
import app.post.model.Post;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final MembershipService membershipService;
//    private final AdditionalInfoService additionalInfoService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService, MembershipService membershipService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.membershipService = membershipService;
//        this.additionalInfoService = additionalInfoService;
    }
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());
        if(optionalUser.isPresent()) {
            throw new DomainException("Username [%s] already exists.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));

        membershipService.createFreeMembership(user);
        walletService.createNewWallet(user);

//        additionalInfoService.saveAdditionalInfo(user.getId(), "UNKNOWN", "Enter phone", "Enter second email");

        log.info("Successfully registered new user account with username [%s] and id [%s].".formatted(user.getUsername(), user.getId()), user.getUsername());

        return user;
    }

    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setProfilePictureUrl(userEditRequest.getProfilePicture());
        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setBio(userEditRequest.getBio());
        user.setEmail(userEditRequest.getEmail());

        userRepository.save(user);

    }

    private User initializeUser(RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .country(registerRequest.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User getById(UUID id) {

        return userRepository.findUserById(id).orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));
    }

    public void blockUser(UUID userId) {
        User user = getById(userId);

        user.setActive(!user.isActive());
        userRepository.save(user);

        log.info("User with id [%s] blocked.".formatted(userId));
    }

    @CacheEvict(value = "users", allEntries = true)
    public void changeRole(UUID userId) {
        User user = getById(userId);

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }

        userRepository.save(user);

        log.info("User with id [%s] changed role.".formatted(userId));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new DomainException("Username [%s] does not exist.".formatted(username)));

        return new AuthenticationMetaData(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }
}
