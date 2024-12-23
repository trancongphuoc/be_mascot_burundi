package burundi.treasure.model.dto.zodiacgame;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@NoArgsConstructor
public class ZodiacCardDTO implements Serializable {

    private String id;

    private String name;

    private Long multiply; // x3. Ví dụ đặt cược 1 icoin sẽ ăn 3 icoin

    private String imageUrl;

    private Long totalIcoinBetting; // Số tiền đặt cược

    // Dùng để sắp sếp list kết quả gần đây
    private Long lastUpdate;
}
