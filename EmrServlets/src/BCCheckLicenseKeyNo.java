import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class BCCheckLicenseKeyNo implements BCGet {
	private static BCCheckLicenseKeyNo mInstance=null;
	private BCCheckLicenseKeyNo(){
		
	}
	
	public static BCCheckLicenseKeyNo getInstance(){
		if(mInstance==null){
			mInstance = new BCCheckLicenseKeyNo();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String licenseKeyNo = (String)param.get("licensekeyno");

		String returnString="no";

		Connection con=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		try {
			String currentDate = Utility.getCurrentDate();
			con = SqlHelper.getBasecampDataSource().getConnection();
			String sql = 
					"select * " +
			        "  from Licenses a left join LicensesExpire b on b.license_key_no=a.license_key_no " +
					" where a.license_key_no=? " +
			        "   and ? between '19990101' and isnull(b.end_date,'99991231')";
			stmt = con.prepareStatement(sql);
			stmt.setString(1, licenseKeyNo);
			stmt.setString(2, currentDate);
			rs = stmt.executeQuery();
			int cnt=0;
			while (rs.next()) {
				cnt++;
			}
			rs.close();
			stmt.close();
			con.close();
			returnString = cnt>0 ? "yes" : "no";
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
