package app.web;

import app.membership.model.Membership;
import app.membership.model.MembershipPeriod;
import app.membership.model.MembershipStatus;
import app.membership.model.MembershipType;
import app.user.model.Country;
import app.user.model.User;
import app.user.model.UserRole;
import app.wallet.model.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public class TestBuilder {

    public static User aRandomUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123123")
                .role(UserRole.USER)
                .country(Country.BULGARIA)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .balance(BigDecimal.ZERO)
                .currency(Currency.getInstance("EUR"))
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Membership membership = Membership.builder()
                .id(UUID.randomUUID())
                .type(MembershipType.FREE)
                .price(BigDecimal.ZERO)
                .status(MembershipStatus.ACTIVE)
                .period(MembershipPeriod.MONTHLY)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMonths(1))
                .owner(user)
                .renewalAllowed(true)
                .build();

        user.setMemberships(List.of(membership));
        user.setWallets(List.of(wallet));

        return user;

    }

}
