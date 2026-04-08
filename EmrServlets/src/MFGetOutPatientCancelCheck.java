import java.sql.SQLException;
import java.util.HashMap;

public class MFGetOutPatientCancelCheck implements MFGet {
	private static MFGetOutPatientCancelCheck mInstance=null;
	private MFGetOutPatientCancelCheck(){
		
	}
	
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	public static MFGetOutPatientCancelCheck getInstance(){
		if(mInstance==null){
			mInstance = new MFGetOutPatientCancelCheck();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String exdt = (String)param.get("exdt");
		String dptcd = (String)param.get("dptcd");
		String hms = (String)param.get("hms");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString = "";
		SqlHelper sqlHelper;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql="";
			sql = getSql();
			para.put(1, pid);
			para.put(2, exdt);
			para.put(3, dptcd);
			para.put(4, hms);
			String rsString = sqlHelper.executeQuery(sql,para,null);
			returnString = rsString;		
		}catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	private String getSql(){
		String key="sql";
		if(sqlMap.containsKey(key)==false){
			String sql = "";
			sql += "select count(*) as cnt" + "\r\n";
			sql += "  from ts21" + "\r\n";
			sql += " where pid=?" + "\r\n";
			sql += "   and exdt=?" + "\r\n";
			sql += "   and dptcd=?" + "\r\n";
			sql += "   and hms=?" + "\r\n";
			sql += "   and isnull(ccfg,'') in ('','0')" + "\r\n";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

}
