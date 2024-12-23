package burundi.treasure.payload;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ChargeRequestMPS {

	@NotBlank
	private String cate;
}
