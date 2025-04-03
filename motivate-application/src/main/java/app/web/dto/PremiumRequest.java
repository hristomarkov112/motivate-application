package app.web.dto;

import app.membership.model.MembershipPeriod;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PremiumRequest {

    private MembershipPeriod membershipPeriod;

    private UUID walletId;
}
