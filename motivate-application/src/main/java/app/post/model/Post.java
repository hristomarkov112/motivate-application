package app.post.model;

import app.comment.model.Comment;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private UUID postId;

    @ManyToOne
    private User owner;

    @Column(nullable = false)
    private String username;

    private String profilePicture;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "postId")
    private List<Comment> comments;

    @Column(nullable = false)
    private boolean showCommentInput;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int commentCount;
}
