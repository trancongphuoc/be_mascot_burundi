package burundi.treasure.common;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.springframework.stereotype.Service;

/**
 * @author Anass AIT BEN EL ARBI
 *         <ul>
 *         <li>AES/CBC/NoPadding (128)</li>
 *         <li>AES/CBC/PKCS5Padding (128)</li>
 *         <li>AES/ECB/NoPadding (128)</li>
 *         <li>AES/ECB/PKCS5Padding (128)</li>
 *         <li>RSA/ECB/PKCS1Padding (1024, 2048)</li>
 *         <li>RSA/ECB/OAEPWithSHA-1AndMGF1Padding (1024, 2048)</li>
 *         <li>RSA/ECB/OAEPWithSHA-256AndMGF1Padding (1024, 2048)</li>
 *         </ul>
 *         <p>
 *         for more details @see <a href=
 *         "https://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html">Java
 *         Ciphers</a>
 */

@Service
public class RSA {
	
	public static final String PUBLIC_KEY_CP = "C:\\IKARA\\JavaServiceWrapperVaaa\\pemfile\\PublicKeyCP.pem";
	public static final String PRIVATE_KEY_CP = "C:\\IKARA\\JavaServiceWrapperVaaa\\pemfile\\PrivateKeyCP.pem";
	public static final String PUBLIC_KEY_VT = "C:\\IKARA\\JavaServiceWrapperVaaa\\pemfile\\PublicKeyVT.pem";

	public static String encrypt(String content, PublicKey publicKey) throws Exception {
		byte[] messageToBytes = content.getBytes();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(messageToBytes);
		return encode(encryptedBytes);
	}
	
	
	public static String decrypt(String encryptedContent, PrivateKey privateKey) throws Exception {
		byte[] encryptedBytes = decode(encryptedContent);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
		return new String(decryptedMessage, "UTF8");
	}
	
	public static String createSignature(String plainText, PrivateKey privateKey) throws Exception {		
	    Signature privateSignature = Signature.getInstance("SHA1withRSA");
	    privateSignature.initSign(privateKey);
	    privateSignature.update(plainText.getBytes());

	    byte[] signature = privateSignature.sign();

	    return Base64.getEncoder().encodeToString(signature);
	}
	
	public static boolean verifySignature(String plainText, String signature, PublicKey publicKey) throws Exception {		
	    Signature publicSignature = Signature.getInstance("SHA1withRSA");
	    publicSignature.initVerify(publicKey);
	    publicSignature.update(plainText.getBytes());

	    byte[] signatureBytes = Base64.getDecoder().decode(signature);

	    return publicSignature.verify(signatureBytes);
	}

	public static PrivateKey readPrivateKey(File file) throws Exception {
		String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
		KeyPair pemPair;
        try (final PEMReader reader = new PEMReader((Reader)new StringReader(key), (PasswordFinder)null, "SunRsaSign")) {
            pemPair = (KeyPair)reader.readObject();
        }
        
		return (PrivateKey) pemPair.getPrivate();
	}
	
	public static PrivateKey readPrivateKey(InputStream inputStream) throws Exception {
		String key = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		KeyPair pemPair;
        try (final PEMReader reader = new PEMReader((Reader)new StringReader(key), (PasswordFinder)null, "SunRsaSign")) {
            pemPair = (KeyPair)reader.readObject();
        }
        
		return (PrivateKey) pemPair.getPrivate();
	}

	public static PublicKey readPublicKey(File file) throws Exception {
		String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
		final PEMReader reader = new PEMReader((Reader) new StringReader(key), (PasswordFinder) null, "SunRsaSign");
		return (PublicKey) reader.readObject();

	}
	
	public static PublicKey readPublicKey(InputStream inputStream) throws Exception {
		String key = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		final PEMReader reader = new PEMReader((Reader) new StringReader(key), (PasswordFinder) null, "SunRsaSign");
		return (PublicKey) reader.readObject();

	}
	
	private static String encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	private static byte[] decode(String data) {
		return Base64.getDecoder().decode(data);
	}

}