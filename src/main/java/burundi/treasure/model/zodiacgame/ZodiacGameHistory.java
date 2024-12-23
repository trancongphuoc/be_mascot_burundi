package burundi.treasure.model.zodiacgame;

import burundi.treasure.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "mc_zodiac_game_history", indexes = {
        @Index(name = "idx_addtime", columnList = "addTime"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_addtime_user_id", columnList = "addTime, user_id"),
        @Index(name = "idx_zodiac_game_id", columnList = "zodiac_game_id")
})
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ZodiacGameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date addTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "zodiac_game_id", nullable = false)
    private ZodiacGame zodiacGame;

    @ManyToOne
    @JoinColumn(name = "zodiac_card_id", nullable = false)
    private ZodiacCard zodiacCard;

    private Long totalIcoinBetting;
    private Long totalIcoinWin;

    private String status; // NEW, PENDING, BETTING_FAILED, WIN, LOSE, ERROR
}
