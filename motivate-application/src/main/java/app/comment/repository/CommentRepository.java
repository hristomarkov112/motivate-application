package app.comment.repository;

import app.comment.model.Comment;
import app.post.model.Post;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

//    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
//    List<Comment> findCommentsByPostIdOrderedByDateDesc(@Param("postId") UUID postId);

    List<Comment> findAllByPost_IdOrderByCreatedAtDesc(UUID postId);

}
