import java.util.HashMap;


public class MFGetOutPatientInfoString implements MFGet {
	private static MFGetOutPatientInfoString mInstance=null;
	private MFGetOutPatientInfoString(){
		
	}
	
	public static MFGetOutPatientInfoString getInstance(){
		if(mInstance==null){
			mInstance = new MFGetOutPatientInfoString();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
		String returnString;
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql="";
			//if(interfaceTableYn){
			//	sql = getSqlInter();
			//}else{
				sql = getSql();
			//}
			para.put(++idx, pid); paraType.put(idx, "C");
			para.put(++idx, bededt); paraType.put(idx, "D");
			String rsString=sqlHelper.executeQuery(sql,para,paraType);
			rs = new ResultSetHelper(rsString,sqlHelper.getMasking());
			returnString = rs.getString(0, "pnm") + " " +
					       rs.getString(0, "psex") + "/" + rs.getString(0,"age") + " " +
					       rs.getString(0, "dptcd") + " " +
					       Utility.getFormattedDate(bededt);
		}catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", e.getLocalizedMessage());
			returnString = e.getMessage();
		}
		return returnString;
	}
	
	private String getSql(){
		String sql =  
				"select s21.pid" +
				"     , a01.pnm" +
				"     , a01.psex" +
				"     , s21.dptcd" +
				"     , dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112)) as age" +
			    "  from ts21 s21 with (nolock) inner join ta01 a01 with (nolock) on a01.pid=s21.pid" +
			    " where s21.pid=?" + 
			    "   and s21.exdt=?" +
			    "   and isnull(s21.ccfg,'') in ('','0')";
		return sql;
	}
	/*
	private String getSqlInter(){
		String sql =  
				"select s21.pat_id pid" +
				"     , a01.pat_name pnm" +
				"     , a01.pat_sex psex" +
				"     , s21.dept_code dptcd" +
				"     , 0 as age" +
			    "  from out_pat_visit_hist s21 with (nolock) inner join pat_mast a01 with (nolock) on a01.pat_id=s21.pat_id" +
			    " where s21.pat_id=?" + 
			    "   and s21.visit_date=?" +
			    "   and isnull(s21.cancel_flag,'') in ('','0')";
		return sql;
	}
	*/
}
