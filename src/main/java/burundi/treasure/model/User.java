package burundi.treasure.model;

import java.util.Date;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "mc_users", indexes = {
        @Index(name = "idx_phone", columnList = "phone"),
        @Index(name = "idx_total_star", columnList = "totalStar"),
        @Index(name = "idx_total_star_month", columnList = "totalStarMonth")
        })
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String phone;
    
    private String otp;
    
    private String lastTransactionId;
    private String lastAction; // REGISTER_SP, CHARGE_SP
    
    @Column(name = "expired_otp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiredOTP;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date addTime;

    private Long totalPlay;
    
    private Long totalWin;

    private Long totalStar;
    private Long totalStarMonth;

//    @JsonProperty("isPremium")
    private boolean isPremium;
    private Boolean isPremiumSupperApp;
    
    @JsonProperty("isWin")
    private boolean isWin;
    
    private String fromUser; //WEB, NATCOM, WS
    private Boolean firstRegister;
    
//    @Column(name = "last_send_sms_cdr")
//    @Temporal(TemporalType.TIMESTAMP)
    @Transient
    private Date lastSendSmsCDR;
}
