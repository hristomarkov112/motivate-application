package app.additionalinfo.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpsertAdditionalInfo {

    private UUID userId;

    private String gender;

    private String phoneNumber;

    private String secondEmail;
}
