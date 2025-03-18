package burundi.treasure.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPaymentUrlResponse {
    private String code;
    private String message;
    private String data;
    private String transactionId;
}
