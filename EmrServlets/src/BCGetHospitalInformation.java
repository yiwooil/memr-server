import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BCGetHospitalInformation implements BCGet {
	private static BCGetHospitalInformation mInstance=null;
	private BCGetHospitalInformation(){
		
	}
	
	public static BCGetHospitalInformation getInstance(){
		if(mInstance==null){
			mInstance = new BCGetHospitalInformation();
		}
		return mInstance;
	}

	class Hospital {
		public String hospitalId;
		public String hospitalName;
		public String databaseUrl;
		public String homeUrl;
		public String maskYn;
		public String scanUrl;
		public String preSaveUrl;
		public String mp4Url;
		public String picUrl;
		public String interfaceTableYn;
		public String servletIp;
		public String emrCompany;
	}
	static Map<String, Hospital> hospitals = null; // 병원정보를 데이터베이스에서 한번만 읽기 위한 처리.

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String reset =  (String)param.get("reset");
		
		if(reset==null) reset=""; //오류방지용
		
		String reUseYn="";
		// 메모리에 다시 올려놓기 위하여 메모리를 비운다.
		if(reset.endsWith("Y")){
			if (hospitals!=null) {
				if (hospitals.containsKey(hospitalId)) {
					hospitals.remove(hospitalId);
				}
			}
		}
		// 메모리에 읽어 놓은 것이 있으면 그것을 사용한다.
		Hospital hos = null;
		if (hospitals!=null) {
			if (hospitals.containsKey(hospitalId)) {
				hos = hospitals.get(hospitalId);
				reUseYn="Y";
			}
		}
		if (hospitals==null) {
			hospitals = new HashMap<String, Hospital>();
		}
		// 메로리에 없으면 데이터베이스에서 읽어서 메모리에 올려놓는다.
		if (hos==null) {
			setHospitalInformation(hospitalId);
			hos = hospitals.get(hospitalId);
		}
		
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		String returnString=null;

		int cnt=0;
		cnt++;
		columns = new JSONObject();
		columns.put("hospital_name", hos.hospitalName);
		columns.put("database_url", hos.databaseUrl);
		columns.put("home_url", hos.homeUrl);
		columns.put("mask_yn", hos.maskYn);
		columns.put("scan_url", hos.scanUrl);
		columns.put("presave_url", hos.preSaveUrl);
		columns.put("mp4_url", hos.mp4Url);
		columns.put("pic_url", hos.picUrl);
		columns.put("interface_table_yn", hos.interfaceTableYn);
		columns.put("servlet_ip", hos.servletIp);
		columns.put("emr_company", hos.emrCompany);
		columns.put("reuse_yn", reUseYn); // 메모리에서 가져온 값을 사용하는지 여부.
		rowData.add(columns);
		
		columns = new JSONObject();
		columns.put("return_code", cnt);
		columns.put("return_desc", "ok");
		status.add(columns);
		// 반환자료
		result.add(status);
		result.add(rowData);
		return result.toJSONString();
		
	}
	
	private void setHospitalInformation(String hospitalId) throws SQLException, Exception{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		Hospital hos = null;
		try{
			hos = new Hospital();
			hos.hospitalId=hospitalId;
			hos.hospitalName="";
			hos.databaseUrl="";
			hos.homeUrl="";
			hos.maskYn="";
			hos.scanUrl="";
			hos.preSaveUrl="";
			hos.mp4Url="";
			hos.picUrl="";
			hos.interfaceTableYn="";
			hos.servletIp="";
			hos.emrCompany="";

			con = SqlHelper.getBasecampDataSource().getConnection();
			String sql = 
					"select hospital_name" +
			        "     , isnull(database_url,'') as database_url" +
			        "     , isnull(home_url,'') as home_url" +
			        "     , isnull(mask_yn,'') as mask_yn" +
			        "     , isnull(scan_url,'') as scan_url" +
			        "     , isnull(presave_url,'') as presave_url" +
			        "     , isnull(mp4_url,'') as mp4_url" +
			        "     , isnull(pic_url,'') as pic_url" +
			        "     , isnull(interface_table_yn,'') as interface_table_yn" +
				    "     , isnull(servlet_ip,'') as servlet_ip" +
			        "     , isnull(emr_company,'') as emr_company" +
					"  from Hospitals " +
					" where hospital_id=?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, hospitalId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				hos.hospitalName = rs.getString("hospital_name");
				hos.databaseUrl = rs.getString("database_url");
				hos.homeUrl = rs.getString("home_url");
				hos.maskYn = rs.getString("mask_yn");
				hos.scanUrl = rs.getString("scan_url");
				hos.preSaveUrl = rs.getString("presave_url");
				hos.mp4Url = rs.getString("mp4_url");
				hos.picUrl = rs.getString("pic_url");
				hos.interfaceTableYn = rs.getString("interface_table_yn");
				hos.servletIp = rs.getString("servlet_ip");
				hos.emrCompany = rs.getString("emr_company");
			}
			hospitals.put(hospitalId, hos);
		} catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "setHospitalInformation", "SQLException", ex.getLocalizedMessage());
			throw new SQLException(ex);
		} catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "setHospitalInformation", "Exception", ex.getLocalizedMessage());
			throw new Exception(ex);
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(con!=null) {
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
