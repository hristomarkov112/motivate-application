package app.additionalinfo.service;

import app.additionalinfo.client.AdditionalInfoClient;
import app.additionalinfo.client.dto.AdditionalInfo;
import app.additionalinfo.client.dto.UpsertAdditionalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AdditionalInfoService {

    private final AdditionalInfoClient additionalInfoClient;

    @Autowired
    public AdditionalInfoService(AdditionalInfoClient additionalInfoClient) {
        this.additionalInfoClient = additionalInfoClient;
    }

    public void saveAdditionalInfo(UUID userId, String gender, String phoneNumber, String secondEmail) {

        UpsertAdditionalInfo additionalInfo = UpsertAdditionalInfo.builder()
                .userId(userId)
                .gender(gender)
                .phoneNumber(phoneNumber)
                .secondEmail(secondEmail)
                .build();

        ResponseEntity<Void> httpResponse = additionalInfoClient.upsertAdditionalInfo(additionalInfo);
        if(!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Error saving additional info with id = [%s]. Feign call to additional-info-svc failed".formatted(userId));
        }
    }

    public AdditionalInfo getAdditionalInfo(UUID userId) {
        ResponseEntity<AdditionalInfo> httpResponse = additionalInfoClient.getAdditionalInfo(userId);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Additional info for user id [%s] does not exist.".formatted(userId));
        }

        return httpResponse.getBody();
    }
}
