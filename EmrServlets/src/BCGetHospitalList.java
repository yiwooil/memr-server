import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class BCGetHospitalList implements BCGet {
	private static BCGetHospitalList mInstance=null;
	private BCGetHospitalList(){
		
	}
	
	public static BCGetHospitalList getInstance(){
		if(mInstance==null){
			mInstance = new BCGetHospitalList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String wifiMacAddress = (String)param.get("wifimacaddress");
		if (wifiMacAddress==null) wifiMacAddress="";
		String licenseKeyNo = (String)param.get("licensekeyno");
		if (licenseKeyNo==null) licenseKeyNo="";
		
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		String returnString=null;
		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String currentDate = Utility.getCurrentDate();
			con = SqlHelper.getBasecampDataSource().getConnection();
			if(!"".equalsIgnoreCase(licenseKeyNo)){
				String sql = "";
				sql += "select b.hospital_id,c.hospital_name,isnull(c.servlet_ip,'') as servlet_ip,isnull(c.servlet_ip_2,'') as servlet_ip_2 " + "\n";
				sql += "  from Licenses b inner join Hospitals c on c.hospital_id=b.hospital_id " + "\n";
				sql += " where b.license_key_no=?" + "\n";
				sql += "   and ? between isnull(start_date,'19990101') and isnull(end_date,'99991231')" + "\n";
				sql += " order by case when b.hospital_id='9996' then 0 else 1 end" + "\n";
				sql += "        , case when left(b.hospital_id,1)>='9' then 1 else 0 end" + "\n";
				sql += "        , case when left(b.hospital_id,1)>='9' then b.hospital_id else c.hospital_name end" + "\n";
				stmt = con.prepareStatement(sql);
				stmt.setString(1, licenseKeyNo);
				stmt.setString(2, currentDate);
			}else{
				String sql = "";
				sql += "select b.hospital_id,c.hospital_name,isnull(c.servlet_ip,'') as servlet_ip,isnull(c.servlet_ip_2,'') as servlet_ip_2 " + "\n";
				sql += "  from Devices a inner join Licenses b on b.license_key_no=a.license_key_no " + "\n";
				sql += "                 inner join Hospitals c on c.hospital_id=b.hospital_id " + "\n";
				sql += " where a.wifi_mac_address=?" + "\n";
				sql += "   and ? between isnull(start_date,'19990101') and isnull(end_date,'99991231')" + "\n";
				stmt = con.prepareStatement(sql);
				stmt.setString(1, wifiMacAddress);
				stmt.setString(2, currentDate);
			}
			rs = stmt.executeQuery();
			// 2026.02.25 WOOIL - 중복 체크용
			Set<String> hospitalIdSet = new HashSet<String>();
			int cnt=0;
			while (rs.next()) {
				String hospitalId = rs.getString("hospital_id");

			    // 이미 추가된 hospital_id면 skip
			    if (hospitalIdSet.contains(hospitalId)) continue;

			    // 추가 처리
			    hospitalIdSet.add(hospitalId);

			    String hospitalName = rs.getString("hospital_name");
				String servlet_ip = rs.getString("servlet_ip");
				String servlet_ip_2 = rs.getString("servlet_ip_2");
				if("".equals(servlet_ip_2)) servlet_ip_2 = servlet_ip;
				
				cnt++;
				columns = new JSONObject();
				columns.put("hospital_id", hospitalId);
				columns.put("hospital_name", hospitalName);
				// 2023.08.08 WOOIL - (신세계)병원 내부에서 공인IP에 접속을 하지 못하여 
				//                    병원용(servlet_ip)과 본사용(servlet_ip_2)으로 IP를 분리하였다.
				//                    라이센스 키가 metro-soft-dev이면 servlet_ip_2를 사용하여 접속한다.
				if("metro-soft-dev".equalsIgnoreCase(licenseKeyNo)){
					columns.put("servlet_ip", servlet_ip_2);
				}else{
					columns.put("servlet_ip", servlet_ip);
				}
				// 2022.04.14 WOOIL - emr_company, mask_yn, servlet_use_yn 을 테이블에서 제거하고 기본값으로 셋팅한다.
				//                  - 화면에서 사용하기 때문에 기본값으로 넘겨야 오류가 발생하지 않는다.
				//                  - 화면(app)을 사용자가 모두 upgrade해야 이부분을 없앨 수 있기때문에, 없애지 말자
				//                  - 구글 플레이에서 자동upgrade하지 않는 병원도 있다.
				columns.put("mask_yn", "");
				columns.put("servlet_use_yn", "y");
				columns.put("emr_company", "metrosoft");
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
	}

}
