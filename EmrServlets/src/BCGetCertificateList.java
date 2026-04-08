import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class BCGetCertificateList implements BCGet {
	private static BCGetCertificateList mInstance=null;
	private BCGetCertificateList(){
		
	}
	
	public static BCGetCertificateList getInstance(){
		if(mInstance==null){
			mInstance = new BCGetCertificateList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// basecamp 에서 읽는 것으로 수정함.
		// 속도를 빠르게 하고
		// 동의서를 선택적으로 오픈하며
		// 탭에 맞게 자유롭게 수정하기 위함인.
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");

		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		String returnString=null;

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try{
			//con = DriverManager.getConnection(connectionUrl);
			con = SqlHelper.getBasecampDataSource().getConnection();
			String sql = 
					"select ccf_id,ccf_name,ccf_filename " +
			        "  from consentforms " +
					" where hospital_id=?" +
			        " order by disp_order";
			stmt = con.prepareStatement(sql);
			stmt.setString(1, hospitalId);
			rs = stmt.executeQuery();
			int cnt=0;
			while (rs.next()) {
				cnt++;
				columns = new JSONObject();
				columns.put("ccf_id", rs.getString("ccf_id"));
				columns.put("ccf_name", rs.getString("ccf_name"));
				columns.put("ccf_filename", rs.getString("ccf_filename"));
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
			return ExceptionHelper.toJSONString(ex);
		} catch(Exception ex) {
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
			if(stmt!=null){
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
		/*
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			String sql = "select * from ta88 where mst1cd='EMR' and mst2cd='FORM' order by convert(numeric,fld1cd) ";
			String rsString = sqlHelper.executeQuery(sql);
			returnString = rsString;
		} catch (SQLException ex) {
			returnString = ExceptionHelper.toJSONString(ex);
			//returnString = new ExceptionHelper(ex).toJSONString();
		}
		return returnString;
		*/
	}

}
