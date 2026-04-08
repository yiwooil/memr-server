import java.util.HashMap;

public class MFPutIo implements MFPut {
	private static MFPutIo mInstance=null;
	private MFPutIo(){
		
	}
	
	public static MFPutIo getInstance(){
		if(mInstance==null){
			mInstance = new MFPutIo();
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
		String oralc = (String)param.get("oralc");
		String oralv = (String)param.get("oralv");
		String patec = (String)param.get("patec");
		String patev = (String)param.get("patev");
		String bloodc = (String)param.get("bloodc");
		String bloodv = (String)param.get("bloodv");
		String urine = (String)param.get("urine");
		String drsu = (String)param.get("drsu");
		String svoc = (String)param.get("svoc");
		String svov = "";
		String stool = (String)param.get("stool");
		String vomit = (String)param.get("vomit");
		String others = (String)param.get("others");
		String userid = (String)param.get("userid");
		
		if("".equals(oralv)) oralv="0";
		if("".equals(patev)) patev="0";
		if("".equals(bloodv)) bloodv="0";
		if("".equals(urine)) urine="0";
		if("".equals(drsu)) drsu="0";
		if("".equals(svov)) svov="0";
		if("".equals(stool)) stool="0";
		if("".equals(vomit)) vomit="0";
		if("".equals(others)) others="0";

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
				}else{
					sql = getSqlUpdate();
					para.put(++idx, oralc);
					para.put(++idx, oralv);
					para.put(++idx, patec);
					para.put(++idx, patev);
					para.put(++idx, bloodc);
					para.put(++idx, bloodv);
					para.put(++idx, urine);
					para.put(++idx, drsu);
					para.put(++idx, svoc);
					para.put(++idx, svov);
					para.put(++idx, stool);
					para.put(++idx, vomit);
					para.put(++idx, others);
					para.put(++idx, userid);
					para.put(++idx, pid);
					para.put(++idx, bededt);
					para.put(++idx, chkdate);
					para.put(++idx, chktime);
				}
				System.out.println(Utility.getCurrentDateTime() + " MFPutIo - UPDATE 시작(" + sql + ")");
				String updResult=sqlHelper.executeUpdate(sql,para);
				System.out.println(Utility.getCurrentDateTime() + " MFPutIo - UPDATE 종료(" + updResult + ")");
				
			}
			else {
				// 새로 삽입
				sqlHelper = new SqlHelper(hospitalId);
				boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
				String sql="";
				if(interfaceTableYn){
				}else{
					sql=getSqlInsert();
					para.put(++idx, pid);
					para.put(++idx, bededt);
					para.put(++idx, chkdate);
					para.put(++idx, chktime);
					para.put(++idx, oralc);
					para.put(++idx, oralv);
					para.put(++idx, patec);
					para.put(++idx, patev);
					para.put(++idx, bloodc);
					para.put(++idx, bloodv);
					para.put(++idx, urine);
					para.put(++idx, drsu);
					para.put(++idx, svoc);
					para.put(++idx, svov);
					para.put(++idx, stool);
					para.put(++idx, vomit);
					para.put(++idx, others);
					para.put(++idx, userid);
				}
				System.out.println(Utility.getCurrentDateTime() + " MFPutIo - INSERT 시작(" + sql + ")");
				String insResult=sqlHelper.executeUpdate(sql,para);
				System.out.println(Utility.getCurrentDateTime() + " MFPutIo - INSERT 종료(" + insResult + ")");
				
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
			    "  from tu57_2" +
		        " where pid=?" +
		        "   and bededt=?" +
		        "   and chkdt=?" +
		        "   and chktm=?";
		return sql;
	}
	
	
	private String getSqlUpdate(){
		String sql =
				"update tu57_2" +
				"   set oral_c=?" +
				"     , oral_v=?" +
				"     , pate_c=?" +
				"     , pate_v=?" +
				"     , blood_c=?" +
				"     , blood_v=?" +
				"     , urine=?" +
				"     , dr_su=?" +
				"     , s_v_o_c=?" +
				"     , s_v_o_v=?" +
				"     , stool=?" +
				"     , vomit=?" +
				"     , others=?" +
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
				"insert into tu57_2(pid,bededt,chkdt,chktm,oral_c,oral_v,pate_c,pate_v,blood_c,blood_v,urine,dr_su,s_v_o_c,s_v_o_v,stool,vomit,others,eid)" +
				"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" +
				"";
		return sql;
	}
}
