package app.additionaInfo;

import app.additionalinfo.client.AdditionalInfoClient;
import app.additionalinfo.client.dto.AdditionalInfo;
import app.additionalinfo.client.dto.UpsertAdditionalInfo;
import app.additionalinfo.service.AdditionalInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdditionalInfoServiceUTest {

    @Mock
    private AdditionalInfoClient additionalInfoClient;

    @InjectMocks
    private AdditionalInfoService additionalInfoService;

    private UUID userId;
    private String gender;
    private String phoneNumber;
    private String secondEmail;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        gender = "Male";
        phoneNumber = "+1234567890";
        secondEmail = "test@example.com";
    }

    @Test
    void testSaveAdditionalInfo_Success() {
        // Mock successful response
        ResponseEntity<Void> successResponse = ResponseEntity.ok().build();
        when(additionalInfoClient.upsertAdditionalInfo(any(UpsertAdditionalInfo.class))).thenReturn(successResponse);

        additionalInfoService.saveAdditionalInfo(userId, gender, phoneNumber, secondEmail);

        verify(additionalInfoClient, times(1)).upsertAdditionalInfo(argThat(info ->
                info.getUserId().equals(userId) &&
                        info.getGender().equals(gender) &&
                        info.getPhoneNumber().equals(phoneNumber) &&
                        info.getSecondEmail().equals(secondEmail)
        ));
    }

    @Test
    void testSaveAdditionalInfo_Failure() {

        ResponseEntity<Void> errorResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        when(additionalInfoClient.upsertAdditionalInfo(any(UpsertAdditionalInfo.class))).thenReturn(errorResponse);

        additionalInfoService.saveAdditionalInfo(userId, gender, phoneNumber, secondEmail);

        verify(additionalInfoClient, times(1)).upsertAdditionalInfo(any(UpsertAdditionalInfo.class));
    }

    @Test
    void testGetAdditionalInfo_Success() {

        UUID userId = UUID.randomUUID();
        AdditionalInfo additionalInfo = AdditionalInfo.builder()
                .gender("MALE")
                .phoneNumber("0888565656")
                .secondEmail("test@abv.bg")
                .build();

        ResponseEntity<AdditionalInfo> successResponse = ResponseEntity.ok(additionalInfo);
        when(additionalInfoClient.getAdditionalInfo(userId)).thenReturn(successResponse);

        AdditionalInfo result = additionalInfoService.getAdditionalInfo(userId);

        assertNotNull(result);
        assertEquals(additionalInfo, result);

        verify(additionalInfoClient, times(1)).getAdditionalInfo(userId);
    }

    @Test
    void testGetAdditionalInfo_Failure() {

        ResponseEntity<AdditionalInfo> errorResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        when(additionalInfoClient.getAdditionalInfo(userId)).thenReturn(errorResponse);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> additionalInfoService.getAdditionalInfo(userId));

        assertEquals("Additional info for user id [%s] does not exist.".formatted(userId), exception.getMessage());

        verify(additionalInfoClient, times(1)).getAdditionalInfo(userId);
    }
}
