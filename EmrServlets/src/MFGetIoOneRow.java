import java.util.HashMap;

public class MFGetIoOneRow implements MFGet {
	private static MFGetIoOneRow mInstance=null;
	private MFGetIoOneRow(){
		
	}
	
	public static MFGetIoOneRow getInstance(){
		if(mInstance==null){
			mInstance = new MFGetIoOneRow();
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
				"     , isnull(oral_c,'') oral_c" +
				"     , convert(numeric(6,0),isnull(oral_v,0)) oral_v" +
				"     , isnull(pate_c,'') pate_c" +
			    "     , convert(numeric(6,0),isnull(pate_v,0)) pate_v" +
			    "     , isnull(blood_c,'') blood_c" +
			    "     , convert(numeric(6,0),isnull(blood_v,0)) blood_v" +							
			    "     , convert(numeric(6,0),isnull(urine,0)) urine" +							
			    "     , convert(numeric(6,0),isnull(dr_su,0)) dr_su" +		
			    "     , isnull(s_v_o_c,'') s_v_o_c" +
			    "     , convert(numeric(6,0),isnull(s_v_o_v,0)) s_v_o_v" +							
			    "     , convert(numeric(6,0),isnull(stool,0)) stool" +							
			    "     , convert(numeric(6,0),isnull(vomit,0)) vomit" +							
			    "     , convert(numeric(6,0),isnull(others,0)) others" +
			    "  from tu57_2" +
		        " where pid=?" +
		        "   and bededt=?" +
		        "   and chkdt=?" +
		        "   and chktm=?" +
		        " order by pid,bededt,chkdt,chktm";
		return sql;
	}

}
