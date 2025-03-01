package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @Size(min = 6, message = "Username or password incorrect")
    private String username;

    @Size(min = 6, message = "Username or password incorrect")
    private String password;
}
