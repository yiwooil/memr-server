import java.sql.SQLException;
import java.util.HashMap;


public class MFGetScanClassList implements MFGet {
	private static MFGetScanClassList mInstance=null;
	private MFGetScanClassList(){
		
	}
	
	public static MFGetScanClassList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetScanClassList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String returnString=null;

		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
			if(interfaceTableYn){
				sql += "select scan_class, scan_class_name from scan_class_mast order by scan_class_name";
			}else{
				sql += "select rptcd as scan_class, rptnm as scan_class_name from tg01 order by rptnm";
			}
			String rsString = sqlHelper.executeQuery(sql);
			returnString = rsString;
		} catch (SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
}
