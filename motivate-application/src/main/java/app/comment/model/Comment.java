package app.comment.model;

import app.post.model.Post;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User owner;

    @ManyToOne
    private Post post;

    @Column(nullable = false)
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();


}
