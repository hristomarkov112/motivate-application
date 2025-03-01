package app.user.service;

import app.comment.service.CommentService;
import app.exception.DomainException;
import app.follow.service.FollowService;
import app.membership.service.MembershipService;
import app.payment.service.PaymentService;
import app.post.service.PostService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final MembershipService membershipService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService, MembershipService membershipService, CommentService commentService, FollowService followService, PaymentService paymentService, PostService postService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.membershipService = membershipService;
    }

    public User login(LoginRequest loginRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
        if(optionalUser.isEmpty()) {
            throw new RuntimeException("Username or password is incorrect.");
        }

        User user = optionalUser.get();
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Username or password is incorrect.");
        }

        return user;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());
        if(optionalUser.isPresent()) {
            throw new DomainException("Username [%s] already exists.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));

        membershipService.createDefaultMembership(user);
        walletService.createNewWallet(user);

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
                .role(UserRole.ADMIN)
                .isBlocked(false)
                .country(registerRequest.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));
    }

    public void blockUser(UUID userId) {
        User user = getById(userId);

        user.setBlocked(!user.isBlocked());
        userRepository.save(user);

        log.info("User with id [%s] blocked.".formatted(userId));
    }

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
}
