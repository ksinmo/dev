package tbwauth.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import tbwauth.model.AccessToken;
import tbwauth.model.Client;

@Service("ssoService")
public class SsoServiceImpl implements SsoService {
	@Autowired
	private DataSource dataSource;
	private static final Logger log = LoggerFactory.getLogger(SsoServiceImpl.class);

	@Override
	public AccessToken getAccessToken(String token, String clientId) {
		String tokenId = extractTokenId(token);
		
		Connection con;
		PreparedStatement pstmt;
		StringBuffer strSQL = new StringBuffer();
		
		strSQL.append("SELECT token_id, token, authentication_id, user_name, client_id, authentication, refresh_token ");
		strSQL.append("FROM oauth_access_token ");
		strSQL.append("WHERE token_id = ? AND client_id = ? ");
		log.info("query={}", strSQL.toString());
		
		AccessToken accessToken = new AccessToken();
		try 
		{
			con = dataSource.getConnection();
			pstmt = con.prepareStatement(strSQL.toString());
			pstmt.setString(1,  tokenId);
			pstmt.setString(2,  clientId);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				accessToken.setTokenId(rs.getString("token_id"));
				accessToken.setUserName(rs.getString("user_name"));				
			}
		} catch(Exception ex) {
			
		}
		return accessToken;
	}

	private String extractTokenId(String value) {
		//
		if (value == null) {
			//
			return null;
		}

		try {
			//
			MessageDigest digest = MessageDigest.getInstance("MD5");
			
			byte[] bytes = digest.digest(value.getBytes("UTF-8"));
			return String.format("%032x", new BigInteger(1, bytes));
		}
		catch (NoSuchAlgorithmException e) {
			//
			throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
		}
		catch (UnsupportedEncodingException e) {
			//
			throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
		}
	}
	
	@Transactional
	public String logoutAllClients(String clientId, String userName) {
		requestLogoutToAllClients(userName);
		
		removeAccessTokens(userName);
		
		Client client = getClient(clientId);
		
		return client.getBaseUri();
	}
	private void requestLogoutToAllClients(String userName) {
		Connection con;
		PreparedStatement pstmt;
		StringBuffer strSQL = new StringBuffer();
		
		strSQL.append("SELECT token_id, token, authentication_id, user_name, client_id, authentication, refresh_token ");
		strSQL.append("FROM oauth_access_token ");
		strSQL.append("WHERE user_name = ? ");
		log.info("query={}", strSQL.toString());
		
		try 
		{
			con = dataSource.getConnection();
			pstmt = con.prepareStatement(strSQL.toString());
			pstmt.setString(1,  userName);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				AccessToken accessToken = new AccessToken();
				accessToken.setTokenId(rs.getString("token_id"));
				accessToken.setUserName(rs.getString("user_name"));				
				accessToken.setAuthenticationId(rs.getString("authentication_id"));				
				accessToken.setClientId(rs.getString("client_id"));						
				requestLogoutToClient(accessToken);
			}
		} catch(Exception ex) {
			
		}
	}
	private void requestLogoutToClient(AccessToken token) {
		//
		Client client = getClient(token.getClientId());
		
		String logoutUri = client.getLogoutUri();
		log.debug("\n## in requestLogoutToClient logoutUri : {}", logoutUri);
	
		String authorizationHeader = null;
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("tokenId", token.getTokenId());
		paramMap.put("userName", token.getUserName());
		
		HttpPost post = buildHttpPost(logoutUri, paramMap, authorizationHeader);
		
		executePostAndParseResult(post, Object.class);
	}
	private HttpPost buildHttpPost(String reqUrl, Map<String, String> paramMap, String authorizationHeader) {
		//
		HttpPost post = new HttpPost(reqUrl);
		if (authorizationHeader != null) {
			//
			post.addHeader("Authorization", authorizationHeader);
		}
		
		List<NameValuePair> urlParameters = new ArrayList<>();
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		
		try {
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return post;
	}
	private <T> T executePostAndParseResult(HttpPost post, Class<T> clazz) {
		//
		T result = null;
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
			log.debug("\n## response body : '{}'", resultBuffer.toString());
			
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(resultBuffer.toString(), clazz);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		
		return result;
	}
	private int removeAccessTokens(String userName) {
		Connection con;
		PreparedStatement pstmt;
		StringBuffer strSQL = new StringBuffer();
		
		strSQL.append("DELETE FROM oauth_access_token ");
		strSQL.append("WHERE user_name = ? ");
		log.info("query={}", strSQL.toString());
		
		try 
		{
			con = dataSource.getConnection();
			pstmt = con.prepareStatement(strSQL.toString());
			pstmt.setString(1,  userName);
			pstmt.execute();
		} catch(Exception ex) {
			return -1;
		}
		return 0;
	}
	private Client getClient(String clientId) {
		Client client = new Client();
		Connection con;
		PreparedStatement pstmt;
		StringBuffer strSQL = new StringBuffer();
		
		strSQL.append("SELECT  ");
		strSQL.append("FROM  ");
		strSQL.append("WHERE client_id = ? ");
		log.info("query={}", strSQL.toString());
		
		try 
		{
			con = dataSource.getConnection();
			pstmt = con.prepareStatement(strSQL.toString());
			pstmt.setString(1,  clientId);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				AccessToken accessToken = new AccessToken();
				accessToken.setTokenId(rs.getString("token_id"));
				accessToken.setUserName(rs.getString("user_name"));				
				accessToken.setAuthenticationId(rs.getString("authentication_id"));				
				accessToken.setClientId(rs.getString("client_id"));						
				requestLogoutToClient(accessToken);
			}
		} catch(Exception ex) {
			
		}
		return client;
	}
	
}
