import java.util.HashMap;


public class MFPutTpr implements MFPut {
	private static MFPutTpr mInstance=null;
	private MFPutTpr(){
		
	}
	
	public static MFPutTpr getInstance(){
		if(mInstance==null){
			mInstance = new MFPutTpr();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String chkdate = (String)param.get("chkdate");
		String chktime = (String)param.get("chktime");
		String bp = (String)param.get("bp");
		String bpmax = (String)param.get("bpmax");
		String bpmin = (String)param.get("bpmin");
		String tmp = (String)param.get("tmp");
		String tmpcase = (String)param.get("tmpcase");
		String pr = (String)param.get("pr");
		String rr = (String)param.get("rr");
		String userid = (String)param.get("userid");

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString="OK";
		SqlHelper sqlHelper;
		try {
			int idx=0;
			if(isTpr(hospitalId,pid,bededt,chkdate,chktime)==true){
				// 업데이트
				sqlHelper = new SqlHelper(hospitalId);
				boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
				String sql="";
				if(interfaceTableYn){
					sql = getSqlUpdateInter();
					para.put(++idx, bpmax);
					para.put(++idx, bpmin);
					para.put(++idx, tmpcase);
					para.put(++idx, tmp);
					para.put(++idx, pr);
					para.put(++idx, rr);
					para.put(++idx, userid);
					para.put(++idx, pid);
					para.put(++idx, bededt);
					para.put(++idx, chkdate);
					para.put(++idx, chktime);
				}else{
					sql = getSqlUpdate();
					para.put(++idx, bp);
					para.put(++idx, tmpcase);
					para.put(++idx, tmp);
					para.put(++idx, pr);
					para.put(++idx, rr);
					para.put(++idx, userid);
					para.put(++idx, pid);
					para.put(++idx, bededt);
					para.put(++idx, chkdate);
					para.put(++idx, chktime);
				}
				System.out.println(Utility.getCurrentDateTime() + " MFPutTpr - UPDATE 시작(" + sql + ")");
				String updResult=sqlHelper.executeUpdate(sql,para);
				System.out.println(Utility.getCurrentDateTime() + " MFPutTpr - UPDATE 종료(" + updResult + ")");
			}
			else {
				// 새로 삽입
				sqlHelper = new SqlHelper(hospitalId);
				boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
				String sql="";
				if(interfaceTableYn){
					sql=getSqlInsertInter();
					para.put(++idx, pid);
					para.put(++idx, bededt);
					para.put(++idx, chkdate);
					para.put(++idx, chktime);
					para.put(++idx, bpmax);
					para.put(++idx, bpmin);
					para.put(++idx, tmpcase);
					para.put(++idx, tmp);
					para.put(++idx, pr);
					para.put(++idx, rr);
					para.put(++idx, userid);
				}else{
					sql=getSqlInsert();
					para.put(++idx, pid);
					para.put(++idx, bededt);
					para.put(++idx, chkdate);
					para.put(++idx, chktime);
					para.put(++idx, bp);
					para.put(++idx, tmpcase);
					para.put(++idx, tmp);
					para.put(++idx, pr);
					para.put(++idx, rr);
					para.put(++idx, userid);
				}
				System.out.println(Utility.getCurrentDateTime() + " MFPutTpr - INSERT 시작(" + sql + ")");
				String insResult=sqlHelper.executeUpdate(sql,para);
				System.out.println(Utility.getCurrentDateTime() + " MFPutTpr - INSERT 종료(" + insResult + ")");
				
			}
		}
		catch (Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "Exception", e.getLocalizedMessage());
			returnString=e.getMessage();
		}
		return returnString;
	}
	
	private boolean isTpr(String hospitalId, String pid, String bededt, String chkdate, String chktime) throws Exception {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		boolean is = false;
		int idx=0;
		SqlHelper sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql="";
		if(interfaceTableYn){
			sql=getSqlInter();
		}else{
			sql=getSql();
		}
		para.put(++idx, pid); paraType.put(idx, "C");
		para.put(++idx, bededt); paraType.put(idx, "D");
		para.put(++idx, chkdate); paraType.put(idx, "D");
		para.put(++idx, chktime); paraType.put(idx, "C");
		String rsString = sqlHelper.executeQuery(sql,para,paraType);
		ResultSetHelper rs = new ResultSetHelper(rsString);
		is = (rs.getRecordCount()>0);
		return is;
	}
	
	private String getSql(){
		String sql = 
				"select * " +
			    "  from tu64 u64" +
		        " where u64.pid=?" +
		        "   and u64.bededt=?" +
		        "   and u64.chkdt=?" +
		        "   and u64.chktm=?";
		return sql;
	}
	
	private String getSqlInter(){
		String sql = 
				"select * " +
			    "  from tpr_hist u64" +
		        " where u64.pat_id=?" +
		        "   and u64.bed_in_date=?" +
		        "   and u64.check_date=?" +
		        "   and u64.check_time=?";
		return sql;
	}
	
	private String getSqlUpdate(){
		String sql =
				"update tu64" +
				"   set bp=?" +
				"     , tmpcase=?" +
				"     , tmp=?" +
				"     , pr=?" +
				"     , rr=?" +
				"     , updid=?" +
		        " where pid=?" +
		        "   and bededt=?" +
		        "   and chkdt=?" +
		        "   and chktm=?" +
		        "";
		return sql;
	}
	
	private String getSqlUpdateInter(){
		String sql =
				"update tpr_enter_hist" +
				"   set bp_max=?" +
				"     , bp_min=?" +
				"     , temp_case=?" +
				"     , temperature=?" +
				"     , pulse=?" +
				"     , breath=?" +
				"     , upd_id=?" +
		        " where pat_id=?" +
		        "   and bed_in_date=?" +
		        "   and check_date=?" +
		        "   and check_time=?" +
		        "";
		return sql;
	}
	
	private String getSqlInsert(){
		String sql =
				"insert into tu64(pid,bededt,chkdt,chktm,bp,tmpcase,tmp,pr,rr,eid)" +
				"values(?,?,?,?,?,?,?,?,?,?)" +
				"";
		return sql;
	}

	private String getSqlInsertInter(){
		String sql =
				"insert into tpr_enter_hist(pat_id,bed_in_date,check_date,check_time,bp_max,bp_min,temp_case,temperature,pulse,breath,reg_id)" +
				"values(?,?,?,?,?,?,?,?,?,?,?)" +
				"";
		return sql;
	}
}
