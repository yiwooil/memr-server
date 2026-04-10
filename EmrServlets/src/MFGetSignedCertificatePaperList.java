import java.sql.SQLException;
import java.util.HashMap;


public class MFGetSignedCertificatePaperList implements MFGet {
	private static MFGetSignedCertificatePaperList mInstance=null;
	private MFGetSignedCertificatePaperList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetSignedCertificatePaperList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetSignedCertificatePaperList();
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

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString = "";
		SqlHelper sqlHelper;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			//String sql="";
			//sql = getSqlInXml(hospitalId); // xml에 저장되어있는 sql문을 찾아본다.
			////new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "sql=" + sql);
			//if("".equalsIgnoreCase(sql)) sql = getSql(interfaceTableYn); // xml에 sql이 없음.
			
			String sql = "";
			sql += "select g02.pid" + "\r\n";
			sql += "     , g02.bdiv" + "\r\n";
			sql += "     , g02.exdt" + "\r\n";
			sql += "     , g02.seq" + "\r\n";
			sql += "     , g02.rptcd" + "\r\n";
			sql += "     , isnull(g02.rptnm2,'') as rptnm" + "\r\n";
			sql += "     , g02.path" + "\r\n";
			sql += "     , null path2" + "\r\n";
			sql += "     , isnull(g02.sub_page_list,'') as sub_page_list" + "\r\n";
			sql += "     , isnull(g02.sub_page_no,'') as sub_page_no " + "\r\n";
			sql += "     , g02.ccf_id" + "\r\n"; // 2026.02.04 WOOIL - 추가.
			sql += "     , g02.rptnm2 as ccf_name" + "\r\n"; // 2026.02.04 WOOIL - 추가.
			sql += "     , g02.path as ccf_filename" + "\r\n"; // 2026.02.04 WOOIL - 추가.
			sql += "     , g02.rptcd2 as emr_scan_class" + "\r\n"; // 2026.02.04 WOOIL - 추가.
			sql += "     , isnull(g02.dptcd,'') as dptcd" + "\r\n"; // 2026.02.04 WOOIL - 추가.
			sql += "     , isnull(g02.drid,'') as drid" + "\r\n"; // 2026.02.04 WOOIL - 추가.
			sql += "     , isnull(g02.qfycd,'') as qfycd" + "\r\n"; // 2026.02.04 WOOIL - 추가.
			sql += "     , isnull(g02.tsa_status,'') as tsa_status" + "\r\n"; // 2026.04.10 WOOIL - 추가
			sql += "     , isnull(g02.tsa_date,'') as tsa_date" + "\r\n"; // 2026.04.10 WOOIL - 추가
			sql += "  from tg02 g02 " + "\r\n";
			sql += " where g02.pid=?" + "\r\n";
			sql += "   and g02.exdt between ? and ?" + "\r\n";
			sql += "   and g02.rptcd='ZZ01'" + "\r\n";
			sql += "   and isnull(g02.deldt,'')=''" + "\r\n"; // 2022.03.02 WOOIL - 삭제된 내역 제외
			sql += "   and isnull(g02.afexdt,'')=''" + "\r\n"; // 2026.02.04 WOOIL - 동의서를 다시 받아서 제외 
			sql += " order by g02.exdt desc,convert(numeric,g02.seq) desc " + "\r\n";
			
			para.put(++idx, pid);
			para.put(++idx, frdt);
			para.put(++idx, todt);
			String rsString = sqlHelper.executeQuery(sql,para,null);
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
	
	private String getSql(boolean interfaceTableYn){
		String key="sql," + interfaceTableYn;
		if(sqlMap.containsKey(key)==false){
			String sql = "";
			if(interfaceTableYn){
				sql =
					"select g02.pat_id pid,g02.in_out_flag bdiv,g02.scan_date exdt,g02.scan_seq seq,g02.scan_class rptcd,g02.sub_class_name as rptnm,g02.file_path path,null path2 " +
					"  from signed_consent_form_hist g02" +
					" where g02.pat_id=?" +
					"   and g02.scan_date between ? and ?" +
					"   and g02.scan_class='ZZ01'" +
					" order by g02.scan_date desc,convert(numeric,g02.scan_seq) desc ";
			}else{
				sql =
					"select g02.pid,g02.bdiv,g02.exdt,g02.seq,g02.rptcd,isnull(g02.rptnm2,'') as rptnm,g02.path,null path2,isnull(sub_page_list,'') as sub_page_list,isnull(sub_page_no,'') as sub_page_no " +
					"  from tg02 g02 " +
					" where g02.pid=?" +
					"   and g02.exdt between ? and ?" +
					"   and g02.rptcd='ZZ01'" +
					"   and isnull(g02.deldt,'')=''" + // 2022.03.02 WOOIL - 삭제된 내역 제외
					" order by g02.exdt desc,convert(numeric,g02.seq) desc ";
			}
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInXml(String hospitalId) throws Exception{
		HashMap<String,Object>param = new HashMap<String,Object>();
		param.put("hospitalid", hospitalId);
		param.put("sqlid", "signed_certificate_paper_list");
		MFGet instance = MFGetHospitalSql.getInstance();
		String sql = instance.getData(param);
		return sql;
	}
	
}
