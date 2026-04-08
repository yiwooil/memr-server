import java.sql.SQLException;
import java.util.HashMap;


public class MFGetMedRecordMinMaxDate implements MFGet {
	private static MFGetMedRecordMinMaxDate mInstance=null;
	private MFGetMedRecordMinMaxDate(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetMedRecordMinMaxDate getInstance(){
		if(mInstance==null){
			mInstance = new MFGetMedRecordMinMaxDate();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String frdt = (String)param.get("frdt");
		String todt = (String)param.get("todt");

		String pid2=""; // 자인컴은 id2가 있음.
		
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
				boolean isOracle = sqlHelper.isOracle();
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
					sql = getSqlInterOracleJaincom();
				}else if(isOracle){
					sql = getSqlInterOracle();
				}else{
					sql = getSqlInter();
				}
			}else{
				sql = getSql();
			}
			para.put(++idx, pid); paraType.put(idx, "C");
			if(isJaincom){
				para.put(++idx, pid2); paraType.put(idx, "C");
			}
			para.put(++idx, bededt); paraType.put(idx, "D");
			para.put(++idx, frdt); paraType.put(idx, "D");
			para.put(++idx, todt); paraType.put(idx, "D");
			sqlHelper = new SqlHelper(hospitalId);
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
					"select isnull(min(v20.dodt),'') as mindodt,isnull(max(v20.dodt),'') as maxdodt" +
					"  from tv20 v20 inner join ta18 a18 on a18.ocd=v20.ocd and a18.credt=(select max(x.credt) from ta18 x where x.ocd=v20.ocd and x.credt<=v20.dodt)" +
				    " where v20.pid=?" +
				    "   and v20.bededt=?" +
				    "   and v20.dodt between ? and ?" +
				    "   and v20.odivcd like 'm%' ";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInter(){
		String key="Inter";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select isnull(min(v20.med_date),'') as mindodt,isnull(max(v20.med_date),'') as maxdodt" +
					"  from med_record_hist v20 inner join order_code_mast a18 on a18.order_code=v20.order_code and a18.start_date=(select max(x.start_date) from order_code_mast x where x.order_code=v20.order_code and x.start_date<=v20.med_date)" +
				    " where v20.pat_id=?" +
				    "   and v20.bed_in_date=?" +
				    "   and v20.med_date between ? and ?";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInterOracle(){
		String key="InterOracle";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select min(v20.med_date) as mindodt,max(v20.med_date) as maxdodt" +
					"  from med_record_hist v20 inner join order_code_mast a18 on a18.order_code=v20.order_code and a18.start_date=(select max(x.start_date) from order_code_mast x where x.order_code=v20.order_code and x.start_date<=v20.med_date)" +
				    " where v20.pat_id=?" +
				    "   and v20.bed_in_date=?" +
				    "   and v20.med_date between ? and ?";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

	private String getSqlInterOracleJaincom(){
		String key="InterOracleJaincom";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select min(v20.med_date) as mindodt,max(v20.med_date) as maxdodt" +
					"  from med_record_hist v20 inner join order_code_mast a18 on a18.order_code=v20.order_code and a18.start_date=(select max(x.start_date) from order_code_mast x where x.order_code=v20.order_code and x.start_date<=v20.med_date)" +
				    " where v20.pat_id=?" +
					"   and v20.pat_id2=?" +
				    "   and v20.bed_in_date=?" +
				    "   and v20.med_date between ? and ?";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
}
