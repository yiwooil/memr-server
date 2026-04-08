import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class BCGetApkVersionName implements BCGet {
	private static BCGetApkVersionName mInstance=null;
	private BCGetApkVersionName(){
		
	}
	
	public static BCGetApkVersionName getInstance(){
		if(mInstance==null){
			mInstance = new BCGetApkVersionName();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String apkName = (String)param.get("apkname");
		
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		String returnString=null;
		
		Connection con=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		try {
			int cnt=0;
			
			con = SqlHelper.getBasecampDataSource().getConnection();
			String sql = 
					"select a.version_name, isnull(a.metrosoft_url,'') as metrosoft_url, isnull(a.google_url,'') google_url " +
					"  from ApkVersion a " +
					" where a.apk_name=? " +
					"   and a.version_code = (select max(b.version_code) from apkversion b where b.apk_name=a.apk_name)";
			stmt = con.prepareStatement(sql);
			stmt.setString(1, apkName);
			rs = stmt.executeQuery();
			if (rs.next()) {
				cnt++;
				columns = new JSONObject();
				columns.put("version_name", rs.getString("version_name"));
				columns.put("metrosoft_url", rs.getString("metrosoft_url"));
				columns.put("google_url", rs.getString("google_url"));
				rowData.add(columns);
			}
			columns = new JSONObject();
			columns.put("return_code", cnt);
			columns.put("return_desc", "ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			result.add(rowData);
			return result.toJSONString();
		} catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			return ExceptionHelper.toJSONString(ex);
		} catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			return ExceptionHelper.toJSONString(ex);
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(stmt!=null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
	}

}
