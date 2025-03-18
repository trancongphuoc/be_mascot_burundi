package burundi.treasure.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import burundi.treasure.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
	
    private Long id;
    private String phone;
    private Date addTime;
    private long totalIcoin;
    private long totalStar;
    private long totalStarMonth;
    private String facebookUserId;
    
//    @JsonProperty("isPremium")
    private boolean isPremium;
    private boolean isPremiumSupperApp;
    
    @JsonProperty("isWin")
    private boolean isWin;

    public String getName() {
        String phone = this.phone;

        if(phone != null && phone.startsWith("257") && phone.length() >= 11) {
            phone = phone.replace("257", "");
            phone = phone.substring(0, phone.length() - 3) + "***";
        } else if (phone != null && phone.length() >= 4){
            phone = phone.substring(0, phone.length() - 3) + "***";
        }

        return phone;
    }

    public String getIdString() {
        return String.valueOf(id);
    }

    public String getProfileImageLink() {
        return String.format("https://picsum.photos/seed/%s/576/576", this.id);
    }
    
    public UserDTO(User user) {
        this.id = user.getId();
        this.phone = user.getPhone();
        this.addTime = user.getAddTime();
        this.totalIcoin = user.getTotalPlay();
        this.totalStar = user.getTotalStar();
        this.totalStarMonth = user.getTotalStarMonth();
        this.isPremium = user.isPremium();
        this.isWin = user.isWin();
        this.isPremiumSupperApp = user.getIsPremiumSupperApp() != null ? user.getIsPremiumSupperApp() : false;
        this.facebookUserId = String.valueOf(this.id);
    }
}
