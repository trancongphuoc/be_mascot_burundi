package burundi.treasure.payload;

import lombok.Data;

@Data
public class VerifyOTPResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String status = "OK";
    private String message = "OK";
    public VerifyOTPResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
