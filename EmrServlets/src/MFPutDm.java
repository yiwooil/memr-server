import java.util.HashMap;

public class MFPutDm implements MFPut {
	private static MFPutDm mInstance=null;
	private MFPutDm(){
		
	}
	
	public static MFPutDm getInstance(){
		if(mInstance==null){
			mInstance = new MFPutDm();
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
		String nvalue = (String)param.get("nvalue");
		String userid = (String)param.get("userid");

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString="OK";
		SqlHelper sqlHelper;
		try {
			int idx=0;
			if(isDm(hospitalId,pid,bededt,chkdate,chktime)==true){
				// 업데이트
				sqlHelper = new SqlHelper(hospitalId);
				boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
				String sql="";
				if(interfaceTableYn){
					//sql = getSqlUpdateInter();
					//para.put(++idx, nvalue);
					//para.put(++idx, userid);
					//para.put(++idx, pid);
					//para.put(++idx, bededt);
					//para.put(++idx, chkdate);
					//para.put(++idx, chktime);
				}else{
					sql = getSqlUpdate();
					para.put(++idx, nvalue);
					para.put(++idx, userid);
					para.put(++idx, pid);
					para.put(++idx, bededt);
					para.put(++idx, chkdate);
					para.put(++idx, chktime);
				}
				System.out.println(Utility.getCurrentDateTime() + " MFPutDm - UPDATE 시작(" + sql + ")");
				String updResult=sqlHelper.executeUpdate(sql,para);
				System.out.println(Utility.getCurrentDateTime() + " MFPutDm - UPDATE 종료(" + updResult + ")");
				
			}
			else {
				// 새로 삽입
				sqlHelper = new SqlHelper(hospitalId);
				boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
				String sql="";
				if(interfaceTableYn){
					//sql=getSqlInsertInter();
					//para.put(++idx, pid);
					//para.put(++idx, bededt);
					//para.put(++idx, chkdate);
					//para.put(++idx, chktime);
					//para.put(++idx, userid);
				}else{
					sql=getSqlInsert();
					para.put(++idx, pid);
					para.put(++idx, bededt);
					para.put(++idx, chkdate);
					para.put(++idx, chktime);
					para.put(++idx, nvalue);
					para.put(++idx, userid);
				}
				System.out.println(Utility.getCurrentDateTime() + " MFPutDm - INSERT 시작(" + sql + ")");
				String insResult=sqlHelper.executeUpdate(sql,para);
				System.out.println(Utility.getCurrentDateTime() + " MFPutDm - INSERT 종료(" + insResult + ")");
				
			}
		}
		catch (Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "Exception", e.getLocalizedMessage());
			returnString=e.getMessage();
		}
		return returnString;
	}
	
	private boolean isDm(String hospitalId, String pid, String bededt, String chkdate, String chktime) throws Exception {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		boolean is = false;
		int idx=0;
		SqlHelper sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql="";
		if(interfaceTableYn){
			//sql=getSqlInter();
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
			    "  from tu59_2" +
		        " where pid=?" +
		        "   and bededt=?" +
		        "   and chkdt=?" +
		        "   and chktm=?";
		return sql;
	}
	
	
	private String getSqlUpdate(){
		String sql =
				"update tu59_2" +
				"   set n_value=?" +
				"     , updid=?" +
		        " where pid=?" +
		        "   and bededt=?" +
		        "   and chkdt=?" +
		        "   and chktm=?" +
		        "";
		return sql;
	}
	
	private String getSqlInsert(){
		String sql =
				"insert into tu59_2(pid,bededt,chkdt,chktm,n_value,eid,unit)" +
				"values(?,?,?,?,?,?,'mg/dl')" +
				"";
		return sql;
	}

}
