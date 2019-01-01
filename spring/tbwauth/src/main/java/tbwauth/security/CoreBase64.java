package tbwauth.security;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BASE64 (RFC 1521)를 직접 구현한 클래스이다.
 */
public class CoreBase64 {
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationServerConfiguration.class);
	
	private static byte[] encodeData;
	private static final String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	private String charSetName = "UTF8";

	static {
		encodeData = new byte[64];
		for (int i = 0; i < 64; i++) {
			byte c = (byte) charSet.charAt(i);
			encodeData[i] = c;
		}
	}

	public CoreBase64() {
	}

	public CoreBase64(String charSetName) {
		this.charSetName = charSetName;
	}

	public String getCharSetName() {
		return charSetName;
	}

	public void setCharSetName(String charSetName) {
		this.charSetName = charSetName;
	}

	public byte[] decode(String str) {
		int end = 0; // end state
		if (str.endsWith("=")) {
			end++;
		}
		if (str.endsWith("==")) {
			end++;
		}
		int len = (str.length() + 3) / 4 * 3 - end;
		byte[] result = new byte[len];
		int dst = 0;

		try {
			for (int src = 0; src < str.length(); src++) {
				int code = charSet.indexOf(str.charAt(src));
				if (code == -1) {
					break;
				}
				switch (src % 4) {
				case 0:
					result[dst] = (byte) (code << 2);
					break;
				case 1:
					result[dst++] |= (byte) ((code >> 4) & 0x3);
					result[dst] = (byte) (code << 4);
					break;
				case 2:
					result[dst++] |= (byte) ((code >> 2) & 0xf);
					result[dst] = (byte) (code << 6);
					break;
				case 3:
					result[dst++] |= (byte) (code & 0x3f);
					break;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Eating Exception!
			logger.warn(e.getMessage());
		}

		return result;
	}

	public byte[] decode(FileInputStream fis) throws Exception
	{
		ByteArrayOutputStream bos = null;

		// read file
		try {
			bos = new ByteArrayOutputStream();
			byte[] in = new byte[1024];
			int len = 0;
			while ((len = fis.read(in, 0, 1024)) > -1) {
				bos.write(in, 0, len);
			}

			// make input string
			String strIn = bos.toString(this.charSetName);

			return this.decode(strIn);

		} catch (IOException ex1) {
			String strMessage = "File read error - " + ex1.getMessage();
			logger.error(strMessage, ex1);
			
			throw new Exception(strMessage, ex1);

		} finally {
			if (bos != null) {
				try {
					bos.close();
					
				} catch (IOException ex1) {
					String strMessage = "File close error - " + ex1.getMessage();
					logger.error(strMessage, ex1);
					
					throw new Exception(strMessage, ex1);
				}
			}
		}

	}

	/**
	 * BASE64로 encode된 주어진 str 문자열을 decode 한 후, 시스템 default encoding 으로 문자열로
	 * 생성해준다.
	 * <p>
	 * 이 메소드는 내부적으로 decode 메소드를 직접 호출한다.
	 */
	public String decodeAsString(String str) throws Exception 
	{
		try {
			return new String(decode(str), this.charSetName);
			
		} catch (UnsupportedEncodingException ex1) {
			String strMessage = "String encoding error - Value : " + str + " -> " + ex1.getMessage();
			logger.error(strMessage, ex1);
			
			throw new Exception(strMessage, ex1);
			
		}
	}

	/**
	 * base-64 encode a string
	 * 
	 * @param s
	 *            The ascii string to encode
	 * @returns The base64 encoded result
	 */

	public String encode(String s) throws Exception
	{
		try {
			return encode(s.getBytes(this.charSetName));
			
		} catch (UnsupportedEncodingException ex1) {
			String strMessage = "String encoding error - Value : " + s + " -> " + ex1.getMessage();
			logger.error(strMessage, ex1);
			
			throw new Exception(strMessage, ex1);
			
		}
		
	}

	/**
	 * base-64 encode a byte array
	 * 
	 * @param src
	 *            The byte array to encode
	 * @returns The base64 encoded result
	 */

	public String encode(byte[] src) throws Exception
	{
		return encode(src, 0, src.length);
	}

	/**
	 * base-64 encode a byte array
	 * 
	 * @param src
	 *            The byte array to encode
	 * @param start
	 *            The starting index
	 * @param len
	 *            The number of bytes
	 * @returns The base64 encoded result
	 */

	public String encode(byte[] src, int start, int length) throws Exception
	{
		byte[] dst = new byte[(length + 2) / 3 * 4 + length / 72];
		int x = 0;
		int dstIndex = 0;
		int state = 0; // which char in pattern
		int old = 0; // previous byte
		int len = 0; // length decoded so far
		int max = length + start;
		for (int srcIndex = start; srcIndex < max; srcIndex++) {
			x = src[srcIndex];
			switch (++state) {
			case 1:
				dst[dstIndex++] = encodeData[(x >> 2) & 0x3f];
				break;
			case 2:
				dst[dstIndex++] = encodeData[((old << 4) & 0x30)
						| ((x >> 4) & 0xf)];
				break;
			case 3:
				dst[dstIndex++] = encodeData[((old << 2) & 0x3C)
						| ((x >> 6) & 0x3)];
				dst[dstIndex++] = encodeData[x & 0x3F];
				state = 0;
				break;
			}
			old = x;
			if (++len >= 72) {
				dst[dstIndex++] = (byte) '\n';
				len = 0;
			}
		}

		/*
		 * now clean up the end bytes
		 */

		switch (state) {
		case 1:
			dst[dstIndex++] = encodeData[(old << 4) & 0x30];
			dst[dstIndex++] = (byte) '=';
			dst[dstIndex++] = (byte) '=';
			break;
		case 2:
			dst[dstIndex++] = encodeData[(old << 2) & 0x3c];
			dst[dstIndex++] = (byte) '=';
			break;
		}
		
		try {
			return new String(dst, this.charSetName);
			
		} catch (UnsupportedEncodingException ex1) {
			String strMessage = "String encoding error - " + ex1.getMessage();
			logger.error(strMessage, ex1);
			
			throw new Exception(strMessage, ex1);

		}
	}
}
