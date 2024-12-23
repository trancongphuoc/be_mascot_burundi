package burundi.treasure.model.zodiacgame;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "mc_zodiac_card")
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ZodiacCard {

    @Id
    private String id;
    private String name;
    private Long multiply; // x3. Ví dụ đặt cược 1 icoin sẽ ăn 3 icoin
    private Double probability; // Xác xuất
    private String imageUrl;
}
