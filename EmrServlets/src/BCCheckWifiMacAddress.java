import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class BCCheckWifiMacAddress implements BCGet {
	private static BCCheckWifiMacAddress mInstance=null;
	private BCCheckWifiMacAddress(){
		
	}
	
	public static BCCheckWifiMacAddress getInstance(){
		if(mInstance==null){
			mInstance = new BCCheckWifiMacAddress();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String wifiMacAddress = (String)param.get("wifimacaddress");
		
		String returnString="no";

		Connection con=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		try {
			String currentDate = Utility.getCurrentDate();
			con = SqlHelper.getBasecampDataSource().getConnection();
			String sql = "select isnull(b.end_date,'') end_date from Devices a left join LicensesExpire b on b.license_key_no=a.license_key_no where a.wifi_mac_address=?";
			stmt = con.prepareStatement(sql);
			stmt.setString(1, wifiMacAddress);
			rs = stmt.executeQuery();
			if(rs.next()){
				String endDate = rs.getString("end_date");
				if("".equalsIgnoreCase(endDate)){
					// 사용가능
					returnString="yes";
				}else if(endDate.compareTo(currentDate)<0){
					// 사용종료일이 지났음.
					returnString="end1";
				}else{
					// 사용종료예정임
					returnString="end2" + endDate;
				}
			}
			rs.close();
			stmt.close();
			con.close();
		}
		catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString="error : " + ex.getMessage();
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString="error : " + ex.getMessage();
		}
		return returnString;
	}

}
