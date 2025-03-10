package app.post.model;

import app.comment.model.Comment;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User owner;

    @Column(nullable = false)
    private String username;

    private String profilePicture;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private boolean showCommentInput;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int commentCount;
}
