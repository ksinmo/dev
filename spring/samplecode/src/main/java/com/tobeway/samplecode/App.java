package com.tobeway.samplecode;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("grant_type", "authorization_code");
		paramMap.put("redirect_uri", "111");
		paramMap.put("code", "ukagZK");
		
		Encoder encoder = Base64.getEncoder();

		String authorizationHeader = null;
		try {
			String toEncodeString = String.format("%s:%s", "System1_id", "System1_secret");
			authorizationHeader = "Basic " + encoder.encodeToString(toEncodeString.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}

		HttpUtil httpUtil = new HttpUtil();
		TokenRequestResult result = httpUtil.sendPost("http://192.168.1.13:8080/TBWAUTH/oauth/token", paramMap, authorizationHeader, TokenRequestResult.class);
		System.out.println("result=" + result.getAccessToken());
    }
}
