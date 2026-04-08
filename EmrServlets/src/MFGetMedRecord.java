import java.sql.SQLException;
import java.util.HashMap;


public class MFGetMedRecord implements MFGet {
	private static MFGetMedRecord mInstance=null;
	private MFGetMedRecord(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetMedRecord getInstance(){
		if(mInstance==null){
			mInstance = new MFGetMedRecord();
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
			String paraString="";
			String paraTypeString="";
			
			sql = getSqlInXml(hospitalId,"sql");
			paraString = getSqlInXml(hospitalId,"paraString");
			paraTypeString = getSqlInXml(hospitalId,"paraTypeString");
			//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "paraString="+paraString+", paraTypeString="+paraTypeString);
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
						paraString = "pid,pid2,bededt,frdt,todt";
						paraTypeString = "C,C,D,D,D";
					}else{
						sql = getSqlInter();
						paraString = "pid,bededt,frdt,todt";
						paraTypeString = "C,D,D,D";
					}
				}else{
					sql = getSql();
					paraString = "pid,bededt,frdt,todt";
					paraTypeString = "C,D,D,D";
				}
			}
			// 일단 값을 보관한다.
			HashMap<String, Object> paraValue = new HashMap<String, Object>();
			paraValue.put("pid", pid);
			paraValue.put("pid2", pid2);
			paraValue.put("bededt", bededt);
			paraValue.put("frdt", frdt);
			paraValue.put("todt", todt);
			/*
			para.put(++idx, pid); paraType.put(idx, "C");
			if(isJaincom){
				para.put(++idx, pid2); paraType.put(idx, "C");
			}
			para.put(++idx, bededt); paraType.put(idx, "D");
			para.put(++idx, frdt); paraType.put(idx, "D");
			para.put(++idx, todt); paraType.put(idx, "D");
			
			String rsString = sqlHelper.executeQuery(sql,para,paraType);
			*/
			String rsString = sqlHelper.executeQuery(sql,paraValue,paraString,paraTypeString);
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
					"select v20.ocd,a18.onm,v20.dunit,v20.dodt,sum(v20.dqty) as dqty,sum(v20.ordcnt) as ordcnt" +
					"  from tv20 v20 inner join ta18 a18 on a18.ocd=v20.ocd and a18.credt=(select max(x.credt) from ta18 x where x.ocd=v20.ocd and x.credt<=v20.dodt)" +
				    " where v20.pid=?" +
				    "   and v20.bededt=?" +
				    "   and v20.dodt between ? and ?" +
				    "   and v20.odivcd like 'm%'" +
				    " group by v20.ocd,a18.onm,v20.dunit,v20.dodt" +
					" order by v20.ocd,a18.onm,v20.dunit,v20.dodt desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInter(){
		String key="Inter";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select v20.order_code ocd,a18.code_name onm,v20.order_unit dunit,v20.med_date dodt,sum(v20.med_quantity) as dqty,1 as ordcnt" +
					"  from med_record_hist v20 inner join order_code_mast a18 on a18.order_code=v20.order_code and a18.start_date=(select max(x.start_date) from order_code_mast x where x.order_code=v20.order_code and x.start_date<=v20.med_date)" +
				    " where v20.pat_id=?" +
				    "   and v20.bed_in_date=?" +
				    "   and v20.med_date between ? and ?" +
				    " group by v20.order_code,a18.code_name,v20.order_unit,v20.med_date" +
					" order by v20.order_code,a18.code_name,v20.order_unit,v20.med_date desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

	private String getSqlInterJaincom(){
		String key="InterJaincom";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select a.ocd,a.onm,a.dunit,a.dodt,sum(a.dqty) as dqty,1 as ordcnt" +
			        "  from (" +
					"    select v20.order_code ocd,(select a18.code_name from order_code_mast a18 where a18.order_code=v20.order_code and a18.start_date in (select max(x.start_date) from order_code_mast x where x.order_code=v20.order_code and x.start_date<=v20.med_date) and rownum<2) onm,v20.order_unit dunit,v20.med_date dodt,v20.med_quantity as dqty" +
					"      from med_record_hist v20" +
				    "     where v20.pat_id=?" +
					"       and v20.pat_id2=?" +
				    "       and v20.bed_in_date=?" +
				    "       and v20.med_date between ? and ?" +
				    " ) a" +
				    " group by a.ocd,a.onm,a.dunit,a.dodt" +
					" order by a.ocd,a.onm,a.dunit,a.dodt desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInXml(String hospitalId, String mode) throws Exception{
		String sqlId = "med_record";
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
