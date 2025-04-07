package burundi.treasure.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Configuration
@PropertySource("classpath:config.properties")
@Data
public class ConfigProperties {

	@Value( "${pro}" )
	private String pro;

	@Value( "${ser}" )
	private String ser;

	@Value( "${sub_ser}" )
	private String subSer;

	@Value( "${public_key_vt_path}" )
	private String publicKeyVTPath;

	@Value( "${public_key_cp_path}" )
	private String publicKeyCPPath;

	@Value( "${private_key_cp_path}" )
	private String privateKeyCPPath;

	@Value( "${otp_url}" )
	private String otpUrl;

	@Value( "${charge_url}" )
	private String chargeUrl;

	@Value( "${smsws_url}" )
	private String smswsUrl;

	@Value( "${smsws_username}" )
	private String smswsUsername;

	@Value( "${smsws_password}" )
	private String smswsPassword;

	@Value( "${smsws_short_code}" )
	private String smswsShortCode;

	@Value( "${sms_ussd_url}" )
	private String smsUssdUrl;
	@Value( "${sms_ussd_id}" )
	private String smsUssdId;
	@Value( "${sms_ussd_username}" )
	private String smsUssdUsername;
	@Value( "${sms_ussd_password}" )
	private String smsUssdPassword;

	@Value( "#{${probabilities}}" )
	private Map<String, Double> probabilities = new HashMap<>();

}
