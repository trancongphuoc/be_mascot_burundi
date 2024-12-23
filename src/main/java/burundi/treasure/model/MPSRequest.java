package burundi.treasure.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.persistence.*;

import org.json.JSONObject;
import org.json.XML;

import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Data
@ToString
@Entity
@Table(name = "mc_request")
@Log4j2
public class MPSRequest {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	private String transId; //KEY (transaction ID)
	private String username;
	private String password;
	private String serviceId;
	private String msisdn; //phone
    @Temporal(TemporalType.TIMESTAMP)
	private Date chargetTime; // time register
	private String params; //0: Subscribe or Renew, 1: Unsubscribe, 2: pending, 3: Restore service

	@Column(name = "mps_mode")
	private String mode; //REAL
	private Integer amount; // $/1
	private String command; //ON OFF renew = null
	private String status; //PENDING //PROCESSED //FAILED
	private String action; // REGISTER CANCEL RENEW
	@Column(length = 4000)
	private String chargeRequest;

	@Column(length = 4000)
	private String chargeResponse;
//	@ManyToOne(cascade = CascadeType.REMOVE)
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	public static MPSRequest parseRequestMPS(String xml, String action) {
//		log.info("Action: " + action);
//		log.info("Data: " + xml);
		
		try {
			MPSRequest requestMPS = new MPSRequest();
			JSONObject xmlJSONObj = XML.toJSONObject(xml);
			JSONObject requestJSONObj = null;
			JSONObject bodyJSONObj = xmlJSONObj.getJSONObject("soapenv:Envelope").getJSONObject("soapenv:Body");

			if (bodyJSONObj.has("soap:resultRequest")) {
				requestJSONObj = bodyJSONObj.getJSONObject("soap:resultRequest");
			} else {
				requestJSONObj = bodyJSONObj.getJSONObject("soap:subRequest");
			}
			
//			log.info("Json: " + requestJSONObj);
			String phone = String.valueOf(requestJSONObj.get("msisdn"));
			requestMPS.msisdn = formatPhoneBurundi(phone);

			requestMPS.transId = requestJSONObj.getString("transid");

			requestMPS.command = requestJSONObj.has("command") ? String.valueOf(requestJSONObj.get("command")) : "NONE";

			requestMPS.action = action;

			requestMPS.status = "PENDING";

			if(requestJSONObj.has("username"))
				requestMPS.username = requestJSONObj.getString("username");

			if(requestJSONObj.has("password"))
				requestMPS.password = requestJSONObj.getString("password");

			if(requestJSONObj.has("serviceid"))
				requestMPS.serviceId = requestJSONObj.getString("serviceid");

			if(requestJSONObj.has("params"))
				requestMPS.params = String.valueOf(requestJSONObj.get("params"));

			if(requestJSONObj.has("mode"))
				requestMPS.mode = requestJSONObj.getString("mode");

			if(requestJSONObj.has("amount")) {
				try {
					requestMPS.amount = requestJSONObj.getInt("amount");
				} catch (Exception e) {
					log.warn("Amount is not integer: " + requestJSONObj.get("amount"));
					requestMPS.amount = 0;
				}
			}

			if(requestJSONObj.has("chargetime")) {
				try {
					String chargeTimeString = String.valueOf(requestJSONObj.get("chargetime"));
					Date chargetTime = new Date();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
					chargetTime = df.parse(chargeTimeString);
					requestMPS.chargetTime = chargetTime;
				} catch (Exception e) {
					log.warn("Amount is not date: " + requestJSONObj.get("chargetime"));
					requestMPS.chargetTime = new Date();
				}

			}

			if("check".equals(requestMPS.mode) || "CHECK".equals(requestMPS.mode)) {
				requestMPS.transId += "_CHECK";
			}
			return requestMPS;
		} catch (Exception e) {
			log.warn("BUGS", e);
			return null;
		}
	}

	private static String formatPhoneBurundi(String phoneNumber) {
		phoneNumber = phoneNumber.trim();
		if (phoneNumber.startsWith("257")) {
			phoneNumber = "+" + phoneNumber;
		} else if (phoneNumber.startsWith("0")) {
			phoneNumber = "+257" + phoneNumber.substring(1);
		} else if (!phoneNumber.startsWith("+257")) {
			phoneNumber = "+257" + phoneNumber;
		}

		return phoneNumber.replace("+", "");
	}
}
