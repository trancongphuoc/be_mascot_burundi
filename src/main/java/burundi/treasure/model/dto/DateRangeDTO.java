package burundi.treasure.model.dto;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DateRangeDTO {
	@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

	private Integer rowsPerPage;

	private String msisdn;}
