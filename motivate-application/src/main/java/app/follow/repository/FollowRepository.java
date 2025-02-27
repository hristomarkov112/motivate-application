package app.follow.repository;

import app.follow.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    List<Follow> findByFollowerId(UUID followerId);

    List<Follow> findByFollowingId(UUID followingId);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

}
