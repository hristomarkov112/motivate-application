package app.membership.service;

import app.exception.DomainException;
import app.exception.NoActiveMembershipException;
import app.membership.model.Membership;
import app.membership.model.MembershipPeriod;
import app.membership.model.MembershipStatus;
import app.membership.model.MembershipType;
import app.membership.repository.MembershipRepository;
import app.payment.model.Payment;
import app.payment.model.PaymentStatus;
import app.user.model.User;
import app.wallet.service.WalletService;
import app.web.dto.PremiumRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final WalletService walletService;


    @Autowired
    public MembershipService(MembershipRepository membershipRepository, WalletService walletService) {
        this.membershipRepository = membershipRepository;
        this.walletService = walletService;
    }

    public void createFreeMembership(User user) {

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

    @Transactional
    public Payment getPremium(User user, MembershipType membershipType, PremiumRequest premiumRequest) {

        Optional<Membership> optionalMembership = membershipRepository.findByStatusAndOwnerId(MembershipStatus.ACTIVE, user.getId());
        if (optionalMembership.isEmpty()) {
            throw new NoActiveMembershipException("No active membership has been found for this user.");
        }

        Membership currentMembership = optionalMembership.get();

        MembershipPeriod membershipPeriod = premiumRequest.getMembershipPeriod();
        String period = membershipPeriod.name().substring(0, 1).toUpperCase() + membershipPeriod.name().substring(1);
        String type = membershipType.name().substring(0, 1).toUpperCase() + membershipType.name().substring(1);
        String chargeDescription = "Purchased %s %s plan".formatted(period.toLowerCase(), type.toLowerCase());
        BigDecimal membershipPrice = getMembershipPrice(membershipType, membershipPeriod);

        Payment chargeResult = walletService.charge(user, premiumRequest.getWalletId(), membershipPrice, chargeDescription);
        if(chargeResult.getStatus() == PaymentStatus.FAILED) {
            log.warn("Membership charge has failed for user with ID [%s], membership type [%s]".formatted(user.getId(), membershipType));
            return chargeResult;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt;
        if(membershipPeriod == MembershipPeriod.MONTHLY) {
            expiresAt = now.plusMonths(1);
        } else {
            expiresAt = now.plusYears(1);
        }

        Membership newMembership = Membership.builder()
                .owner(user)
                .status(MembershipStatus.ACTIVE)
                .period(membershipPeriod)
                .type(membershipType)
                .price(membershipPrice)
                .renewalAllowed(membershipPeriod == MembershipPeriod.MONTHLY)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

        currentMembership.setExpiresAt(now);
        currentMembership.setStatus(MembershipStatus.INACTIVE);

        membershipRepository.save(currentMembership);
        membershipRepository.save(newMembership);

        return chargeResult;
    }

    BigDecimal getMembershipPrice(MembershipType membershipType, MembershipPeriod membershipPeriod) {
        if (membershipType == MembershipType.FREE) {
            return BigDecimal.ZERO;
        } else if (membershipType == MembershipType.PREMIUM && membershipPeriod == MembershipPeriod.MONTHLY) {
            return new BigDecimal("12.99");
        } else {
            return new BigDecimal("99.99");
        }
    }

    @Transactional
    public void updateMembershipRenewal(User user, boolean renewalAllowed, MembershipPeriod period) {

        if (user.getMemberships() == null || user.getMemberships().isEmpty()) {
            throw new IllegalStateException("User has no memberships to update");
        }

        Membership membership = user.getMemberships().get(0);

        membership.setRenewalAllowed(renewalAllowed);
        membership.setPeriod(period);

        membershipRepository.save(membership);
    }

    public List<Membership> getAllMembershipsReadyForRenewal() {
        
        return membershipRepository.findAllByStatusAndExpiresAtLessThanEqual(MembershipStatus.ACTIVE, now);
    }

    public void changeMembershipToInactive(Membership membership) {

        if (membership == null) {
            throw new IllegalArgumentException("Membership must not be null");
        }

        membership.setStatus(MembershipStatus.INACTIVE);
        membership.setExpiresAt(LocalDateTime.now());

        membershipRepository.save(membership);
    }
}
