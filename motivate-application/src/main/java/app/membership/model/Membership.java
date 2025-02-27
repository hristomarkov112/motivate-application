package app.membership.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User owner;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipPeriod period;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipType type;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean renewalAllowed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
