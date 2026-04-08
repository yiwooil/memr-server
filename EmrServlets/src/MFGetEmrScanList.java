import java.sql.SQLException;
import java.util.HashMap;

/***
 * 
 * @author WILEE
 *
 * 2013.09.09 WOOIL - 자인컴인 경우 file_path2가 있음.
 * 
 */
public class MFGetEmrScanList implements MFGet {
	private static MFGetEmrScanList mInstance=null;
	private MFGetEmrScanList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetEmrScanList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetEmrScanList();
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
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			//boolean isJaincom = sqlHelper.isJaincom();
			String sql="";
			//if(interfaceTableYn){
			//	boolean isOracle = sqlHelper.isOracle();
			//	if(isJaincom){
			//		int pidLen = sqlHelper.getJainComPidLen(); // 2016.07.23 WOOIL
			//		String patientId = pid;
			//		if(patientId.length()<=pidLen){
			//			pid=patientId;
			//			pid2=" ";							
			//		}else{
			//			pid=patientId.substring(0, pidLen);
			//			pid2=patientId.substring(pidLen, pidLen+1);
			//		}
			//		sql = getSqlInterOracleJaincom();
			//	}else if(isOracle){
			//		sql = getSqlInterOracle();
			//	}else{
			//		sql = getSqlInter();
			//	}
			//}else{
				sql = getSql();
			//}
			para.put(++idx, pid); paraType.put(idx, "C");
			//if(isJaincom){
			//	para.put(++idx, pid2); paraType.put(idx, "C");
			//}
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
			String sql = ""; 
			sql += "select g02.pid,g02.bdiv,g02.exdt,g02.seq,g02.rptcd" + "\r\n";
			sql += "     , case when g02.rptcd='ZZ01'" + "\r\n";
			sql += "		     then isnull(g02.rptnm2,'') + ' ' + '(모바일)'" + "\r\n"; 
			sql += "            else (case when isnull(g01.rptnm,'')='' then isnull(g02.rptnm2,'') else isnull(g01.rptnm,'') end)" + "\r\n";
			sql += "       end as rptnm" + "\r\n";
			sql += " 	  , g02.path,null path2" + "\r\n";
			sql += "  from tg02 g02 left join tg01 g01 on g01.rptcd=g02.rptcd" + "\r\n";
			sql += " where g02.pid=?" + "\r\n";
			sql += "   and g02.exdt between ? and ?" + "\r\n";
			sql += "   and isnull(g02.deldt,'')=''" + "\r\n"; // 2022.02.22 WOOIL - 삭제된 내역 제외
			sql += "   and isnull(g02.rptcd,'') not in ('TK92')" + "\r\n"; // 2024.12.11 WOOIL - 서식생성기 사인 이미지.
			sql += " order by g02.exdt desc,convert(numeric,g02.seq) desc" + "\r\n";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

	//private String getSqlInter(){
	//	String key="Inter";
	//	if(sqlMap.containsKey(key)==false){
	//		String sql = 
	//				"select g02.pat_id pid,g02.in_out_flag bdiv,g02.scan_date exdt,g02.scan_seq seq,g02.scan_class rptcd" +
	//		        "     , case when isnull(g01.scan_class_name,'')='' then isnull(g02.sub_class_name,'')+'.' else isnull(g01.scan_class_name,'') end as rptnm" +
	//				"     , g02.file_path path,null path2 " +
	//				"  from scan_hist g02 left join scan_class_mast g01 on g01.scan_class=g02.scan_class " +
	//			    " where g02.pat_id=?" +
	//			    "   and g02.scan_date between ? and ?" +
	//				" order by g02.scan_date desc,g02.scan_seq desc ";
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}
	
	//private String getSqlInterOracle(){
	//	String key="InterOracle";
	//	if(sqlMap.containsKey(key)==false){
	//		String sql = 
	//				"select g02.pat_id pid,g02.in_out_flag bdiv,g02.scan_date exdt,g02.scan_seq seq,g02.scan_class rptcd,nvl(g01.scan_class_name,' ')||nvl(g02.sub_class_name,' ') as rptnm,g02.file_path path,null path2 " +
	//				"  from scan_hist g02 left join scan_class_mast g01 on g01.scan_class=g02.scan_class " +
	//			    " where g02.pat_id=?" +
	//			    "   and g02.scan_date between ? and ?" +
	//				" order by g02.scan_date desc,g02.scan_seq desc ";
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}

	//private String getSqlInterOracleJaincom(){
	//	String key="InterOracleJaincom";
	//	if(sqlMap.containsKey(key)==false){
	//		String sql = 
	//				"select g02.pat_id pid,g02.in_out_flag bdiv,g02.scan_date exdt,g02.scan_seq seq,g02.scan_class rptcd" +
	//				"     , ltrim(nvl(g01.scan_class_name,' ')||nvl(g02.sub_class_name,' ')) as rptnm" +
	//				"     , g02.file_path path,g02.file_path2 path2 " +
	//				"  from scan_hist g02 left join scan_class_mast g01 on g01.scan_class=g02.scan_class " +
	//			    " where g02.pat_id=?" +
	//				"   and g02.pat_id2=?" +
	//			    "   and g02.scan_date between ? and ?" +
	//				" order by g02.scan_date desc,g02.scan_seq desc ";
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}
	
}
