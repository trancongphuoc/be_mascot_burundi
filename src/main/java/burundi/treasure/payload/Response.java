package burundi.treasure.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Response {
	public String status;
	public String message;
	private Map<String, Object> data;

	public Response(String status, String message) {
		this.status = status;
		this.message = message;
	}
}
