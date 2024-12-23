package burundi.treasure.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class VerifyOTPRequest {
    @NotBlank
    private String phone;

    @NotBlank
    private String otp;
}
