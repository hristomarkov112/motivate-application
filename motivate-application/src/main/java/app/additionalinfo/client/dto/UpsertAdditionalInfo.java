package app.additionalinfo.client.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpsertAdditionalInfo {

    @NotNull
    private UUID id;

    @NotNull
    private UUID userId;

    private String gender;

    private String phoneNumber;

    private String secondEmail;
}
