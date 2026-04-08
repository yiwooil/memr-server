import java.util.HashMap;

public class MFGetIoList implements MFGet {
	private static MFGetIoList mInstance=null;
	private MFGetIoList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetIoList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetIoList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");

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
			
			sql = getSql();
			paraString = "pid,bededt";
			paraTypeString = "C,D";
			
			// 일단 값을 보관한다.
			HashMap<String, Object> paraValue = new HashMap<String, Object>();
			paraValue.put("pid", pid);
			paraValue.put("bededt", bededt);
			
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
					"select isnull(u57_2.oral_v,0) oral_v" +
				    "     , isnull(u57_2.pate_v,0) pate_v" +
				    "     , isnull(u57_2.blood_v,0) blood_v" +							
				    "     , isnull(u57_2.urine,0) urine" +							
				    "     , isnull(u57_2.dr_su,0) dr_su" +							
				    "     , isnull(u57_2.s_v_o_v,0) s_v_o_v" +							
				    "     , isnull(u57_2.stool,0) stool" +							
				    "     , isnull(u57_2.vomit,0) vomit" +							
				    "     , isnull(u57_2.others,0) others" +
				    "     , u57_2.chkdt" +
				    "     , u57_2.chktm" +
				    "     , a04.wardid" +
				    "     , case when CHKTM between substring(a88.fld1qty,1,4) and SUBSTRING(a88.fld1qty,5,4) then 'D'" +
				    "            when CHKTM between substring(a88.fld1qty,2,4) and SUBSTRING(a88.fld2qty,5,4) then 'E'" +
				    "            when CHKTM between substring(a88.fld3qty,1,4) and '2400' then 'N'" +
				    "            when CHKTM between '0000' and SUBSTRING(a88.fld3qty,5,4) then 'N'" +
				    "            else 'D'" +
				    "       end duty_name" +
				    "  from tu57_2 u57_2 inner join TA04 a04 on a04.PID=u57_2.PID and a04.BEDEDT=u57_2.bededt" +
				    "                    inner join ta88 a88 on a88.MST1CD='duty_time' and a88.MST2CD='2' and a88.MST3CD=a04.wardid" +
			        " where u57_2.pid=?" +
			        "   and u57_2.bededt=?" +
			        " order by u57_2.pid,u57_2.bededt,u57_2.chkdt desc,u57_2.chktm desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
}
