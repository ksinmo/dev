package tbwauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

public class ClientDao {
	@Autowired
	DataSource dataSource;
	
	private Connection con;
	public ClientDao(Connection con) {
		this.con = con;
	}
	public JSONObject find(String cliendId) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		StringBuffer strSQL = new StringBuffer();
		
		strSQL.append("SELECT client_id, client_secret, resource_ids, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove, logout_uri, base_uri ");
		strSQL.append("FROM oauth_client_details ");
		strSQL.append("WHERE CLIENT_ID = ? ");
		
		try {
			pstmt = this.con.prepareStatement(strSQL.toString());
			pstmt.setString(1,  cliendId);
			rs = pstmt.executeQuery();

			return JsonDao.convertObject(rs);
		} catch (SQLException ex) {
			throw ex;
		}
		finally {
			if (rs != null) { 
				rs.close();
				rs = null;
			}
			
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		}
		
	}
}
