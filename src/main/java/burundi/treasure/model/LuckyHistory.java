package burundi.treasure.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Entity
@Table(name = "mc_history")
public class LuckyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String giftId;

    @Column(nullable = false)
    private Long noItem;

    private Long noWin;

    private Long noGame;

//    @ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

//    @JsonFormat(pattern = "dd/MM/yyyy")
    @Temporal(TemporalType.TIMESTAMP)
    private Date addTime;

    private Boolean isShare;
    
    private String giftType;
    private String giftBetting;
    private String giftResult;

    private Long money;

    private String status;

    @Column(length = 4000)
    private String lumicashRequest;

    @Column(length = 4000)
    private String lumicashResponse;
}
