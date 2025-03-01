package app.user.model;

import app.comment.model.Comment;
import app.membership.model.Membership;
import app.post.model.Post;
import app.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    private String firstName;

    private String lastName;

    private String profilePictureUrl;

    private String bio;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private Country country;

    private boolean isBlocked;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "owner")
    @OrderBy("createdAt DESC")
    private List<Membership> memberships = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "owner")
    @OrderBy("createdAt DESC")
    private List<Wallet> wallets;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "owner")
    @OrderBy("createdAt DESC")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "owner")
    @OrderBy("createdAt DESC")
    private List<Comment> comments = new ArrayList<>();

}
