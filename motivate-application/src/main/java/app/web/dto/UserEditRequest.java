package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
public class UserEditRequest {

    @URL(message = "URL must be a correct format.")
    private String profilePicture;

    @Size(max = 20, message = "First name must not be more than 20 characters.")
    private String firstName;

    @Size(max = 20, message = "Last name must not be more than 20 characters.")
    private String lastName;

    @Size(max = 1000, message = "Biography must not be more than 1000 characters.")
    private String bio;

    @Email(message = "Email must be a correct format.")
    private String email;


}
