package burundi.treasure.service;

import burundi.treasure.common.Utils;
import burundi.treasure.model.Config;
import burundi.treasure.payload.GetPaymentUrlResponse;
import burundi.treasure.repository.ConfigRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SupperAppService {

//    private static String DOMAIN = "https://selfcare-my.lumitel.bi/lumitel-api-v2";
    private static String DOMAIN = "http://10.225.10.103:8089/lumitel-api-v2";
    private static String VERIFY_API = "/without-bearer/mini-app/sso";
    private static String CANCEL_API = "/charging-service/cancel";
    private static String GET_PAYMENT_URL = "/charging-service/get-payment-url";
//    private static String PARTNER_TOKEN = "JnxjM211mVwjhM149h0Y2OnxOQYIMKiL";
    private static String PARTNER_TOKEN = "HMA9VhGTir9F1usahunETcd80TfIeJye";
    private static String PARTNER_ID = "41";
    private static String PACKAGE_REGISTER = "93";
    private static String PACKAGE_CHARGE = "94";
    private static String APP_ID = "10108";

    private static String TYPE = "0";  // 0: Thanh toán qua ví, 1: Thanh toán qua tài khoản viễn thông

    @Autowired
    private Utils utils;

    @Autowired
    private ConfigRepository configRepository;

    @PostConstruct
    public void init() {
//    	configRepository.save(new Config("SUPPER_APP_DOMAIN", DOMAIN));
//    	configRepository.save(new Config("SUPPER_APP_PARTNER_TOKEN", PARTNER_TOKEN));
//    	configRepository.save(new Config("SUPPER_APP_PARTNER_ID", PARTNER_ID));

//    	configRepository.save(new Config("SUPPER_APP_PACKAGE_REGISTER", PACKAGE_REGISTER));
//    	configRepository.save(new Config("SUPPER_APP_PACKAGE_CHARGE", PACKAGE_CHARGE));
//    	configRepository.save(new Config("SUPPER_APP_APP_ID", APP_ID));
//    	configRepository.save(new Config("SUPPER_APP_TYPE", TYPE));

        DOMAIN = configRepository.findById("SUPPER_APP_DOMAIN").map(Config::getCodeValue).orElse(DOMAIN);
        PARTNER_TOKEN = configRepository.findById("SUPPER_APP_PARTNER_TOKEN").map(Config::getCodeValue).orElse(PARTNER_TOKEN);
        PARTNER_ID = configRepository.findById("SUPPER_APP_PARTNER_ID").map(Config::getCodeValue).orElse(PARTNER_ID);

        PACKAGE_REGISTER = configRepository.findById("SUPPER_APP_PACKAGE_REGISTER").map(Config::getCodeValue).orElse(PACKAGE_REGISTER);
        PACKAGE_CHARGE = configRepository.findById("SUPPER_APP_PACKAGE_CHARGE").map(Config::getCodeValue).orElse(PACKAGE_CHARGE);
        APP_ID = configRepository.findById("SUPPER_APP_APP_ID").map(Config::getCodeValue).orElse(APP_ID);
        TYPE = configRepository.findById("SUPPER_APP_TYPE").map(Config::getCodeValue).orElse(TYPE);

    }
    public boolean verifyToken(String msisdn, String token) throws Exception {
        String reqBody = generateRequestBodyVerify(msisdn, token);
        String res = callApiVerify(reqBody);
        JSONObject jsonObject = new JSONObject(res);

        // Kiểm tra ResponseCode và ResponseMessage
        String responseCode = jsonObject.getString("code");
        String responseMessage = jsonObject.getString("message");

        return "200".equals(responseCode) && "Success".equals(responseMessage);
    }


    public GetPaymentUrlResponse getPaymentUrl(String msisdn, String action, String saToken) throws Exception {
        String transactionId = utils.generateTransactionID();
        if(!msisdn.startsWith("+")) {
            msisdn = "+" + msisdn;
        }
        String reqBody = generateRequestBodyGetPaymentUrl(msisdn, action, transactionId);
        String res = callApiGetPaymentUrl(reqBody, saToken);
        JSONObject jsonObject = new JSONObject(res);

        // Kiểm tra ResponseCode và ResponseMessage
        GetPaymentUrlResponse getPaymentUrlResponse = new GetPaymentUrlResponse();
        getPaymentUrlResponse.setTransactionId(transactionId);
        getPaymentUrlResponse.setCode(jsonObject.getString("code"));
        getPaymentUrlResponse.setMessage(jsonObject.getString("message"));
        if(jsonObject.has("data")) {
            getPaymentUrlResponse.setData(jsonObject.getString("data"));
        }
        return getPaymentUrlResponse;
    }

    public boolean cancel(String msisdn, String transactionId) throws Exception {
        String reqBody = generateRequestBodyCancel(msisdn, transactionId);
        String res = callApiCancel(reqBody);
        JSONObject jsonObject = new JSONObject(res);

        // Kiểm tra ResponseCode và ResponseMessage
        String responseCode = jsonObject.getString("code");
        String responseMessage = jsonObject.getString("message");

        return "200".equals(responseCode) && "Success".equals(responseMessage);
    }

    public String generateRequestBodyGetPaymentUrl(String msisdn, String action, String transactionId) throws Exception {

        String _package = action.equals("CHARGE") ? PACKAGE_CHARGE : PACKAGE_REGISTER;
        Long amount = action.equals("CHARGE") ? 100L : 120L;
        Long ts = System.currentTimeMillis();
        String signature = generateSignatureGetPaymentUrl(msisdn, transactionId, _package, amount, ts);
        // Sử dụng HashMap để chứa các tham số
        Map<String, Object> bodyParams = new HashMap<>();
        if(msisdn.startsWith("+")) {
            bodyParams.put("saUser", URLEncoder.encode(msisdn, StandardCharsets.UTF_8));
        } else {
            bodyParams.put("saUser", "%2B" + msisdn);
        }
        bodyParams.put("partnerId", PARTNER_ID);
        bodyParams.put("appId", APP_ID);
        bodyParams.put("transaction", transactionId);
        bodyParams.put("type", TYPE);
        bodyParams.put("package", _package);
        bodyParams.put("amount", amount);
        bodyParams.put("ts", ts);
        bodyParams.put("signature", signature);
        if(action.equals("CANCEL")) {
            bodyParams.put("action", "cancel");
        } else {
            bodyParams.put("action", "charge");
        }
        // Chuyển HashMap thành JSON String
//        ObjectMapper objectMapper = new ObjectMapper();
//        return objectMapper.writeValueAsString(bodyParams);

        StringBuilder sb = new StringBuilder();
        bodyParams.forEach((k, v) -> {
            sb.append(k).append("=").append(bodyParams.get(k)).append("&");
        });

        return sb.toString();
    }

    public String generateRequestBodyVerify(String msisdn, String token) throws Exception {
        Long ts = System.currentTimeMillis();
        String signature = generateSignatureVerify(msisdn, token, ts);
        // Sử dụng HashMap để chứa các tham số
        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("pid", PARTNER_ID);
        if(msisdn.startsWith("+")) {
            bodyParams.put("saUser", URLEncoder.encode(msisdn, StandardCharsets.UTF_8));
        } else {
            bodyParams.put("saUser", "%2B" + msisdn);
        }
        bodyParams.put("saToken", token);
        bodyParams.put("ts", ts);
        bodyParams.put("sec", signature);

        // Chuyển HashMap thành JSON String
//        ObjectMapper objectMapper = new ObjectMapper();
//        return objectMapper.writeValueAsString(bodyParams);

        StringBuilder sb = new StringBuilder();
        bodyParams.forEach((k, v) -> {
            sb.append(k).append("=").append(bodyParams.get(k)).append("&");
        });

        return sb.toString();
    }

    public String generateRequestBodyCancel(String msisdn, String transactionId) throws Exception {
        Long ts = System.currentTimeMillis();
        String signature = generateSignatureCancel(msisdn, transactionId, ts);
        // Sử dụng HashMap để chứa các tham số
        Map<String, Object> bodyParams = new HashMap<>();
        if(msisdn.startsWith("+")) {
            bodyParams.put("saUser", URLEncoder.encode(msisdn, StandardCharsets.UTF_8));
        } else {
            bodyParams.put("saUser", "%2B" + msisdn);
        }
        bodyParams.put("partnerId", PARTNER_ID);
        bodyParams.put("transaction", transactionId);
        bodyParams.put("package", PACKAGE_REGISTER);
        bodyParams.put("ts", ts);
        bodyParams.put("signature", signature);

        // Chuyển HashMap thành JSON String
//        ObjectMapper objectMapper = new ObjectMapper();
//        return objectMapper.writeValueAsString(bodyParams);

        StringBuilder sb = new StringBuilder();
        bodyParams.forEach((k, v) -> {
            sb.append(k).append("=").append(bodyParams.get(k)).append("&");
        });

        return sb.toString();
    }

    public String callApiVerify(String req) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return utils.callApi(DOMAIN + VERIFY_API + "?" + req, "POST", null, headers);
    }

    public String callApiGetPaymentUrl(String req, String saToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + saToken);
        return utils.callApi(DOMAIN + GET_PAYMENT_URL + "?" + req, "POST", null, headers);
    }

    public String callApiCancel(String req) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return utils.callApi(DOMAIN + CANCEL_API + "?" + req, "POST", null, headers);
    }

    private String generateSignatureVerify(String msisdn, String token, Long ts) {
        String rawText = msisdn + token + ts + PARTNER_TOKEN;
        return DigestUtils.md5Hex(rawText);
    }

    private String generateSignatureCancel(String msisdn, String transactionId, Long ts) {
        String rawText = msisdn + PARTNER_ID + transactionId + PACKAGE_REGISTER + ts + PARTNER_TOKEN;
        return DigestUtils.md5Hex(rawText);
    }

    private String generateSignatureGetPaymentUrl(String msisdn, String transactionId, String _package, Long amount, Long ts) {
        String rawText = msisdn + PARTNER_ID + APP_ID + transactionId + TYPE + _package + amount + ts + PARTNER_TOKEN;
        return DigestUtils.md5Hex(rawText);
    }

    public String decrypt(String encryptedData) throws Exception {
        // Chuyển key thành dạng byte (phải đủ 32 byte)
        byte[] keyBytes = PARTNER_TOKEN.getBytes(StandardCharsets.UTF_8);

        // Tạo SecretKeySpec
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Khởi tạo Cipher với chế độ AES/ECB/PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Giải mã dữ liệu
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        String msisdn = "+25766700003";
        System.out.println(msisdn);
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIrMjU3NjY3MDAwMDMiLCJkb21haW4iOiJ0cmVhc3VyZS5sdW1pdGVsLmJpIiwicGlkIjo0MSwiZXhwIjoxNzQxMzYwMjI5LCJ1c2VyaWQiOiIrMjU3NjY3MDAwMDMifQ.iVvpez1gMvWX_WaHBgeZVOz4g5QLuf1aCxCA4gYeMvo";
//        Long ts = System.currentTimeMillis();
        Long ts = 1741359931418L;
        System.out.println(ts);
        String rawText = msisdn + token + ts + PARTNER_TOKEN;
        System.out.println(rawText);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(rawText.getBytes());
        byte[] digest = md.digest();
        String md5 = DigestUtils.md5Hex(rawText);
        System.out.println(md5);

        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }
        System.out.println(hexString.toString());
    }
}
