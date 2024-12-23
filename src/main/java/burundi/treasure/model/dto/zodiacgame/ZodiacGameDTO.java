package burundi.treasure.model.dto.zodiacgame;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
public class ZodiacGameDTO implements Serializable {

    private Long id;

    private Date addTime;
    private Long totalIcoinWin;
    private Long totalIcoinBetting;
    private String status; // NEW, PROCESSING, PROCESSED

    private String zodiacCardId; // Kết quả

    private ZodiacCardDTO zodiacCard;

    private Long noGame;
}
