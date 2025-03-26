package app.web.mapper;

import app.user.model.User;
import app.web.dto.UserEditRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static UserEditRequest mapUserToEditRequest(User user) {

        if (user == null) {
            throw new NullPointerException("User must not be null");
        }

        return UserEditRequest.builder()
                .profilePicture(user.getProfilePictureUrl())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .email(user.getEmail())
                .build();
    }
}
