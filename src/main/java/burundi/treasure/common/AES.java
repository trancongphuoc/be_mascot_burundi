package burundi.treasure.common;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class AES {

	private final static int KEY_SIZE = 128;

	
	// Tạo AES Key
	public static SecretKey generateAESKey() throws Exception {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(KEY_SIZE);
		return generator.generateKey();
	}

	public static SecretKey hexKey(SecretKey key) {
		String keyHexString = byteToHex(key.getEncoded());
		key = new SecretKeySpec(hexToBytes(keyHexString),"AES");
		return key;
	}
	
	public static SecretKey hexKey(String keyHexString) {
		SecretKey key = new SecretKeySpec(hexToBytes(keyHexString),"AES");
		return key;
	}
	
	public static String encrypt(String message, SecretKey key) throws Exception {
		byte[] messageInBytes = message.getBytes();
		Cipher encryptionCipher = Cipher.getInstance("AES");		
		encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedBytes = encryptionCipher.doFinal(messageInBytes);
		return encode(encryptedBytes);
	}
	
	public static String decrypt(String encryptedMessage, SecretKey key) throws Exception {
		byte[] messageInBytes = decode(encryptedMessage);
		Cipher decryptionCipher = Cipher.getInstance("AES");		
		decryptionCipher.init(Cipher.DECRYPT_MODE, key, decryptionCipher.getParameters());
		byte[] decryptedBytes = decryptionCipher.doFinal(messageInBytes);
		return new String(decryptedBytes);
	}
	
    private static String byteToHex(final byte[] data) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; ++i) {
            int halfbyte = data[i] >>> 4 & 0xF;
            int two_halfs = 0;
            do {
                if (0 <= halfbyte && halfbyte <= 9) {
                    buf.append((char)(48 + halfbyte));
                }
                else {
                    buf.append((char)(97 + (halfbyte - 10)));
                }
                halfbyte = (data[i] & 0xF);
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
    
    private static byte[] hexToBytes(final char[] hex) {
        final int length = hex.length / 2;
        final byte[] raw = new byte[length];
        for (int i = 0; i < length; ++i) {
            final int high = Character.digit(hex[i * 2], 16);
            final int low = Character.digit(hex[i * 2 + 1], 16);
            int value = high << 4 | low;
            if (value > 127) {
                value -= 256;
            }
            raw[i] = (byte)value;
        }
        return raw;
    }
    
    private static byte[] hexToBytes(final String hex) {
        return hexToBytes(hex.toCharArray());
    }
    

	private static String encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	private static byte[] decode(String data) {
		return Base64.getDecoder().decode(data);
	}
	
	public static String keyToString(SecretKey key) {
		return byteToHex(key.getEncoded());
	}
	
}