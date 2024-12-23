package burundi.treasure.payload;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data

public class SendOTPRequest {

	@NotBlank
	private String phone;
}
