package burundi.treasure.payload;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class VerifyOTPRequestMPS {

	@NotBlank
	private String otp;
}
