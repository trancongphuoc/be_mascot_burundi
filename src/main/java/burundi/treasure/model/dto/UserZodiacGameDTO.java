package burundi.treasure.model.dto;

import burundi.treasure.model.User;
import burundi.treasure.model.dto.zodiacgame.ZodiacCardDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class UserZodiacGameDTO extends UserDTO{
    private Long noBettingToday;
    private Long totalIcoinWinToday;
    private List<ZodiacCardDTO> zodiacCards;

    public UserZodiacGameDTO(User user) {
        super(user);
    }
}
