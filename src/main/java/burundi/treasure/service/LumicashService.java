package burundi.treasure.service;

import burundi.treasure.common.Utils;
import burundi.treasure.model.Config;
import burundi.treasure.repository.ConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class LumicashService {
    @Autowired
    private Utils utils;

    @Autowired
    private ConfigRepository configRepository;
    private static String USERNAME = "W_MASCOT_PRIZE";
    private static String PASSWORD = "MASCOTPay_1234%^&*";
    private static String KEY = "ab50fc6f7245a7913ac7aca1f2ecd0ca";
    private static String PARTNER_CODE = "W_MASCOT_PRIZE";
    private static String URL = "http://10.225.5.73:8020/ewapi/api/3rd/customer/transaction/payonbehalf";

    //    private static String USERNAME = "LOTO_BASIC";
//    private static String PASSWORD = "1qazXSW@";
//    private static String KEY = "805328e2e9b3892a44a76da3f309c8d7";
//    private static String PARTNER_CODE = "LOTO_BASIC";
//    private static String URL = "http://154.73.105.33:9020/ewapi/api/3rd/customer/transaction/payonbehalf";
    private static final String CONTENT = "Pay for mascot service";
    @PostConstruct
    public void init() {
//    	configRepository.save(new Config("LUMICASH_USERNAME", USERNAME));
//    	configRepository.save(new Config("LUMICASH_PASSWORD", PASSWORD));
//    	configRepository.save(new Config("LUMICASH_KEY", KEY));
//    	configRepository.save(new Config("LUMICASH_PARTNER_CODE", PARTNER_CODE));
//    	configRepository.save(new Config("LUMICASH_URL", URL));

        USERNAME = configRepository.findById("LUMICASH_USERNAME").map(Config::getCodeValue).orElse(USERNAME);
        PASSWORD = configRepository.findById("LUMICASH_PASSWORD").map(Config::getCodeValue).orElse(PASSWORD);
        KEY = configRepository.findById("LUMICASH_KEY").map(Config::getCodeValue).orElse(KEY);
        PARTNER_CODE = configRepository.findById("LUMICASH_PARTNER_CODE").map(Config::getCodeValue).orElse(PARTNER_CODE);
        URL = configRepository.findById("LUMICASH_URL").map(Config::getCodeValue).orElse(URL);
    }

    private String generateSignature(String mobile, double amount, String requestDate, String requestId) throws NoSuchAlgorithmException {
        // Chuyển đổi amount thành BigDecimal 2 chữ số thập phân
        String amountStr = String.valueOf(convertDouble2BigDecimal2f(amount));

        // Ghép các chuỗi để tạo rawText
        String rawText = requestDate + amountStr + PARTNER_CODE + mobile + requestId;
        String inforRequest = KEY + rawText;

        // Sử dụng MD5 để băm thông tin
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(inforRequest.getBytes());
        byte[] digest = md.digest();

        // Trả về chuỗi hexa của Signature
        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }

    // Hàm để tạo JSON body
    public String generateRequestBody(String desMobile, double transAmount) throws Exception {

        String requestDate = getCurrentRequestDate();
        String requestId = PARTNER_CODE + requestDate;
        String signature = generateSignature(desMobile, transAmount, requestDate, requestId);
        // Sử dụng HashMap để chứa các tham số
        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("RequestId", requestId);
        bodyParams.put("RequestDate", requestDate);
        bodyParams.put("PartnerCode", PARTNER_CODE);
        bodyParams.put("DesMobile", desMobile);
        bodyParams.put("TransAmount", transAmount);
        bodyParams.put("Content", CONTENT);
        bodyParams.put("Description", "");
        bodyParams.put("Signature", signature);

        // Chuyển HashMap thành JSON String
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(bodyParams);
    }

    private BigDecimal convertDouble2BigDecimal2f(Double input) {
        Long inputLog;
        try {
            inputLog = Math.round(input * 100);
        } catch (NumberFormatException ex) {
            inputLog = Long.valueOf("0");
        }
        return BigDecimal.valueOf(inputLog, 2);
    }

    private String getCurrentRequestDate() {
        // Định dạng ngày tháng theo yêu cầu
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(new Date());
    }

    public String callWsClient(String req) {
        String auth = USERNAME + ":" + PASSWORD;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + encodedAuth);

        return utils.callApi(URL, "POST", req, headers);
    }

    public String callWsClient(String msisdn, Long money) throws Exception {
        String req = generateRequestBody(msisdn, money.doubleValue());
        String auth = USERNAME + ":" + PASSWORD;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Basic " + encodedAuth);

        return utils.callApi(URL, "POST", req, headers);
    }

    public boolean isPayOk(String response) {
        // Phân tích chuỗi JSON
        JSONObject jsonObject = new JSONObject(response);

        // Kiểm tra ResponseCode và ResponseMessage
        String responseCode = jsonObject.getString("ResponseCode");
        String responseMessage = jsonObject.getString("ResponseMessage");

        return "01".equals(responseCode) && "SUCCESS".equals(responseMessage);
    }
}
