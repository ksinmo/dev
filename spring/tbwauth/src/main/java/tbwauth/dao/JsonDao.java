package tbwauth.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonDao {
	@SuppressWarnings("unchecked")
	public static JSONArray convert(ResultSet rs) throws SQLException {
		JSONArray array = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		while (rs.next()) {
			int numColumns = rsmd.getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 1; i <= numColumns; i++) {
				String column_name = rsmd.getColumnName(i);
				if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
					obj.put(column_name, rs.getDate(column_name));
				} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
					obj.put(column_name, rs.getTimestamp(column_name));
				} else {
					obj.put(column_name, rs.getObject(column_name));
				}
			}
			array.add(obj);
		}
		return array;
	}
	@SuppressWarnings("unchecked")
	public static JSONObject convertObject(ResultSet rs) throws SQLException {
		JSONObject obj = new JSONObject();
		ResultSetMetaData rsmd = rs.getMetaData();
		if (rs.next()) {
			int numColumns = rsmd.getColumnCount();			
			for (int i = 1; i <= numColumns; i++) {
				String column_name = rsmd.getColumnName(i);
				if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
					obj.put(column_name, rs.getDate(column_name));
				} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
					obj.put(column_name, rs.getTimestamp(column_name));
				} else {
					obj.put(column_name, rs.getObject(column_name));
				}
			}
		}
		return obj;
	}
}
