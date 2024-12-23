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
    private String facebookUserId;
    
//    @JsonProperty("isPremium")
    private boolean isPremium;
    
    @JsonProperty("isWin")
    private boolean isWin;

    public String getName() {
        String phone = this.phone;

        if (phone != null && phone.length() > 4) {
            return phone.substring(0, 4) + "****";
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
        this.isPremium = user.isPremium();
        this.isWin = user.isWin();
        this.facebookUserId = String.valueOf(this.id);
    }
}
