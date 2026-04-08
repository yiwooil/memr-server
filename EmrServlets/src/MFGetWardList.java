import java.sql.SQLException;
import java.util.HashMap;


public class MFGetWardList implements MFGet {
	private static MFGetWardList mInstance=null;
	private MFGetWardList(){
		
	}
	
	public static MFGetWardList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetWardList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn=sqlHelper.getInterfaceTableYn();
			String sql="";
			if(interfaceTableYn){
				sql = "select ward_id as wardid,ward_name as wardnm from ward_mast order by ward_name";
			}else{
				sql = "select dptcd as wardid,dptnm as wardnm from ta09 where dptdiv='3' order by dptnm";
			}
			String rsString=sqlHelper.executeQuery(sql);
			returnString=rsString;
		}catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;		
	}

}
