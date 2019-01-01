package com.tobeway.samplecode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpUtil {
	public <T> T sendPost(String reqUrl, Map<String, String> paramMap, String authorizationHeader, Class<T> clazz) {
		T result = null;
		HttpPost post = buildHttpPost(reqUrl,paramMap,authorizationHeader);
		try {
			//
			HttpClient client = HttpClientBuilder.create().build();
			
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));
	
			StringBuffer resultBuffer = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				resultBuffer.append(line);
			}
			//log.debug("\n## response body : '{}'", resultBuffer.toString());
			
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(resultBuffer.toString(), clazz);
		} catch (IOException e) {
			//log.error(e.getMessage(), e);
		}
		
		return result;
	}
	
	private HttpPost buildHttpPost(String reqUrl, Map<String, String> paramMap, String authorizationHeader) {		
		HttpPost post = new HttpPost(reqUrl);
		if (authorizationHeader != null) {
			post.addHeader("Authorization", authorizationHeader);
		}
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		
		try {
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
		} catch (UnsupportedEncodingException e) {
			//log.error(e.getMessage(), e);
		}
		return post;
	}

}
