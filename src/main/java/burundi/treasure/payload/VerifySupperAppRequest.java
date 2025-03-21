package burundi.treasure.payload;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class VerifySupperAppRequest {

	@NotBlank
	private String token;

	@NotBlank
	private String msisdn;
}
