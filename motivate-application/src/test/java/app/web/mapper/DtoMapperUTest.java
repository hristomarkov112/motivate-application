package app.web.mapper;


import app.user.model.User;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class DtoMapperUTest {

    @Test
    void mapUserToEditRequest_WithCompleteUser_ReturnsCorrectRequest() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .profilePictureUrl("https://example.com/profile.jpg")
                .bio("Software Developer")
                .build();

        UserEditRequest result = DtoMapper.mapUserToEditRequest(user);

        assertThat(result)
                .isNotNull()
                .satisfies(req -> {
                    assertThat(req.getEmail()).isEqualTo("test@example.com");
                    assertThat(req.getFirstName()).isEqualTo("John");
                    assertThat(req.getLastName()).isEqualTo("Doe");
                    assertThat(req.getProfilePicture()).isEqualTo("https://example.com/profile.jpg");
                    assertThat(req.getBio()).isEqualTo("Software Developer");
                });
    }

    @Test
    void mapUserToEditRequest_WithNullValues_ReturnsRequestWithNulls() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(null)
                .firstName(null)
                .lastName(null)
                .profilePictureUrl(null)
                .bio(null)
                .build();

        UserEditRequest result = DtoMapper.mapUserToEditRequest(user);

        assertThat(result)
                .isNotNull()
                .satisfies(req -> {
                    assertThat(req.getEmail()).isNull();
                    assertThat(req.getFirstName()).isNull();
                    assertThat(req.getLastName()).isNull();
                    assertThat(req.getProfilePicture()).isNull();
                    assertThat(req.getBio()).isNull();
                });
    }

    @Test
    void mapUserToEditRequest_WithEmptyStrings_ReturnsRequestWithEmptyStrings() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("")
                .firstName("")
                .lastName("")
                .profilePictureUrl("")
                .bio("")
                .build();

        UserEditRequest result = DtoMapper.mapUserToEditRequest(user);

        assertThat(result)
                .isNotNull()
                .satisfies(req -> {
                    assertThat(req.getEmail()).isEmpty();
                    assertThat(req.getFirstName()).isEmpty();
                    assertThat(req.getLastName()).isEmpty();
                    assertThat(req.getProfilePicture()).isEmpty();
                    assertThat(req.getBio()).isEmpty();
                });
    }

    @Test
    void mapUserToEditRequest_WithPartialData_ReturnsPartialRequest() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("partial@example.com")
                .firstName(null)
                .lastName("Smith")
                .profilePictureUrl(null)
                .bio("Only partial data")
                .build();

        UserEditRequest result = DtoMapper.mapUserToEditRequest(user);

        assertThat(result)
                .isNotNull()
                .satisfies(req -> {
                    assertThat(req.getEmail()).isEqualTo("partial@example.com");
                    assertThat(req.getFirstName()).isNull();
                    assertThat(req.getLastName()).isEqualTo("Smith");
                    assertThat(req.getProfilePicture()).isNull();
                    assertThat(req.getBio()).isEqualTo("Only partial data");
                });
    }

    @Test
    void mapUserToEditRequest_WithNullUser_ThrowsException() {
        assertThatThrownBy(() -> DtoMapper.mapUserToEditRequest(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("User must not be null");
    }
}
