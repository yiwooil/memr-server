import java.util.HashMap;


public class MFGetTprOneRow implements MFGet {
	private static MFGetTprOneRow mInstance=null;
	private MFGetTprOneRow(){
		
	}
	
	public static MFGetTprOneRow getInstance(){
		if(mInstance==null){
			mInstance = new MFGetTprOneRow();
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
				sql = getSqlInter();
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
				"select u64.chkdt" +
				"     , u64.chktm" +
				"     , u64.tmpcase" +
				"     , u64.tmp" +
				"     , u64.pr" +
				"     , u64.rr" +
			    "     , dbo.mfn_piece(u64.bp,'/',1) as maxbp" +
			    "     , dbo.mfn_piece(u64.bp,'/',2) as minbp" +
			    "     , '' as hod" +
			    "     , '' as pod" +
			    "  from tu64 u64" +
		        " where u64.pid=?" +
		        "   and u64.bededt=?" +
		        "   and u64.chkdt=?" +
		        "   and u64.chktm=?" +
		        " order by u64.pid,u64.bededt,u64.chkdt,u64.chktm";
		return sql;
	}
	
	private String getSqlInter(){
		String sql = 
				"select u64.check_date chkdt" +
				"     , u64.check_time chktm" +
				"     , u64.temp_case tmpcase" +
				"     , u64.temperature tmp" +
				"     , u64.pulse pr" +
				"     , u64.breath rr" +
			    "     , u64.bp_max as maxbp" +
			    "     , u64.bp_min as minbp" +
			    "     , '' as hod" +
			    "     , '' as pod" +
			    "  from tpr_hist u64" +
		        " where u64.pat_id=?" +
		        "   and u64.bed_in_date=?" +
		        "   and u64.check_date=?" +
		        "   and u64.check_time=?" +
		        " order by u64.pat_id,u64.bed_in_date,u64.check_date,u64.check_time";
		return sql;
	}

}
