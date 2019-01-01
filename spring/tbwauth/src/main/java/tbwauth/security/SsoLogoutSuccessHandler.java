package tbwauth.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import tbwauth.dao.AccessTokenDao;
import tbwauth.dao.ClientDao;

@Component
public class SsoLogoutSuccessHandler implements LogoutSuccessHandler {
	private static final Logger log = LoggerFactory.getLogger(SsoLogoutSuccessHandler.class);
//	@Autowired
//	DataSource dataSource;

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		String username = request.getParameter("username");
		String clientId = request.getParameter("clientId");
		
		log.info("SsoLogoutSuccessHandler onLogoutSuccess. {}, {}", username, clientId);
		
		String baseUri = "";
		
		Connection conn = null;
		
		try {
			
			InitialContext ctx = new InitialContext();
			
			DataSource ds = (DataSource) ctx.lookup("TOBECORE");
			conn = ds.getConnection();
			requestLogoutToAllClients(username, conn);

			ClientDao clientDao = new ClientDao(conn);
			JSONObject client = clientDao.find(clientId);
			baseUri = (String)client.get("BASE_URI");
		} catch (Exception ex) {
			log.error("onLogoutSuccess error. {}", ex);
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					
				}
				conn = null;
			}
		}
		request.getSession().invalidate();
		response.setStatus(HttpStatus.SC_OK);
		//response.sendRedirect(baseUri);
	}
	
	private void requestLogoutToAllClients(String username, Connection conn) throws Exception {
		
		
		AccessTokenDao accessTokenDao = new AccessTokenDao(conn);
		ClientDao clientDao = new ClientDao(conn);
				
		JSONArray tokens = accessTokenDao.findByUsername(username);
		
		for (Object o : tokens) {
			JSONObject token = (JSONObject)o;
			JSONObject client = clientDao.find((String)token.get("CLIENT_ID"));
			log.info("Call client logout: clientId={}, logoutUri={}, tokenId={}, username={}", 
					token.get("CLIENT_ID"), client.get("LOGOUT_URI"), token.get("TOKEN_ID"), username);
		
			String authorizationHeader = null;
			
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("tokenId", (String)token.get("TOKEN_ID"));
			paramMap.put("userName", username);
			
			HttpPost post = buildHttpPost((String)client.get("LOGOUT_URI"), paramMap, authorizationHeader);
			
			executePostAndParseResult(post, Object.class);
		}
		accessTokenDao.deleteByUserName(username);

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
			log.info("\n## response body : '{}'", resultBuffer.toString());
			
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(resultBuffer.toString(), clazz);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		
		return result;
	}
} 