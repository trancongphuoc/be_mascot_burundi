package burundi.treasure.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import burundi.treasure.common.AES;
import burundi.treasure.common.RSA;
import burundi.treasure.common.Utils;
import burundi.treasure.config.ConfigProperties;
import burundi.treasure.model.MPSRequest;
import burundi.treasure.model.User;
import burundi.treasure.repository.MPSRequestRepository;

import lombok.extern.log4j.Log4j2;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@Log4j2
public class MPSService {

	@Autowired
	private ConfigProperties properties;

	@Autowired
	private MPSRequestRepository mpsRequestRepository;

	@Autowired
	private Utils utils;

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS) // Connection timeout
            .writeTimeout(5, TimeUnit.SECONDS) // Write timeout
            .readTimeout(5, TimeUnit.SECONDS) // Read timeout
            .build();


	public final String REGISTER = "REGISTER";
	public final String CANCEL = "CANCEL";
	public final String RENEW = "RENEW";
	public final String PLAYONETIME = "PLAYONETIME";


	public final String responseFailed = "<?xml version=\"1.0\"?>\r\n"
			+ "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" >\r\n" + "<S:Body>\r\n"
			+ "<subRequestResponse xmlns=\"http://contentws/xsd\">\r\n" + "<return>1</return>\r\n"
			+ "</subRequestResponse>\r\n" + "</S:Body>\r\n" + "</S:Envelope>";
	public final String responseSusccessfully = "<?xml version=\"1.0\"?>\r\n"
			+ "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" >\r\n" + "<S:Body>\r\n"
			+ "<subRequestResponse xmlns=\"http://contentws/xsd\">\r\n" + "<return>0</return>\r\n"
			+ "</subRequestResponse>\r\n" + "</S:Body>\r\n" + "</S:Envelope>";

	private String makeParametersSmsws(String msisdn, String content) {
		String parameters = "<?xml version=\"1.0\"  encoding=\"utf-8\" ?>"
				+ "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+ "<S:Body>"
				+ "<smsRequest xmlns=\"http://smsws/xsd\">"
				+ "<username>" + properties.getSmswsUsername() + "</username>"
				+ "<password>" + properties.getSmswsPassword() + "</password>"
				+ "<shortcode>" + properties.getSmswsShortCode() + "</shortcode>"
				+ "<msisdn>" + msisdn + "</msisdn>"
				+ "<params>" + "" + "</params>"
				+ "<content>" + content + "</content>"
				+ "<transid>" + utils.generateTransactionID() + "</transid>"
				+ "</smsRequest>"
				+ "</S:Body>"
				+ "</S:Envelope>";

		log.info("1. -----------> Parameters smsws: " + parameters);
		return parameters;
	}

	private String makeParametersSmsUssd(String msisdn, String content) {
		String parameters = "<?xml version=\"1.0\"  encoding=\"utf-8\" ?>"
				+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://ussd/xsd\">"
				+ "<soapenv:Header/>"
				+ "<soapenv:Body>"
				+ "<xsd:ussdRequest>"
				+ "<xsd:ussdgwId>" + properties.getSmsUssdId() + "</xsd:ussdgwId>"
				+ "<xsd:ussdgwUsername>" + properties.getSmsUssdUsername() + "</xsd:ussdgwUsername> "
				+ "<xsd:ussdgwPassword>" + properties.getSmsUssdPassword() + "</xsd:ussdgwPassword>"
				+ "<xsd:msisdn>" + msisdn + "</xsd:msisdn> "
				+ "<xsd:content>" + content + "</xsd:content>"
				+ "<xsd:type>201</xsd:type>"
				+ "</xsd:ussdRequest>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";

		log.info("1. -----------> Parameters smsUssd: " + parameters);
		return parameters;
	}

	// msisdn: số điện thoại
	// cmd: Register, Cancel
	private String makeParametersSendOTP(String msisdn, String cmd, String transactionId) throws Exception {
		log.info("Start encrypt request send otp");
		StringBuilder parameters = new StringBuilder();
		parameters.append("PRO=" + properties.getPro());
		parameters.append("&SER=" + properties.getSer());
		parameters.append("&SUB=" + properties.getSubSer());
		parameters.append("&CMD=" + cmd); // REGISTER, CANCEL
		SecretKey key = AES.hexKey(AES.generateAESKey());

		InputStream streamPublicKeyVT = this.getClass().getResourceAsStream(properties.getPublicKeyVTPath());
		InputStream streamPublicKeyCP = this.getClass().getResourceAsStream(properties.getPublicKeyCPPath());
		InputStream streamPrivateKeyCP = this.getClass().getResourceAsStream(properties.getPrivateKeyCPPath());

		PublicKey publicKeyVT = RSA.readPublicKey(streamPublicKeyVT);
		PublicKey publicKeyCP = RSA.readPublicKey(streamPublicKeyCP);
		PrivateKey privateKeyCP = RSA.readPrivateKey(streamPrivateKeyCP);

		StringBuilder data = new StringBuilder();
		data.append("SUB=" + properties.getSubSer());
		data.append("&CATE=" + "BLANK");
		data.append("&ITEM=" + null);
		data.append("&SUB_CP=" + null);
		data.append("&CONT=" + null);
		data.append("&PRICE=" + 0);
		data.append("&REQ=" + transactionId);
		data.append("&MOBILE=" + msisdn);
		data.append("&SOURCE=" + "CLIENT");
		data.append("&OTP=" + null);
		data.append("&OTP_TYPE=" + 0);
		log.info("1. -----------> Input data: " + data);

		String encryptAESData = "value=" + AES.encrypt(data.toString(), key) + "&key=" + AES.keyToString(key);
		log.info("2. -----------> Encrypt AES for data and add AES key: " + encryptAESData);

		String encryptRSAData = RSA.encrypt(encryptAESData, publicKeyVT);
		log.info("3. -----------> Encrypt RSA for data use PublicKeyVT: " + encryptRSAData);

		String signature = RSA.createSignature(encryptRSAData, privateKeyCP);
		log.info("4. -----------> Create signature use PrivateKeyCP: " + signature);

		boolean verify = RSA.verifySignature(encryptRSAData, signature, publicKeyCP);
		log.info("5. -----------> Verify signature use PublicKeyCP: " + verify);

		parameters.append("&DATA=" + URLEncoder.encode(encryptRSAData))
				.append("&SIG=" + URLEncoder.encode(signature));
		log.info("6. -----------> Parameters: " + parameters.toString());

		return parameters.toString();
	}

	// msisdn: số điện thoại
	// cmd: Register, Cancel
	// otp
	private String makeParametersVerifyOTP(String msisdn, String cmd, String otp, String req) throws Exception {
		log.info("Start encrypt request verify otp");
		StringBuilder parameters = new StringBuilder();
		parameters.append("PRO=" + properties.getPro());
		parameters.append("&SER=" + properties.getSer());
		parameters.append("&SUB=" + properties.getSubSer());
		parameters.append("&CMD=" + cmd); // REGISTER, CANCEL
		SecretKey key = AES.hexKey(AES.generateAESKey());

		InputStream streamPublicKeyVT = this.getClass().getResourceAsStream(properties.getPublicKeyVTPath());
		InputStream streamPublicKeyCP = this.getClass().getResourceAsStream(properties.getPublicKeyCPPath());
		InputStream streamPrivateKeyCP = this.getClass().getResourceAsStream(properties.getPrivateKeyCPPath());

		PublicKey publicKeyVT = RSA.readPublicKey(streamPublicKeyVT);
		PublicKey publicKeyCP = RSA.readPublicKey(streamPublicKeyCP);
		PrivateKey privateKeyCP = RSA.readPrivateKey(streamPrivateKeyCP);

		StringBuilder data = new StringBuilder();
		data.append("SUB=" + properties.getSubSer());
		data.append("&CATE=" + "BLANK");
		data.append("&ITEM=" + null);
		data.append("&SUB_CP=" + null);
		data.append("&CONT=" + null);
		data.append("&PRICE=" + 0);
		data.append("&REQ=" + req);
		data.append("&MOBILE=" + msisdn);
		data.append("&SOURCE=" + "CLIENT");
		data.append("&OTP=" + otp);
		data.append("&OTP_TYPE=" + 1);
		log.info("1. -----------> Input data: " + data);

		String encryptAESData = "value=" + AES.encrypt(data.toString(), key) + "&key=" + AES.keyToString(key);
		log.info("2. -----------> Encrypt AES for data and add AES key: " + encryptAESData);

		String encryptRSAData = RSA.encrypt(encryptAESData, publicKeyVT);
		log.info("3. -----------> Encrypt RSA for data use PublicKeyVT: " + encryptRSAData);

		String signature = RSA.createSignature(encryptRSAData, privateKeyCP);
		log.info("4. -----------> Create signature use PrivateKeyCP: " + signature);

		boolean verify = RSA.verifySignature(encryptRSAData, signature, publicKeyCP);
		log.info("5. -----------> Verify signature use PublicKeyCP: " + verify);

		parameters.append("&DATA=" + URLEncoder.encode(encryptRSAData))
				.append("&SIG=" + URLEncoder.encode(signature));
		log.info("6. -----------> Parameters: " + parameters.toString());

		return parameters.toString();
	}


	// msisdn: số điện thoại
	// cmd: Register, Cancel
	private String makeParametersCharge(String msisdn,String command, String cate) throws Exception {
		log.info("Start encrypt request charge");
		StringBuilder parameters = new StringBuilder();
		parameters.append("PRO=" + properties.getPro());
		parameters.append("&SER=" + properties.getSer());
		parameters.append("&SUB=" + properties.getSubSer());
		parameters.append("&CMD=" + command); //CHARGE,MODBALANCE
		SecretKey key = AES.hexKey(AES.generateAESKey());

		InputStream streamPublicKeyVT = this.getClass().getResourceAsStream(properties.getPublicKeyVTPath());
		InputStream streamPublicKeyCP = this.getClass().getResourceAsStream(properties.getPublicKeyCPPath());
		InputStream streamPrivateKeyCP = this.getClass().getResourceAsStream(properties.getPrivateKeyCPPath());

		PublicKey publicKeyVT = RSA.readPublicKey(streamPublicKeyVT);
		PublicKey publicKeyCP = RSA.readPublicKey(streamPublicKeyCP);
		PrivateKey privateKeyCP = RSA.readPrivateKey(streamPrivateKeyCP);

		StringBuilder data = new StringBuilder();
		data.append("SUB=" + properties.getSubSer());
		data.append("&CATE=" + cate.toUpperCase());
		data.append("&ITEM=" + null);
		data.append("&SUB_CP=" + null);
		data.append("&CONT=" + null);
		data.append("&PRICE=" + 0);
		data.append("&REQ=" + utils.generateTransactionID());
		data.append("&MOBILE=" + msisdn);
		data.append("&SOURCE=" + "CLIENT");
		data.append("&OTP=" + null);
		data.append("&OTP_TYPE=" + 0);
		log.info("1. -----------> Input data: " + data);

		String encryptAESData = "value=" + AES.encrypt(data.toString(), key) + "&key=" + AES.keyToString(key);
		log.info("2. -----------> Encrypt AES for data and add AES key: " + encryptAESData);

		String encryptRSAData = RSA.encrypt(encryptAESData, publicKeyVT);
		log.info("3. -----------> Encrypt RSA for data use PublicKeyVT: " + encryptRSAData);

		String signature = RSA.createSignature(encryptRSAData, privateKeyCP);
		log.info("4. -----------> Create signature use PrivateKeyCP: " + signature);

		boolean verify = RSA.verifySignature(encryptRSAData, signature, publicKeyCP);
		log.info("5. -----------> Verify signature use PublicKeyCP: " + verify);

		parameters.append("&DATA=" + URLEncoder.encode(encryptRSAData))
				.append("&SIG=" + URLEncoder.encode(signature));
		log.info("6. -----------> Parameters: " + parameters.toString());

		return parameters.toString();
	}

	// From XML to TEXT
	private String convertResponseSmsws(String xmlString) throws Exception {
		log.info("Start convert response smsws XML -> TEXT");
		log.info("XML: " + xmlString);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
		String response = doc.getElementsByTagName("return").item(0).getTextContent();
		log.info("TEXT: " + response);
		return response;

	}


	public Map<String, String> convertResponseOTP(String sParameters) {
		log.info("Start convert response otp");
		log.info("Before: " + sParameters);
		Map<String, String> parameters = new HashMap<String, String>();

		String[] arrParameters = sParameters.split("&");
		for (String s : arrParameters) {
			parameters.put(s.split("=", 2)[0], s.split("=", 2)[1]);
		}
		log.info("After: " + parameters);

		return parameters;
	}


	public String decryptResponseOTP(String response) throws Exception {
		log.info("Start decrypt response otp");
		log.info("1. -----------> Response: " + response);

		InputStream streamPublicKeyVT = this.getClass().getResourceAsStream(properties.getPublicKeyVTPath());
		InputStream streamPrivateKeyCP = this.getClass().getResourceAsStream(properties.getPrivateKeyCPPath());

		PublicKey publicKeyVT = RSA.readPublicKey(streamPublicKeyVT);
		PrivateKey privateKeyCP = RSA.readPrivateKey(streamPrivateKeyCP);

		String data = response.split("&")[0].split("=", 2)[1];
		String signature = response.split("&")[1].split("=", 2)[1];

		response = RSA.decrypt(data, privateKeyCP);
		log.info("2. -----------> Decrypt RSA for response use PrivateKeyCP: " + response);

		boolean verify = RSA.verifySignature(data, URLDecoder.decode(signature), publicKeyVT);
		log.info("3. -----------> Verify signature use PublicKeyVT: " + verify);

		String value = response.split("&")[0].split("=", 2)[1];
		String keyHexString = response.split("&")[1].split("=", 2)[1];
		SecretKey key = AES.hexKey(keyHexString);
		response = AES.decrypt(value, key);
		log.info("4. -----------> Decrypt AES for response: " + response);

		return response;
	}

	public String decryptResponseCharge(String response) throws Exception {
		log.info("Start decrypt response charge");
		log.info("1. -----------> Response: " + response);

		InputStream streamPublicKeyVT = this.getClass().getResourceAsStream(properties.getPublicKeyVTPath());
		InputStream streamPrivateKeyCP = this.getClass().getResourceAsStream(properties.getPrivateKeyCPPath());

		PublicKey publicKeyVT = RSA.readPublicKey(streamPublicKeyVT);
		PrivateKey privateKeyCP = RSA.readPrivateKey(streamPrivateKeyCP);

		String data = response.split("&")[0].split("=", 2)[1];
		String signature = response.split("&")[1].split("=", 2)[1];

		response = RSA.decrypt(data, privateKeyCP);
		log.info("2. -----------> Decrypt RSA for response use PrivateKeyCP: " + response);

		boolean verify = RSA.verifySignature(data, URLDecoder.decode(signature), publicKeyVT);
		log.info("3. -----------> Verify signature use PublicKeyVT: " + verify);

		String value = response.split("&")[0].split("=", 2)[1];
		String keyHexString = response.split("&")[1].split("=", 2)[1];
		SecretKey key = AES.hexKey(keyHexString);
		response = AES.decrypt(value, key);
		log.info("4. -----------> Decrypt AES for response: " + response);

		return response;
	}


	public String callApiSmsws(String msisdn, String content) throws Exception{
		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "text/xml; charset=utf-8");
			String parameters = makeParametersSmsws(msisdn, content);

			String response = utils.callApi(properties.getSmswsUrl(), "POST", parameters, headers);
			return convertResponseSmsws(response);
		} catch (Exception e) {
			log.warn(e);
		}
		return "";
	}

	public String callApiSmsUssd(String msisdn, String content) throws Exception {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "text/xml; charset=utf-8");
			String parameters = makeParametersSmsUssd(msisdn, content);

			String response = utils.callApi(properties.getSmsUssdUrl(), "POST", parameters, headers);
			return convertResponseSmsws(response);
		} catch (Exception e) {
			log.warn(e);
		}
		return "";
	}

	public void callApiSmswsAsync(String msisdn, String content) throws Exception{
		try {
            System.out.println("callApiSmswsAsync start");

			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "text/xml; charset=utf-8");
			String parameters = makeParametersSmsws(msisdn, content);

			// Create request body
            RequestBody body = RequestBody.create(parameters, MediaType.parse("text/xml; charset=utf-8"));

            // Build the request
            Request.Builder requestBuilder = new Request.Builder()
                    .url(properties.getSmswsUrl())
                    .post(body);

            // Add headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }

            Request request = requestBuilder.build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.warn("Request failed: " + e.getMessage());

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {

                    } else {

                    }
                    response.close();
                }
            });

            System.out.println("callApiSmswsAsync stop, doing other work...");
		} catch (Exception e) {
			log.warn(e);
		}
	}


	public String callApiSendOtp(String msisdn, String cmd, String transactionId) throws Exception{
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/xml; charset=utf-8");
		String parameters = makeParametersSendOTP(msisdn, cmd, transactionId);
		String url = properties.getOtpUrl() + parameters;
		String response = utils.callApi(url, "GET", null, null);
		response = decryptResponseOTP(response);
		return response;
	}

	public String callApiSendOtp(String msisdn, String cmd, String transactionId, MPSRequest mpsRequest) throws Exception{
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/xml; charset=utf-8");
		String parameters = makeParametersSendOTP(msisdn, cmd, transactionId);
		String url = properties.getOtpUrl() + parameters;
		String response = utils.callApi(url, "GET", null, null);

		try {
			mpsRequest.setChargeRequest(parameters);
			mpsRequest.setChargeResponse(response);
		} catch (Exception e) {
			log.warn(e);
		}

		response = decryptResponseOTP(response);
		return response;
	}


	public String callApiVerifyOtp(String msisdn, String cmd, String otp, String req) throws Exception{
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/xml; charset=utf-8");
		String parameters = makeParametersVerifyOTP(msisdn, cmd, otp, req);
		String url = properties.getOtpUrl()+ parameters;
		String response = utils.callApi(url, "GET", null, null);
		response = decryptResponseOTP(response);
		return response;
	}

	public String callApiCharge(String msisdn, String command, String cate) throws Exception{
		String parameters = makeParametersCharge(msisdn, command, cate);

		String response = utils.callApi(properties.getChargeUrl() + parameters, "GET", null, null);
		response = decryptResponseCharge(response);
		return response;
	}

	public String callApiCharge(String msisdn, String command, String cate, MPSRequest mpsRequest) throws Exception{
		String parameters = makeParametersCharge(msisdn, command, cate);

		String response = utils.callApi(properties.getChargeUrl() + parameters, "GET", null, null);

		try {
			mpsRequest.setChargeRequest(parameters);
			mpsRequest.setChargeResponse(response);
		} catch (Exception e) {
			log.warn(e);
		}

		response = decryptResponseCharge(response);
		return response;
	}

	public String callApiNatcom(String token) throws Exception{
		String param = "token=" + token;

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Content-Length", Integer.toString(param.getBytes().length));

//		String response = utils.callApi(properties.getNatcomUrl(), "POST", param, headers);
		String response = null;
		return response;
	}

	public MPSRequest newMPSRequest(User user, String action) {
		MPSRequest mpsRequest = new MPSRequest();
		mpsRequest.setAction(action);
		if("CANCEL_WEB".equals(action)) {
			mpsRequest.setAmount(0);
			mpsRequest.setParams("1");
		} else {
			mpsRequest.setAmount(0);
			mpsRequest.setParams("0");
		}
		mpsRequest.setMsisdn(user.getPhone());
		mpsRequest.setChargetTime(new Date());
		mpsRequest.setStatus("PENDING");
		mpsRequest.setTransId(user.getLastTransactionId());
		mpsRequest.setUser(user);

		return mpsRequestRepository.save(mpsRequest);
	}

	public MPSRequest newMPSRequestSupperApp(User user, String action, String transactionId) {
		MPSRequest mpsRequest = new MPSRequest();
		mpsRequest.setAction(action);
		if("CANCEL_SP".equals(action)) {
			mpsRequest.setAmount(0);
			mpsRequest.setParams("1");
		} else {
			mpsRequest.setAmount(0);
			mpsRequest.setParams("0");
		}
		mpsRequest.setMsisdn(user.getPhone());
		mpsRequest.setChargetTime(new Date());
		mpsRequest.setStatus("NONE");
		mpsRequest.setTransId(transactionId);
		mpsRequest.setUser(user);

		return mpsRequestRepository.save(mpsRequest);
	}

	public MPSRequest save(MPSRequest mpsRequest) {
		return mpsRequestRepository.save(mpsRequest);
	}


	public MPSRequest add(MPSRequest mpsRequest) {
		return mpsRequestRepository.save(mpsRequest);
	}

	public List<MPSRequest> findAllMPSRequest() {
		return mpsRequestRepository.findAll();
	}

	public void deleteAll() {
		mpsRequestRepository.deleteAll();
	}

	public long sumAmountWithin24Hours(Date startTime) {
		return mpsRequestRepository.sumAmountWithin24Hours(startTime);
	}

	public void deleteByActionAndParamsAndChargetTimeBefore(String action, String params, Date date) {
		mpsRequestRepository.deleteByActionAndParamsAndChargetTimeBefore(action, params, date);
	}

	public List<Object[]> groupByDateAndSumAmountByDateRange(Date startDate, Date endDate, String params, List<String> actions, String phone) {
		return mpsRequestRepository.groupByDateAndSumAmountByDateRange(startDate, endDate, params, actions, phone);
	}

	public List<Object[]> groupByDateAndCountRecordByDateRange(Date startDate, Date endDate, String params, List<String> actions, String phone) {
		return mpsRequestRepository.groupByDateAndCountRecordByDateRange(startDate, endDate, params, actions, phone);
	}

	public List<Object[]> groupByUserPhoneAndSumAmountByDateRange(Date startDate, Date endDate, String params, List<String> actions, String phone, Pageable pageable) {
		return mpsRequestRepository.groupByUserPhoneAndSumAmountByDateRange(startDate, endDate, params, actions, phone, pageable);
	}

	public Long sumAmountByDateRange(Date startDate, Date endDate, String params, List<String> actions, String phone) {
		Long res = mpsRequestRepository.sumAmountByDateRange(startDate, endDate, params, actions, phone);
		return res != null ? res : 0;
	}

	public void deleteByChargetTimeBefore(Date date) {
		mpsRequestRepository.deleteByChargetTimeBefore(date);
	}


	public Page<MPSRequest> findAllByChargetTimeBetweenAndMsisdnContaining(Date startDate, Date endDate, String phone, Pageable pageable) {

		Long amoutCondition = -1L;
		if (startDate != null && endDate != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(endDate);
			calendar.add(Calendar.DATE, 1);
			// Cả hai điều kiện đều được thỏa mãn
			return mpsRequestRepository.findAllByChargetTimeBetweenAndMsisdnContaining(startDate, calendar.getTime(), phone, pageable);
		} else if (startDate != null) {
			// Chỉ điều kiện startDate được thỏa mãn
			return mpsRequestRepository.findAllByChargetTimeAfterAndMsisdnContaining(startDate, phone, pageable);
		} else if (endDate != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(endDate);
			calendar.add(Calendar.DATE, 1);
			// Chỉ điều kiện endDate được thỏa mãn
			return mpsRequestRepository.findAllByChargetTimeBeforeAndMsisdnContaining(calendar.getTime(), phone, pageable);
		} else {
			// Không có điều kiện tìm kiếm về thời gian
			return mpsRequestRepository.findAllByMsisdnContainingOrderByChargetTimeDesc(phone, pageable);
		}

	}
}
