package app.post.repository;

import app.post.model.Post;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<Post> findPostById(UUID id);

    void deletePostById(UUID postId);

    List<Post> findAllByOrderByCreatedAtDesc();
}
