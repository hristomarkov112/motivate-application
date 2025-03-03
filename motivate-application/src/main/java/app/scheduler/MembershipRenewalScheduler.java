package app.scheduler;

import app.membership.model.Membership;
import app.membership.model.MembershipPeriod;
import app.membership.model.MembershipType;
import app.membership.service.MembershipService;
import app.payment.model.Payment;
import app.payment.model.PaymentStatus;
import app.user.model.User;
import app.web.dto.GetPremiumRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MembershipRenewalScheduler {

    private final MembershipService membershipService;

    @Autowired
    public MembershipRenewalScheduler(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @Scheduled(fixedRate = 10000000)
    public void renewMembership() {

        List<Membership> memberships = membershipService.getAllMembershipsReadyForRenewal();

        if (memberships.isEmpty()) {
            log.info("No membership plan found for renewal.");
            return;
        }

        for (Membership membership : memberships) {

            if(membership.isRenewalAllowed()) {
                User membershipOwner = membership.getOwner();
                MembershipType membershipType = membership.getType();
                MembershipPeriod membershipPeriod = membership.getPeriod();
                UUID walletId = membershipOwner.getWallets().get(0).getId();
                GetPremiumRequest getPremiumRequest = GetPremiumRequest.builder()
                        .membershipPeriod(membershipPeriod)
                        .walletId(walletId)
                        .build();

                Payment payment = membershipService.getPremium(membershipOwner, membershipType, getPremiumRequest);
                if (payment.getStatus() == PaymentStatus.FAILED) {
                    membershipService.changeMembershipToInactive(membership);
                    membershipService.createFreeMembership(membership.getOwner());
                }

            } else {
                membershipService.changeMembershipToInactive(membership);
                membershipService.createFreeMembership(membership.getOwner());
            }
        }
    }


}
