package burundi.treasure.service;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

public class GenerateSigature {
    public static void main(String[] args) throws ParseException {

        // Những tham số này truyền vào cho postman
        String requestDate = "20200309093002103";
        String amount = String.valueOf(convertDouble2BigDecimal2f(1000.00));
        String mobile = "68828555";
        String partner = "LOTO_BASIC";
        String requestId = "ERP0200309100001278";

        // Truyền giá trị key cua partner
        String privateKey = "805328e2e9b3892a44a76da3f309c8d7";

        String rawText = requestDate + amount + partner + mobile  + requestId;

//        2003091000012331000.00AVADA_PAY_FEE25768191919020094754344

        String inforRequest = privateKey + rawText;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(inforRequest.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter.printHexBinary(digest).toLowerCase();
        System.out.println(myHash);

    }
    public static BigDecimal convertDouble2BigDecimal2f(Double input) {
        Long inputLog;
        try {
            inputLog = Math.round(input * 100);
        } catch (NumberFormatException ex) {
            inputLog = Long.valueOf("0");
        }
        return BigDecimal.valueOf(inputLog, 2);
    }
}
