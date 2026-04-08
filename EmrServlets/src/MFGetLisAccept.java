import java.sql.SQLException;
import java.util.HashMap;


public class MFGetLisAccept implements MFGet {
	private static MFGetLisAccept mInstance=null;
	private MFGetLisAccept(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetLisAccept getInstance(){
		if(mInstance==null){
			mInstance = new MFGetLisAccept();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String frdt = (String)param.get("frdt");
		String todt = (String)param.get("todt");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
		String returnString = "";
		SqlHelper sqlHelper;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			String sql=getSql(); 
			para.put(++idx, pid); paraType.put(idx, "C");
			para.put(++idx, frdt); paraType.put(idx, "D");
			para.put(++idx, todt); paraType.put(idx, "D");
			String rsString = sqlHelper.executeQuery(sql,para,paraType);
			returnString = rsString;
		}catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getSql(){
		String key="sql";
		if(sqlMap.containsKey(key)==false){
			String sql = 
				"select orddt,deptcd,ward,room,ordnm,alltestnm,spcnm,stscd,rcvdt,rcvtm,vfydt,vfytm,rtrim(spcno) as spcno" +
			    "     , case stscd when '0' then '처방'" +
			    "                  when '1' then '채혈/채취'" + 
			    "                  when '2' then '접수'" + 
			    "                  when '3' then '검사'" + 
			    "                  when '4' then '입력중'" + 
			    "                  when '5' then '중간결과'" + 
			    "                  when '6' then '부분결과'" + 
			    "                  when '7' then '결과'" + 
			    "                  when '8' then '수정'" + 
			    "                  when '9' then '출력'" + 
			    "       end as stsnm" +
				"  from tc201" +
			    " where ptid=?" +
			    "   and orddt between ? and ?" +
			    "   and (cancelfg != '1' or cancelfg is null)" +
				" order by orddt desc,spcno";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

}
