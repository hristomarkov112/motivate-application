package app.web.dto;

import app.post.model.Post;
import app.user.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {

    @NotNull
    private User owner;

    @NotNull
    private Post post;

    @NotBlank
    @Size(max = 4000, message = "The content must be not more than 4000 characters.")
    private String content;

    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();

}
