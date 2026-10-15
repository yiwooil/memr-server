import java.util.HashMap;

public class MFGetRptcdList implements MFGet {
	private static MFGetRptcdList mInstance=null;
	private MFGetRptcdList(){
		
	}
	
	public static MFGetRptcdList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetRptcdList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		
		String returnString;
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			String sql = "";
			sql += "select rptcd, rptnm" + "\r\n"; 
			sql += "  from tg01" + "\r\n";
			sql += " where ISNULL(expdt,'')=''" + "\r\n";
			sql += " order by rptnm" + "\r\n";   					

			String rsString=sqlHelper.executeQuery(sql);
			returnString=rsString;
		}catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", e.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(e);
		}
		return returnString;
	}

}
