package burundi.treasure.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetPaymentUrlRequest {
    @NotBlank
    private String msisdn;

    @NotNull
    private String action; // REGISTER, CHARGE
}
