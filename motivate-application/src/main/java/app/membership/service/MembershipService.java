package app.membership.service;

import app.membership.model.Membership;
import app.membership.model.MembershipPeriod;
import app.membership.model.MembershipStatus;
import app.membership.model.MembershipType;
import app.membership.repository.MembershipRepository;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;

    @Autowired
    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public void createDefaultMembership(User user) {

        Membership membership = membershipRepository.save(initializeMembership(user));;
        log.info("Successfully created new membership with id [%s] and type [%s].".formatted(membership.getId(), membership.getType()));

    }

    LocalDateTime now = LocalDateTime.now();

    private Membership initializeMembership(User user) {
        return Membership.builder()
                .owner(user)
                .status(MembershipStatus.ACTIVE)
                .period(MembershipPeriod.MONTHLY)
                .type(MembershipType.FREE)
                .price(new BigDecimal("0.00"))
                .renewalAllowed(true)
                .createdAt(now)
                .expiresAt(now.plusMonths(1))
                .build();
    }
}
