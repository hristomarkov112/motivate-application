package app.web.dto;

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
public class PostRequest {

    @NotNull
    private User owner;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 4000, message = "The content must be not more than 4000 characters.")
    private String content;

    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();
}
