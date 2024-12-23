package burundi.treasure.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


import burundi.treasure.common.Utils;
import burundi.treasure.model.Config;
import burundi.treasure.repository.ConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Administrator
 */
@Log4j2
@Service
public class AnypayService {
    @Autowired
    private Utils utils;
    
    @Autowired
    private ConfigRepository configRepository;
    
    private PaywayService sec = PaywayService.getInstance();
    
    private static String USERNAME = "389b5d6b726d5584";
    private static String PASSWORD = "c7a94786ec10526d0eef71859b1ebf31";
    private static final String WSCODE = "saleAnypayCustomerV3";
    private static String P_USERNAME = "C58A21BBC90FD5A2";
    private static String P_PASSWORD = "2D734BF6D40F1CB4E774B8471B3995F4";
    private static String SOURCE_MSISDN = "65654877";
	private static String ICCID = "8925708001282919114";
	private static String MPIN = "F9743F2A674BF0D3";
	private static String URL = "http://10.225.6.73:8128/BCCSGateway?wsdl";

    public static final String KEY_SUCCESSFULL_AUTHEN = "<error>0</error>";
    public static final String KEY_SUCCESSFULL_EXECUTE = "<responseCode>0000</responseCode>";
    
    public static final String KEY_UNSUCCESSFULL_NOT_ENOUGH_EXECUTE = "<responseCode>13</responseCode>";
    public static final String KEY_UNSUCCESSFULL_NOT_ENOUGH_MESSAGE = "Unsuccessful! You do not have enough money in anypay account";

    public static final String KEY_UNSUCCESSFULL_IP_INCORRECT_MESSAGE = "The ip is incorrect";

    static String XML_REQUEST = 
    		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservice.bccsgw.viettel.com/\">\n"
            + "		<soapenv:Header/>\n"
            + "		<soapenv:Body>\n"
            + "			<web:gwOperation>\n"
            + "				<Input>\n"
            + "					<username>%s</username>\n"
            + "					<password>%s</password>\n"
            + "					<wscode>%s</wscode>\n"
            + "					<param name=\"username\" value=\"%s\"/>\n"
            + "					<param name=\"password\" value=\"%s\"/>\n"
            + "					<param name=\"data\" value=\"%s\"/>\n"
            + "					<rawData>?</rawData>\n"
            + "				</Input>\n"
            + "			</web:gwOperation>\n"
            + "		</soapenv:Body>\n"
            + "</soapenv:Envelope>";

    
    @PostConstruct
    public void init() {
//    	configRepository.save(new Config("ANYPAY_USERNAME", USERNAME));
//    	configRepository.save(new Config("ANYPAY_PASSWORD", PASSWORD));
//    	configRepository.save(new Config("ANYPAY_PARAM_USERNAME", P_USERNAME));
//    	configRepository.save(new Config("ANYPAY_PARAM_PASSWORD", P_PASSWORD));
//    	configRepository.save(new Config("ANYPAY_SOURCE_MSISDN", SOURCE_MSISDN));
//    	configRepository.save(new Config("ANYPAY_ICCID", ICCID));
//    	configRepository.save(new Config("ANYPAY_MPIN", MPIN));
//    	configRepository.save(new Config("ANYPAY_URL", URL));

    	USERNAME = configRepository.findById("ANYPAY_USERNAME").map(Config::getCodeValue).orElse(USERNAME);
    	PASSWORD = configRepository.findById("ANYPAY_PASSWORD").map(Config::getCodeValue).orElse(PASSWORD);
    	P_USERNAME = configRepository.findById("ANYPAY_PARAM_USERNAME").map(Config::getCodeValue).orElse(P_USERNAME);
    	P_PASSWORD = configRepository.findById("ANYPAY_PARAM_PASSWORD").map(Config::getCodeValue).orElse(P_PASSWORD);
    	SOURCE_MSISDN = configRepository.findById("ANYPAY_SOURCE_MSISDN").map(Config::getCodeValue).orElse(SOURCE_MSISDN);
    	ICCID = configRepository.findById("ANYPAY_ICCID").map(Config::getCodeValue).orElse(ICCID);
    	MPIN = configRepository.findById("ANYPAY_MPIN").map(Config::getCodeValue).orElse(MPIN);
    	URL = configRepository.findById("ANYPAY_URL").map(Config::getCodeValue).orElse(URL);

    }
    
