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
    private final CommentService commentService;
    private final FollowService followService;
    private final PaymentService paymentService;
    private final PostService postService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService, MembershipService membershipService, CommentService commentService, FollowService followService, PaymentService paymentService, PostService postService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.membershipService = membershipService;
        this.commentService = commentService;
        this.followService = followService;
        this.paymentService = paymentService;
        this.postService = postService;
    }

    public User login(LoginRequest loginRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
        if(optionalUser.isPresent()) {
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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User getById(UUID id) {

        return userRepository.findById(id).orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));
    }
}
