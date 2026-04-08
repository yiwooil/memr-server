import java.util.HashMap;


public class MFGetTprList implements MFGet {
	private static MFGetTprList mInstance=null;
	private MFGetTprList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetTprList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetTprList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");

		String pid2=""; // 자인컴은 id2가 있음.
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
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
			
			sql = getSqlInXml(hospitalId,"sql");
			paraString = getSqlInXml(hospitalId,"paraString");
			paraTypeString = getSqlInXml(hospitalId,"paraTypeString");
			if("".equalsIgnoreCase(sql)){
				if(interfaceTableYn){
					if(isJaincom){
						int pidLen = sqlHelper.getJainComPidLen(); // 2016.07.23 WOOIL
						String patientId = pid;
						if(patientId.length()<=pidLen){
							pid=patientId;
							pid2=" ";							
						}else{
							pid=patientId.substring(0, pidLen);
							pid2=patientId.substring(pidLen, pidLen+1);
						}
						sql = getSqlInterJaincom();
						paraString = "pid,pid2,bededt";
						paraTypeString = "C,C,D";
					}else{
						sql = getSqlInter();
						paraString = "pid,bededt";
						paraTypeString = "C,D";
					}
				}else{
					sql = getSql();
					paraString = "pid,bededt";
					paraTypeString = "C,D";
				}
			}
			// 일단 값을 보관한다.
			HashMap<String, Object> paraValue = new HashMap<String, Object>();
			paraValue.put("pid", pid);
			paraValue.put("pid2", pid2);
			paraValue.put("bededt", bededt);
			/*
			para.put(++idx, pid); paraType.put(idx, "C");
			if(isJaincom){
				para.put(++idx, pid2); paraType.put(idx, "C");
			}
			para.put(++idx, bededt); paraType.put(idx, "D");
			String rsString = sqlHelper.executeQuery(sql,para,paraType);
			*/
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
			        " order by u64.pid,u64.bededt,u64.chkdt desc,u64.chktm desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInter(){
		String key="Inter";
		if(sqlMap.containsKey(key)==false){
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
			        " order by u64.pat_id,u64.bed_in_date,u64.check_date desc,u64.check_time desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

	private String getSqlInterJaincom(){
		String key="InterJaincom";
		if(sqlMap.containsKey(key)==false){
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
				    "   and u64.pat_id2=?" +
			        "   and u64.bed_in_date=?" +
				    "   and u64.check_time is not null" +
			        " order by u64.pat_id,u64.bed_in_date,u64.check_date desc,u64.check_time desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInterTprSep(boolean isTprTableBedInDateNoYn){
		String key="InterTprSep";
		if(sqlMap.containsKey(key)==false){
			String sql = 
			"select x.chkdt,x.chktm,max(x.maxbp) maxbp,max(x.minbp) minbp,max(tmpcase) tmpcase,max(tmp) tmp,max(pr) pr,max(rr) rr,max(hod) hod,max(pod) pod " +
			"  from (" +
			"select a.check_date chkdt,a.check_time chktm,1 kind,a.bp_max maxbp,a.bp_min minbp,'' tmpcase,'' tmp,'' pr,'' rr,'' hod,'' pod " +
			"  from tpr_bp_hist a " + 
			" where a.pat_id=? ";
			if(isTprTableBedInDateNoYn==false){
			sql+=
			"   and a.bed_in_date=? ";
			}
			sql+=
			" union all " + 
			"select a.check_date chkdt,a.check_time chktm,2 kind,'' maxbp,'' minbp,a.temp_case tmpcase,a.temperature tmp,'' pr,'' rr,'' hod,'' pod " +
			"  from tpr_temp_hist a " + 
			" where a.pat_id=? ";
			if(isTprTableBedInDateNoYn==false){
			sql+=
			"   and a.bed_in_date=? ";
			}
			sql+=
			" union all " + 
			"select a.check_date chkdt,a.check_time chktm,3 kind,'' maxbp,'' minbp,'' tmpcase,'' tmp,a.pulse pr,'' rr,'' hod,'' pod " +
			"  from tpr_pulse_hist a " + 
			" where a.pat_id=? ";
			if(isTprTableBedInDateNoYn==false){
			sql+=
			"   and a.bed_in_date=? ";
			}
			sql+=
			" union all " +
			"select a.check_date chkdt,a.check_time chktm,4 kind,'' maxbp,'' minbp,'' tmpcase,'' tmp,'' pr,a.breath rr,'' hod,'' pod " +
			"  from tpr_breath_hist a " + 
			" where a.pat_id=? " +
			"   and a.bed_in_date=? " +
			" ) x " +
			" group by x.chkdt,x.chktm " +
			" order by 1 desc,2 desc ";
			
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInXml(String hospitalId, String mode) throws Exception{
		String sqlId = "tpr_list";
		HashMap<String,Object>param = new HashMap<String,Object>();
		param.put("hospitalid", hospitalId);
		param.put("sqlid", sqlId);
		param.put("mode", mode);
		MFGet instance = MFGetHospitalSql.getInstance();
		String sql = instance.getData(param);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getSqlInXml", "sql=" + sql);
		return sql;
	}
	
}
