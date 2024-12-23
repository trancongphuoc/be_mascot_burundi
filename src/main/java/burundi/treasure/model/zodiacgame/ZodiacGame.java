package burundi.treasure.model.zodiacgame;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "mc_zodiac_game", indexes = {
        @Index(name = "idx_status", columnList = "status")
})
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ZodiacGame {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date addTime;

    @ManyToOne
    @JoinColumn(name = "zodiac_card_id")
    private ZodiacCard zodiacCard;

    private Long totalIcoinWin;
    private Long totalIcoinBetting;
    private String status; // NEW, PROCESSING, PROCESSED
    private Long noGame;
}
