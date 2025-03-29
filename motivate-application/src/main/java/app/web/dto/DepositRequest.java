package app.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositRequest {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "10.00", message = "Deposit must be at least 10.00")
    BigDecimal amount;
}
