package app.membership.repository;

import app.membership.model.Membership;
import app.membership.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    Optional<Membership> findByStatusAndOwnerId(MembershipStatus status, UUID ownerId);

    List<Membership> findAllByStatusAndExpiresAtLessThanEqual(MembershipStatus status, LocalDateTime now);
}
