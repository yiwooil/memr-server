import java.util.HashMap;

public class MFGetDmList implements MFGet {
	private static MFGetDmList mInstance=null;
	private MFGetDmList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetDmList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetDmList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");

		String returnString="";
		SqlHelper sqlHelper;

		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			boolean isJaincom = sqlHelper.isJaincom();
			String sql="";
			String paraString="";
			String paraTypeString="";
			
			sql = getSql();
			paraString = "pid,bededt";
			paraTypeString = "C,D";
			
			// 일단 값을 보관한다.
			HashMap<String, Object> paraValue = new HashMap<String, Object>();
			paraValue.put("pid", pid);
			paraValue.put("bededt", bededt);
			
			String rsString = sqlHelper.executeQuery(sql,paraValue,paraString,paraTypeString);
			returnString=rsString;
		} 
		catch (Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", e.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(e);
		}
		
		return returnString;
	}
	
	private String getSql(){
		String key="sql";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select chkdt" +
					"     , chktm" +
					"     , n_value" +
				    "  from tu59_2" +
			        " where pid=?" +
			        "   and bededt=?" +
			        " order by pid,bededt,chkdt desc,chktm desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
}