    public String generateAnypayRequest(String msisdn, long money) {
    	String msisdnRemovePrefix = utils.removePrefixPhoneNumber(msisdn);
    	String moneyEncrypt = sec.Encrypt(String.valueOf(money));
    	String data = String.format("%s|%s|%s|%s|%s",SOURCE_MSISDN, ICCID, MPIN, msisdnRemovePrefix, moneyEncrypt);
        String req = String.format(XML_REQUEST, USERNAME, PASSWORD, WSCODE, P_USERNAME, P_PASSWORD, data);
        
        return req;
    }
    
    public String callWsClient(String req) {
//        Map<String, String> headers = new HashMap<String, String>();
//		headers.put("Content-Type", "text/xml; charset=utf-8");
//
//		String response = utils.callApi(URL, "POST", req, headers);
//        return response;
        return null;
    }
    
    
    public String callWsClient(String msisdn, long money) {
    	String msisdnRemovePrefix = utils.removePrefixPhoneNumber(msisdn);
    	String moneyEncrypt = sec.Encrypt(String.valueOf(money));
    	String data = String.format("%s|%s|%s|%s|%s",SOURCE_MSISDN, ICCID, MPIN, msisdnRemovePrefix, moneyEncrypt);
        String req = String.format(XML_REQUEST, USERNAME, PASSWORD, WSCODE, P_USERNAME, P_PASSWORD, data);
        
        Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "text/xml; charset=utf-8");
		
		String response = utils.callApi(URL, "POST", req, headers);
        return response;
    }
    
    
    
    // Test function
    
    static Map<String, String> html_specialchars_table = new Hashtable<String, String>();

    static {
        html_specialchars_table.put("&lt;", "<");
        html_specialchars_table.put("&gt;", ">");
        html_specialchars_table.put("&amp;", "&");
        html_specialchars_table.put("&quot;", "\"");
        html_specialchars_table.put("&apos;", "'");
    }

    private String htmlspecialcharsDecode(String s) {
        Set<String> en = html_specialchars_table.keySet();
        for (String key : en) {
            String val = html_specialchars_table.get(key);
            s = s.replaceAll(key, val);
        }
        return s;
    }
    
    
    private String callWsClient() {
        String username = "fdsafdklsgjfdjskglfds";
        String password = "jhgfjgy5ytghfrghf";
        String wscode = "getAccountInfo";
        String p_username = "8DSFSDFDSKFJS";
        String p_password = "5KLJFDSFSD";
        String data = "61002422|8925708000289021520|F9743F2A674BF0D3";
        String req = String.format(XML_REQUEST, username, password, wscode, p_username, p_password, data);
        log.info(req);
        StringBuffer line = new StringBuffer();
        try {

            //Create socket
            String hostname = "10.225.6.73";
            int port = 8128;
            InetAddress addr = InetAddress.getByName(hostname);
            Socket sock = new Socket(addr, port);
            sock.setSoTimeout(5 * 60 * 1000);
            //Send header
            String path = "/BCCSGateway?wsdl";
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"));
            // You can use "UTF8" for compatibility with the Microsoft virtual machine.
            wr.write("POST " + path + " HTTP/1.0\r\n");
            wr.write("Host: " + hostname + "\r\n");
            wr.write("Content-Length: " + req.length() + "\r\n");
            wr.write("Content-Type: text/xml; charset=\"utf-8\"\r\n");
            wr.write("\r\n");

            //Send data
            wr.write(req);
            wr.flush();
            // Response
            BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String strtemp = "";
            while ((strtemp = rd.readLine()) != null) {
                line.append(strtemp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String rs = line.toString();
        rs = htmlspecialcharsDecode(rs);
        log.info(rs);
        return rs;
    }

    private void main(String arg[]) {
        try {
            String KEY_SUCCESSFULL_AUTHEN = "<error>0</error>";
            String KEY_SUCCESSFULL_EXECUTE = "<responseCode>0000</responseCode>";
            int retry = 0;
            while (retry < 10) {
                String rs = callWsClient();
                //This case is success
                if (rs.contains(KEY_SUCCESSFULL_AUTHEN) && rs.contains(KEY_SUCCESSFULL_EXECUTE)) {
                    return;//Success and Finish call webservice
                } else { //in case of fail
                    retry++;
                    Thread.sleep(2 * 1000); //try again in 2 seconds
                }
            }
        } catch (Exception ex) {
            
        }

    }



}

