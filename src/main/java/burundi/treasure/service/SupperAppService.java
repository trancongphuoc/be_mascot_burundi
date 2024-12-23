package burundi.treasure.service;

import burundi.treasure.common.Utils;
import burundi.treasure.model.Config;
import burundi.treasure.repository.ConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SupperAppService {

    private static String DOMAIN = "https://selfcare-my.lumitel.bi/lumitel-api-v2";
    private static String VERIFY_API = "/without-bearer/mini-app/sso";
    private static String PARTNER_TOKEN = "JnxjM211mVwjhM149h0Y2OnxOQYIMKiL";
    private static String PARTNER_ID = "41";

    @Autowired
    private Utils utils;

    @Autowired
    private ConfigRepository configRepository;

    @PostConstruct
    public void init() {
//    	configRepository.save(new Config("SUPPER_APP_DOMAIN", DOMAIN));
//    	configRepository.save(new Config("SUPPER_APP_PARTNER_TOKEN", PARTNER_TOKEN));
//    	configRepository.save(new Config("SUPPER_APP_PARTNER_ID", PARTNER_ID));

        DOMAIN = configRepository.findById("SUPPER_APP_DOMAIN").map(Config::getCodeValue).orElse(DOMAIN);
        PARTNER_TOKEN = configRepository.findById("SUPPER_APP_PARTNER_TOKEN").map(Config::getCodeValue).orElse(PARTNER_TOKEN);
        PARTNER_ID = configRepository.findById("SUPPER_APP_PARTNER_ID").map(Config::getCodeValue).orElse(PARTNER_ID);
    }
    public boolean verifyToken(String msisdn, String token) throws Exception {
        String reqBody = generateRequestBodyVerify(msisdn, token);
        String res = callApi(reqBody);
        JSONObject jsonObject = new JSONObject(res);

        // Kiểm tra ResponseCode và ResponseMessage
        String responseCode = jsonObject.getString("code");
        String responseMessage = jsonObject.getString("message");
        return "200".equals(responseCode) && "Success".equals(responseMessage);
    }

    public String generateRequestBodyVerify(String msisdn, String token) throws Exception {
        Long ts = System.currentTimeMillis();
        String signature = generateSignatureVerify(msisdn, token, ts);
        // Sử dụng HashMap để chứa các tham số
        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("pid", Integer.parseInt(PARTNER_ID));
        bodyParams.put("saUser", msisdn);
        bodyParams.put("saToken", token);
        bodyParams.put("ts", ts);
        bodyParams.put("sec", signature);

        // Chuyển HashMap thành JSON String
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(bodyParams);
    }

    public String callApi(String req) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return utils.callApi(DOMAIN + VERIFY_API, "POST", req, headers);
    }

    private String generateSignatureVerify(String msisdn, String token, Long ts) throws NoSuchAlgorithmException {
        String rawText = msisdn + token + ts + PARTNER_TOKEN;
        return DigestUtils.md5Hex(rawText);
    }
}
