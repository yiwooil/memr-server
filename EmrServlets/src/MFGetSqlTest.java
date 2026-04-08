import java.sql.SQLException;
import java.util.HashMap;


public class MFGetSqlTest implements MFGet {
	private static MFGetSqlTest mInstance=null;
	private MFGetSqlTest(){
		
	}
	
	public static MFGetSqlTest getInstance(){
		if(mInstance==null){
			mInstance = new MFGetSqlTest();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String sql = (String)param.get("sql");
		
		SqlHelper sqlHelper;
		String returnString;
		
		try {
			sqlHelper = new SqlHelper(hospitalId);
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
