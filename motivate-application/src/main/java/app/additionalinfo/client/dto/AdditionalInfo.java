package app.additionalinfo.client.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AdditionalInfo {

    private String gender;

    private String phoneNumber;

    private String secondEmail;
}
