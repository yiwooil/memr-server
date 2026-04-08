import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class BCPutRegisterDevice implements BCPut {
	private static BCPutRegisterDevice mInstance=null;
	private BCPutRegisterDevice(){
		
	}
	
	public static BCPutRegisterDevice getInstance(){
		if(mInstance==null){
			mInstance = new BCPutRegisterDevice();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		String wifiMacAddress = (String)param.get("wifimacaddress");
		String licenseKeyNo = (String)param.get("licensekeyno");

		String returnString="no";
		
		Connection con=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		int cnt=0;
		//String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Basecamp;user=sa;password=mms;";
		try {
			con = SqlHelper.getBasecampDataSource().getConnection();
			String sql = "select count(*) as cnt from Devices where wifi_mac_address=?";
			stmt = con.prepareStatement(sql);
			stmt.setString(1, wifiMacAddress);
			rs = stmt.executeQuery();
			if (rs.next()) {
				cnt=rs.getInt("cnt");
			}
			rs.close();
			if(cnt>0){
				sql = "update Devices set license_key_no=? where wifi_mac_address=?";
				stmt = con.prepareStatement(sql);
				stmt.setString(1, licenseKeyNo);
				stmt.setString(2, wifiMacAddress);
				stmt.executeUpdate();
				stmt.close();
			}else{
				sql = "insert into Devices(wifi_mac_address,license_key_no) values(?,?)";
				stmt = con.prepareStatement(sql);
				stmt.setString(1, wifiMacAddress);
				stmt.setString(2, licenseKeyNo);
				stmt.executeUpdate();
				stmt.close();				
			}
			con.close();
			returnString = "yes";
		}
		catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "SQLException", ex.getLocalizedMessage());
			returnString="error : " + ex.getMessage();
		} catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "Exception", ex.getLocalizedMessage());
			returnString="error : " + ex.getMessage();
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
		return returnString;
	}

}
