package tbwauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONArray;

public class AccessTokenDao {
	private Connection con;
	public AccessTokenDao(Connection con) {
		this.con = con;
	}
	public JSONArray findByUsername(String username) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
				
		StringBuffer strSQL = new StringBuffer();
		
		strSQL.append("SELECT token_id, token, authentication_id, user_name, client_id, authentication, refresh_token ");
		strSQL.append("FROM oauth_access_token ");
		strSQL.append("WHERE user_name = ? ");
		
		try {
			pstmt = this.con.prepareStatement(strSQL.toString());
			pstmt.setString(1,  username);
			rs = pstmt.executeQuery();
			
			return JsonDao.convert(rs);
		}
		catch (SQLException ex) {
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
	public void deleteByUserName(String username) throws Exception{
		PreparedStatement pstmt = null;
		StringBuffer strSQL = new StringBuffer();
		
		strSQL.append("DELETE FROM oauth_access_token ");
		strSQL.append("WHERE user_name = ? ");
		
		try {
			pstmt = this.con.prepareStatement(strSQL.toString());
			pstmt.setString(1,  username);
			pstmt.executeUpdate();
		}
		catch (SQLException ex) {
			throw ex;
		}
		finally {
			
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		}
	}
}
