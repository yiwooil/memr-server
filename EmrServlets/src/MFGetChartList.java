import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetChartList implements MFGet {
	private static MFGetChartList mInstance=null;
	private MFGetChartList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetChartList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetChartList();
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
		String bdiv = (String)param.get("bdiv");

		String pid2=""; // 자인컴은 id2가 있음.
		if(bdiv==null) bdiv="2";
		
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
					if(bdiv.equalsIgnoreCase("1")){
						sql = getSqlOut();
						paraString = "pid,frdt,todt";
						paraTypeString = "C,D,D";
					}else{
						sql = getSql();
						paraString = "pid,bededt,frdt,todt";
						paraTypeString = "C,D,D,D";
					}
				}
			}
			new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "sql=" + sql);
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
			if(isJaincom) returnString = convertRtfToPlain(returnString);
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
				"select e12c.exdt,e12c.c_case,e12c.rmk1,e12c.bdiv" +
				"  from te12c e12c" +
			    " where e12c.pid=?" +
			    "   and e12c.bededt=?" +
			    "   and e12c.exdt between ? and ?" +
				" order by e12c.exdt desc,e12c.bdiv,e12c.userid,e12c.seq,e12c.sort_seq";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

	private String getSqlOut() {
		String key="sql_out";
		if(sqlMap.containsKey(key)==false){
			String sql = 
				"select e12c.exdt,e12c.c_case,e12c.rmk1,e12c.bdiv" +
				"  from te12c e12c" +
			    " where e12c.pid=?" +
			    "   and e12c.exdt between ? and ?" +
				" order by e12c.exdt desc,e12c.bdiv,e12c.userid,e12c.seq,e12c.sort_seq";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInter() {
		String key="Inter";
		if(sqlMap.containsKey(key)==false){
			String sql = 
				"select e12c.chart_date exdt,e12c.chart_case c_case,e12c.remark1 rmk1,e12c.in_out_flag bdiv" +
				"  from chart_hist e12c" +
			    " where e12c.pat_id=?" +
			    "   and e12c.bed_in_date=?" +
			    "   and e12c.chart_date between ? and ?" +
				" order by e12c.chart_date desc,e12c.in_out_flag,e12c.doctor_id,e12c.chart_no,e12c.chart_seq";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

	private String getSqlInterJaincom() {
		String key="InterJaincom";
		if(sqlMap.containsKey(key)==false){
			String sql = 
				"select e12c.chart_date exdt,e12c.chart_case c_case,e12c.remark1 rmk1,e12c.in_out_flag bdiv" +
				"  from chart_hist e12c" +
			    " where e12c.pat_id=?" +
				"   and e12c.pat_id2=?" +
			    "   and e12c.bed_in_date=?" +
			    "   and e12c.chart_date between ? and ?" +
				" order by e12c.chart_date desc,e12c.in_out_flag,e12c.doctor_id,e12c.chart_no,e12c.chart_seq";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String convertRtfToPlain(String rsString) {
		String returnString="";
		try {
			JSONArray rowData = new JSONArray();
			JSONObject columns = null;
			
			ResultSetHelper rs = new ResultSetHelper(rsString);
			int cnt=rs.getRecordCount();
			if(cnt>0){
				for(int i=0;i<cnt;i++){
					columns = new JSONObject();
					rowData.add(columns);
					columns.put("exdt", rs.getString(i,"exdt"));
					columns.put("c_case", rs.getString(i,"c_case"));
					columns.put("rmk1", Utility.rtfToPlainText(rs.getString(i,"rmk1")));
					columns.put("bdiv", rs.getString(i,"bdiv"));
				}
			}
			// 리턴값과 메시지
			JSONArray status = new JSONArray();
			JSONArray result = new JSONArray();
			//
			columns = new JSONObject();
			columns.put("return_code",cnt);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			result.add(rowData);
			//
			returnString=result.toJSONString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "convertRtfToPlain", "JSONException", e.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "convertRtfToPlain", "Exception", e.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(e);
		} finally {
			return returnString;
		}
	}
	
	private String getSqlInXml(String hospitalId, String mode) throws Exception{
		String sqlId = "chart_hist";
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
