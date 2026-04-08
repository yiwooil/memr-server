import java.util.HashMap;

public class MFGetDmOneRow implements MFGet {
	private static MFGetDmOneRow mInstance=null;
	private MFGetDmOneRow(){
		
	}
	
	public static MFGetDmOneRow getInstance(){
		if(mInstance==null){
			mInstance = new MFGetDmOneRow();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String chkdate = (String)param.get("chkdate");
		String chktime = (String)param.get("chktime");

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
		String returnString="";
		SqlHelper sqlHelper;
		
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql="";
			if(interfaceTableYn){
				//sql = getSqlInter();
			}else{
				sql = getSql();
			}
			para.put(++idx, pid); paraType.put(idx, "C");
			para.put(++idx, bededt); paraType.put(idx, "D");
			para.put(++idx, chkdate); paraType.put(idx, "D");
			para.put(++idx, chktime); paraType.put(idx, "C");
			String rsString = sqlHelper.executeQuery(sql,para,paraType);
			returnString=rsString;
		} 
		catch (Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", e.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(e);
		}
		
		return returnString;
	}
	
	private String getSql(){
		String sql = 
				"select chkdt" +
				"     , chktm" +
				"     , n_value" +
			    "  from tu59_2" +
		        " where pid=?" +
		        "   and bededt=?" +
		        "   and chkdt=?" +
		        "   and chktm=?" +
		        " order by pid,bededt,chkdt,chktm";
		return sql;
	}

}
