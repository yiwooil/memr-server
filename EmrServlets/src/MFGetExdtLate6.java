import java.sql.SQLException;
import java.util.HashMap;

public class MFGetExdtLate6  implements MFGet {
	private static MFGetExdtLate6 mInstance=null;
	private MFGetExdtLate6(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetExdtLate6 getInstance(){
		if(mInstance==null){
			mInstance = new MFGetExdtLate6();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String exdt = (String)param.get("exdt");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
		String returnString = "";
		SqlHelper sqlHelper;
		
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			boolean isJaincom = sqlHelper.isJaincom();
			String sql="";
			if(interfaceTableYn){
				sql = getSql();
			}else{
				sql = getSql();
			}
			para.put(++idx, pid); paraType.put(idx, "C");
			para.put(++idx, exdt); paraType.put(idx, "C");
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

	private String getSql() {
		String key="sql";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select isnull(min(x.exdt),'') exdt_late_6" +
			        "  from (" +
					"        select top 6 s21.exdt" +
					"          from ts21 s21" +
				    "         where s21.pid=?" +
					"           and s21.exdt<=?" +
				    "           and isnull(s21.ccfg,'') in ('','0')" +
					"         order by s21.exdt desc" +
			        "       ) x";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
}
