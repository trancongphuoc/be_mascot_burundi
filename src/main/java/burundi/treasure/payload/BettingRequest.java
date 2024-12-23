package burundi.treasure.payload;

import burundi.treasure.model.dto.UserDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacCardDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacGameDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
public class BettingRequest {
    private Long totalIcoin;
    private String zodiacCardId;
    private Long zodiacGameId;
}
