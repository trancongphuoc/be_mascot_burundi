package burundi.treasure.model.dto.zodiacgame;

import burundi.treasure.model.dto.UserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class ZodiacGameUserDTO {
    private String id; //zodiacGameId#fbUserId
    private Date addTime;
    private Long facebookUserId;
    private UserDTO user;
    private Long zodiacGameId;
    private ZodiacGameDTO zodiacGame;
    private List<String> zodiacCardIds; // các thẻ bài mà user đặt cược
    private List<ZodiacCardDTO> zodiacCards;
    private String zodiacCardId; // kết quả ván cược
    private ZodiacCardDTO zodiacCard;
    private Long totalIcoinBetting; // Tổng số tiền đặt cược
    private Long totalIcoinWin; // Tổng số tiền thắng
    private Long noGame;
}
