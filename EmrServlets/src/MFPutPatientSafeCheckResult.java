import java.util.HashMap;

public class MFPutPatientSafeCheckResult implements MFPut {
	private static MFPutPatientSafeCheckResult mInstance=null;
	private MFPutPatientSafeCheckResult(){
		
	}
	
	public static MFPutPatientSafeCheckResult getInstance(){
		if(mInstance==null){
			mInstance = new MFPutPatientSafeCheckResult();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userid = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String chktype = (String)param.get("chktype");
		String chkdata = (String)param.get("chkdata"); // chktype에 따라 검체번호이거나 혈액번호
		String chkresult = (String)param.get("chkresult");

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString="OK";
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			String sql =
					"insert into tz21(pid,chktype,chkdata,chkresult,empid,sysdt,systm)" +
					"values(?,?,?,?,?,convert(varchar,getdate(),112),replace(convert(varchar,getdate(),24),':',''))" +
					"";
			int idx=0;
			para.put(++idx, pid);
			para.put(++idx, chktype);
			para.put(++idx, chkdata);
			para.put(++idx, chkresult);
			para.put(++idx, userid);

			
			String insResult=sqlHelper.executeUpdate(sql,para);
			returnString=insResult;
		}
		catch (Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "Exception", e.getLocalizedMessage());
			returnString=e.getMessage();
		}
		return returnString;
	}
	
}
