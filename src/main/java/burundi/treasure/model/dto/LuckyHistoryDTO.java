package burundi.treasure.model.dto;

import java.util.Date;

import burundi.treasure.model.Gift;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LuckyHistoryDTO {
	private Date addTime;
	private Gift gift;
}
