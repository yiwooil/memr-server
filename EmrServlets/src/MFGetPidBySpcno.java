import java.util.HashMap;

public class MFGetPidBySpcno implements MFGet {
	private static MFGetPidBySpcno mInstance=null;
	private MFGetPidBySpcno(){
		
	}
	
	public static MFGetPidBySpcno getInstance(){
		if(mInstance==null){
			mInstance = new MFGetPidBySpcno();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String spcno = (String)param.get("spcno");

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
				    "  from tc201 (nolock)" +
				    " where spcno=?" + 
				    "   and isnull(cancelfg,'0') ='0'";
			para.put(++idx, spcno); paraType.put(idx, "C");
			String rsString=sqlHelper.executeQuery(sql,para,paraType);
			returnString=rsString;
		}catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", e.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(e);
		}
		return returnString;
	}
}
