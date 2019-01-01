package tbwauth.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AesPasswordEncoder implements PasswordEncoder {
	private static final Logger log = LoggerFactory.getLogger(AesPasswordEncoder.class);
	public static final String AES_ECB_PKCS5PADDING = "AES/ECB/PKCS5Padding";

    @Override
    public String encode(CharSequence rawPassword) {
    	
    	String strInput = rawPassword.toString();
    	String result = "";
    	String defaultKeyString = "KA0zC4Havp/W4Eu7XQ4ksg=="; 
    	String defaultIVString = "zNVIwXJsiJRMU15ZhNLUYQ==";
		
		if (!"".equals(strInput)) {
			try {
				String strCryptoAlgorithm = AES_ECB_PKCS5PADDING;
				
				CoreBase64 base64 = new CoreBase64();
				byte[] byteKey = base64.decode(defaultKeyString);
				SecretKeySpec keySpec = new SecretKeySpec(byteKey, "AES");
				Cipher cipher = Cipher.getInstance(strCryptoAlgorithm);
				
				// AES/ECB/PKCS5Padding - IV 필요 없음
				if (strCryptoAlgorithm.equals(AES_ECB_PKCS5PADDING)) {
					cipher.init(Cipher.ENCRYPT_MODE, keySpec);
					
				} else {
					byte[] byteIV = base64.decode(defaultIVString);
					
					IvParameterSpec ivParams = new IvParameterSpec(byteIV);
					cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParams);
					
				}
				
				byte[] byteInput = strInput.getBytes("UTF8");
				byte[] byteEncrypted = cipher.doFinal(byteInput);
				result = base64.encode(byteEncrypted);			
			}catch (Exception ex) {
				//String strMessage = "String encoding error - " + ex8.getMessage();
				//throw new Exception(strMessage, ex7);
				
			}
		}
		System.out.println("## Encrypted Password = " + result + ",rawPassword=" + rawPassword.toString());
		return result;    
	}
 
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
    	String curEncoded = encode(rawPassword.toString());
		log.info("Called /oauth/authorize : password matches {} : input = {}, db = {} " ,curEncoded.equals(encodedPassword)
				,curEncoded, encodedPassword);
        return curEncoded.equals(encodedPassword);
    }
    
 
}
