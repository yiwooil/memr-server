import java.util.HashMap;

public class MFGetPidByBldno implements MFGet {
	private static MFGetPidByBldno mInstance=null;
	private MFGetPidByBldno(){
		
	}
	
	public static MFGetPidByBldno getInstance(){
		if(mInstance==null){
			mInstance = new MFGetPidByBldno();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String bldno = (String)param.get("bldno");

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
		String returnString;
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			String sql =  
					"select ptid pid" +
				    "  from tb010 (nolock)" +
				    " where bldno=?" + 
				    "   and stscd in ('D1','E1')" +
				    " order by outdt desc";
			para.put(++idx, bldno); paraType.put(idx, "C");
			String rsString=sqlHelper.executeQuery(sql,para,paraType);
			returnString=rsString;
		}catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", e.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(e);
		}
		return returnString;
	}
}
