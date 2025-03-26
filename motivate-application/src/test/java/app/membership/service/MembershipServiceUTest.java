package app.membership.service;

import app.membership.model.Membership;
import app.membership.model.MembershipPeriod;
import app.membership.model.MembershipStatus;
import app.membership.model.MembershipType;
import app.membership.repository.MembershipRepository;
import app.user.model.User;
import app.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipServiceUTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private WalletService walletService; // Unused in this method, but mocked for completeness

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void createFreeMembership_WhenUserProvided_SavesCorrectMembership() {

        User user = User.builder()
                .username("user")
                .build();
        LocalDateTime fixedTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        membershipService.now = fixedTime; // Override "now" for predictable expiresAt

        Membership expectedMembership = Membership.builder()
                .owner(user)
                .status(MembershipStatus.ACTIVE)
                .period(MembershipPeriod.MONTHLY)
                .type(MembershipType.FREE)
                .price(new BigDecimal("0.00"))
                .renewalAllowed(true)
                .createdAt(fixedTime)
                .expiresAt(fixedTime.plusMonths(1))
                .build();

        when(membershipRepository.save(any(Membership.class))).thenReturn(expectedMembership);

        membershipService.createFreeMembership(user);

        verify(membershipRepository).save(argThat(savedMembership ->
                savedMembership.getOwner().equals(user) &&
                        savedMembership.getType() == MembershipType.FREE &&
                        savedMembership.getPrice().compareTo(BigDecimal.ZERO) == 0 &&
                        savedMembership.getExpiresAt().equals(fixedTime.plusMonths(1))
        ));
    }


    @ParameterizedTest
    @MethodSource("membershipPriceProvider")
    void getMembershipPrice_ReturnsCorrectPrice(
            MembershipType type,
            MembershipPeriod period,
            BigDecimal expectedPrice
    ) {
        MembershipService service = new MembershipService(membershipRepository, walletService);

        BigDecimal actualPrice = service.getMembershipPrice(type, period);

        assertThat(actualPrice).isEqualByComparingTo(expectedPrice);
    }

    static Stream<Arguments> membershipPriceProvider() {
        return Stream.of(

                Arguments.of(MembershipType.FREE, MembershipPeriod.MONTHLY, BigDecimal.ZERO),
                Arguments.of(MembershipType.FREE, MembershipPeriod.ANNUAL, BigDecimal.ZERO),

                Arguments.of(MembershipType.PREMIUM, MembershipPeriod.MONTHLY, new BigDecimal("12.99")),

                Arguments.of(MembershipType.PREMIUM, MembershipPeriod.ANNUAL, new BigDecimal("99.99"))
        );
    }

    @Test
    void updateMembershipRenewal_WhenUserHasMembership_UpdatesAndSaves() {
        User user = User.builder()
                .username("gosho123")
                .build();
        Membership membership = new Membership();
        user.setMemberships(Collections.singletonList(membership));

        membershipService.updateMembershipRenewal(user, true, MembershipPeriod.ANNUAL);

        assertThat(membership.isRenewalAllowed()).isTrue();
        assertThat(membership.getPeriod()).isEqualTo(MembershipPeriod.ANNUAL);
        verify(membershipRepository).save(membership);
    }

    @Test
    void updateMembershipRenewal_WhenUserHasNoMembership_ThrowsException() {

        User user = User.builder()
                .username("gosho123")
                .build();
        user.setMemberships(Collections.emptyList()); // or null

        assertThatThrownBy(() ->
                membershipService.updateMembershipRenewal(user, false, MembershipPeriod.MONTHLY)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User has no memberships to update");
    }


    @Test
    void updateMembershipRenewal_WhenUserHasMultipleMemberships_UpdatesFirstOne() {

        User user = User.builder()
                .username("gosho123")
                .build();
        Membership firstMembership = new Membership();
        Membership secondMembership = new Membership();
        user.setMemberships(List.of(firstMembership, secondMembership));

        membershipService.updateMembershipRenewal(user, true, MembershipPeriod.ANNUAL);

        assertThat(firstMembership.isRenewalAllowed()).isTrue();
        verify(membershipRepository).save(firstMembership);
        verify(membershipRepository, never()).save(secondMembership);
    }

    @Test
    void changeMembershipToInactive_ValidMembership_UpdatesStatusAndExpiresAt() {

        Membership membership = new Membership();
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setExpiresAt(LocalDateTime.now().plusDays(30));


        membershipService.changeMembershipToInactive(membership);


        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.INACTIVE);
        assertThat(membership.getExpiresAt()).isBeforeOrEqualTo(LocalDateTime.now());
        verify(membershipRepository).save(membership);
    }

    @Test
    void changeMembershipToInactive_NullMembership_ThrowsException() {
        assertThatThrownBy(() -> membershipService.changeMembershipToInactive(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Membership must not be null");
    }

    @Test
    void changeMembershipToInactive_AlreadyInactive_UpdatesExpiresAtOnly() {

        Membership membership = new Membership();
        membership.setStatus(MembershipStatus.INACTIVE);
        LocalDateTime originalExpiry = LocalDateTime.now().plusDays(10);
        membership.setExpiresAt(originalExpiry);


        membershipService.changeMembershipToInactive(membership);

        
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.INACTIVE); // Unchanged
        assertThat(membership.getExpiresAt()).isBeforeOrEqualTo(LocalDateTime.now()); // Updated
        verify(membershipRepository).save(membership);
    }
}
