package burundi.treasure.model.dto.zodiacgame;


import burundi.treasure.model.dto.UserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@NoArgsConstructor
public class ZodiacGameUserHistoryDTO {

    private Long id;

    private Date addTime;

    private Long totalIcoin;

    private String status;

    private Long facebookUserId;

    private UserDTO user;

    private String zodiacCardId;

    private ZodiacCardDTO zodiacCard;

    private Long zodiacGameId;

    private ZodiacGameDTO zodiacGame;
}

