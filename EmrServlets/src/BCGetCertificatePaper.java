import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class BCGetCertificatePaper implements BCGet {
	private static BCGetCertificatePaper mInstance=null;
	private BCGetCertificatePaper(){
		
	}
	
	public static BCGetCertificatePaper getInstance(){
		if(mInstance==null){
			mInstance = new BCGetCertificatePaper();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String ccfId = (String)param.get("ccfid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		
		if (pid == null) pid = "";
		if (bededt == null) bededt = "";

		SqlHelper sqlHelper;
		String returnString;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			// 증명서 파일을 가져온다.
			String paperString = getPaperString(hospitalId, ccfId);
			// 환자 정보를 추가한다.
			if (paperString != null) {
				paperString = getPaperStringFillValue(paperString, hospitalId, ccfId, pid, bededt);
			}
			returnString = paperString;
		} catch (Exception ex) {
			returnString = null;
		}
		return returnString;
	}
	
	private String getPaperString(String hospitalId, String ccfId) throws Exception {
		return "Not Used";
		/*
		// basecamp에서 읽는 것으로 수정한다.
		String filename="";
		//String connectionUrl = Utility.getBasecampDbConnectionUrl();//"jdbc:sqlserver://localhost:1433;databaseName=Basecamp;user=sa;password=mms;";
		String returnString=null;

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try{
			con = SqlHelper.getBasecampDataSource().getConnection();//DriverManager.getConnection(connectionUrl);
			
			String sql = 
					"select ccf_filename " +
			        "  from consentforms " +
					" where hospital_id=?" +
			        "   and ccf_id=?";
			returnString = sql;
			stmt = con.prepareStatement(sql);
			stmt.setString(1, hospitalId);
			stmt.setString(2, ccfId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				filename=rs.getString("ccf_filename");
			}
			if(filename.equals("")==false){
				returnString = new SqlHelper(hospitalId).getFileBasecamp("D:/EmrDroid/ConsentForms/Form/" + filename);
			}
			return returnString;
		} catch(SQLException ex) {
			return ExceptionHelper.toJSONString(ex);
		} catch(Exception ex) {
			return ExceptionHelper.toJSONString(ex);
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(stmt!=null){
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
		*/
		
		/*
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		sqlHelper = new SqlHelper(hospitalId);
		String sql = "select * from ta88 where mst1cd='EMR' and mst2cd='FORM' and mst3cd='" + mst3cd + "'";
		String rsString = sqlHelper.executeQuery(sql);
		rs = new ResultSetHelper(rsString);
		String fileName = rs.getString(0, "cdnm");
		returnString = sqlHelper.getFile("Form/" + fileName + ".htm");
		return returnString;
		*/
	}
	
	private String getPaperStringFillValue(String ccfString, String hospitalId, String ccfId, String pid, String bededt) throws Exception {
		String paperString = ccfString;
		if("007".equalsIgnoreCase(ccfId)){
			// 환자 인적사항만으로 가증한 자료
			String rsString = get007Value(hospitalId, pid);
			ResultSetHelper rs = new ResultSetHelper(rsString, false);
			if (rs.getReturnCode() <= 0) {
				paperString = paperString.replace("@PID@", "");
				paperString = paperString.replace("@PNM@", "");
				paperString = paperString.replace("@SEX@", "");
				paperString = paperString.replace("@AGE@", "");
				paperString = paperString.replace("@RESID@", "");
				paperString = paperString.replace("@ADDR@", "");
				paperString = paperString.replace("@HTELNO@", "");
				paperString = paperString.replace("@OTELNO@", "");
				paperString = paperString.replace("@MADDR@", "");
				paperString = paperString.replace("@YY@", "");
				paperString = paperString.replace("@MM@", "");
				paperString = paperString.replace("@DD@", "");
				paperString = paperString.replace("@HOSNM@", "");
			} else {
				paperString = paperString.replace("@PID@", rs.getString(0, "pid"));
				paperString = paperString.replace("@PNM@", rs.getString(0, "pnm"));
				paperString = paperString.replace("@SEX@", rs.getString(0, "psex"));
				paperString = paperString.replace("@AGE@", rs.getString(0, "age"));
				paperString = paperString.replace("@RESID@", rs.getString(0, "resid"));
				paperString = paperString.replace("@ADDR@", rs.getString(0, "addr"));
				paperString = paperString.replace("@HTELNO@", rs.getString(0, "htelno"));
				paperString = paperString.replace("@OTELNO@", rs.getString(0, "otelno"));
				paperString = paperString.replace("@YY@", rs.getString(0, "yy"));
				paperString = paperString.replace("@MM@", rs.getString(0, "mm"));
				paperString = paperString.replace("@DD@", rs.getString(0, "dd"));
				paperString = paperString.replace("@HOSNM@", rs.getString(0, "hosnm"));
			}
		}
		else{
			// 입원환자의 기본적인 자료
			String rsString = get001Value(hospitalId, pid, bededt);
			ResultSetHelper rs = new ResultSetHelper(rsString, false);
			if (rs.getReturnCode() <= 0) {
				paperString = paperString.replace("@PID@", "");
				paperString = paperString.replace("@BDEDT@", "");
				paperString = paperString.replace("@BEDODT@", "");
				paperString = paperString.replace("@PNM@", "");
				paperString = paperString.replace("@SEX@", "");
				paperString = paperString.replace("@AGE@", "");
				paperString = paperString.replace("@RESID@", "");
				paperString = paperString.replace("@ADDR@", "");
				paperString = paperString.replace("@HTELNO@", "");
				paperString = paperString.replace("@OTELNO@", "");
				paperString = paperString.replace("@MADDR@", "");
				paperString = paperString.replace("@INSNM@", "");
				paperString = paperString.replace("@DPTNM@", "");
				paperString = paperString.replace("@DRNM@", "");
				paperString = paperString.replace("@WARD@", "");
				paperString = paperString.replace("@DXNM@", "");
				paperString = paperString.replace("@YY@", "");
				paperString = paperString.replace("@MM@", "");
				paperString = paperString.replace("@DD@", "");
				paperString = paperString.replace("@HOSNM@", "");
			} else {
				paperString = paperString.replace("@PID@", rs.getString(0, "pid"));
				paperString = paperString.replace("@BDEDT@", rs.getString(0, "bdedt"));
				paperString = paperString.replace("@BEDODT@", rs.getString(0, "bedodt"));
				paperString = paperString.replace("@PNM@", rs.getString(0, "pnm"));
				paperString = paperString.replace("@SEX@", rs.getString(0, "psex"));
				paperString = paperString.replace("@AGE@", rs.getString(0, "age"));
				paperString = paperString.replace("@RESID@", rs.getString(0, "resid"));
				paperString = paperString.replace("@ADDR@", rs.getString(0, "addr"));
				paperString = paperString.replace("@HTELNO@", rs.getString(0, "htelno"));
				paperString = paperString.replace("@OTELNO@", rs.getString(0, "otelno"));
				paperString = paperString.replace("@MADDR@", rs.getString(0, "maddr"));
				paperString = paperString.replace("@INSNM@", rs.getString(0, "insnm"));
				paperString = paperString.replace("@FAMRELCD@", rs.getString(0, "famrelcd"));
				paperString = paperString.replace("@P_RESID@", rs.getString(0, "p_resid"));
				paperString = paperString.replace("@DPTNM@", rs.getString(0, "dptnm"));
				paperString = paperString.replace("@DRNM@", rs.getString(0, "drnm"));
				paperString = paperString.replace("@WARD@", rs.getString(0, "ward"));
				paperString = paperString.replace("@DXNM@", rs.getString(0, "dxd"));
				paperString = paperString.replace("@YY@", rs.getString(0, "yy"));
				paperString = paperString.replace("@MM@", rs.getString(0, "mm"));
				paperString = paperString.replace("@DD@", rs.getString(0, "dd"));
				paperString = paperString.replace("@HOSNM@", rs.getString(0, "hosnm"));
			}
		}
		// 추가자료가 필요한 경우
		if(ccfId.equals("002")||ccfId.equals("003")){
			// 수술명
			String rsString = get002Value(hospitalId, pid, bededt);
			ResultSetHelper rs = new ResultSetHelper(rsString, false);
			if (rs.getReturnCode() <= 0) {
				paperString = paperString.replace("@OPNM@", "");
			} else {
				paperString = paperString.replace("@OPNM@", rs.getString(0, "rsvop"));
			}
		}
		return paperString;
	
	}
	
	private String get001Value(String hospitalId, String pid, String bededt) throws Exception {
		// 스마트.입원약정서
		SqlHelper sqlHelper;
		String returnString;
		sqlHelper = new SqlHelper(hospitalId);
		StringBuilder sb = new StringBuilder();
		sb.append("select a04.pid");
		sb.append("     , substring(a04.bededt,1,4)+'.'+substring(a04.bededt,5,2)+'.'+substring(a04.bededt,7,2) as bdedt");
		sb.append("     , case when isnull(a04.bedodt,'')='' then '' else substring(a04.bedodt,1,4)+'.'+substring(a04.bedodt,5,2)+'.'+substring(a04.bedodt,7,2) end as bedodt");
		sb.append("     , a01.pnm");
		sb.append("     , case isnull(a01.psex,'') when 'M' then '남' when 'F' then '여' end as psex ");
		sb.append("     , dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112)) as age");
		//sb.append("     , substring(a01.resid,1,6)+'-'+substring(a01.resid,7,7) as resid");
		sb.append("     , substring(a01.resid,1,6)+'-'+substring(a01.resid,7,1)+'******' as resid");
		sb.append("     , isnull(a01.addr1,'') + ' ' + isnull(a01.addr2,'') as addr");
		sb.append("     , isnull(a01.htelno,'') as htelno");
		sb.append("     , a01.otelno");
		sb.append("     , (select a09.dptnm from ta09 a09 where a09.dptcd=a04.dptcd) as dptnm");
		sb.append("     , (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid) as drnm");
		//sb.append("     , a04.wardid+'-'+a04.rmid+'-'+a04.bedid as ward");
		sb.append("     , a04.wardid+'-'+a04.rmid as ward"); // bed는 작성하지 않는다. 스마트병원이 이렇게 되어있음.
		sb.append("     , (select top 1 t05.dxd from tt05 t05 where t05.pid=a04.pid and t05.bdedt=a04.bededt and t05.seq='1') as dxd");
		sb.append("     , isnull(a56.maddr,'') as maddr");
		sb.append("     , isnull(a56.insnm,'') as insnm");
		sb.append("     , isnull(a56.famrelcd,'') as famrelcd");
		//sb.append("     , substring(a56.resid,1,6)+'-'+substring(a56.resid,7,7) as p_resid");
		sb.append("     , case when isnull(a56.resid,'')='' then '' else substring(a56.resid,1,6)+'-'+substring(a56.resid,7,1)+'******' end as p_resid");
		sb.append("     , substring(convert(varchar,getdate(),112),1,4) as yy");
		sb.append("     , substring(convert(varchar,getdate(),112),5,2) as mm");
		sb.append("     , substring(convert(varchar,getdate(),112),7,2) as dd");
		sb.append("     , (select fld1qty from ta88 where mst1cd='a' and mst2cd='hospital' and mst3cd='1') as hosnm");
		sb.append("  from ta04 a04 with (nolock) inner join ta01 a01 with (nolock) on a01.pid=a04.pid");
		sb.append("                              left join ta56 a56 with (nolock) on a56.pid=a04.pid and a56.qlfycd=a04.qlfycd and a56.credt=(select max(z.credt) from ta56 z where z.pid=a56.pid and z.qlfycd=a56.qlfycd)");
		sb.append(" where a04.pid='" + pid + "'" + " and a04.bededt='" + bededt + "'");
		String sql = sb.toString();
		String rsString = sqlHelper.executeQuery(sql);
		returnString = rsString;
		return returnString;
	}
	
	private String get002Value(String hospitalId, String pid, String bededt) throws Exception {
		// 스마트.수술동의서
		// 수술명을 가져온다.
		SqlHelper sqlHelper;
		String returnString;
		sqlHelper = new SqlHelper(hospitalId);
		StringBuilder sb = new StringBuilder();
		sb.append("select top 1 u01.opdt,u01.rsvop,u01.dacd");
		sb.append("  from tu01 u01");
		sb.append(" where u01.pid='" + pid + "'");
		sb.append("   and isnull(u01.chgdt,'')=''");
		sb.append(" order by opdt desc");
		String sql = sb.toString();
		String rsString = sqlHelper.executeQuery(sql);
		returnString = rsString;
		return returnString;
	}
	
	private String get007Value(String hospitalId, String pid) throws Exception {
		// 환자명을 가져온다.
		SqlHelper sqlHelper;
		String returnString;
		sqlHelper = new SqlHelper(hospitalId);
		StringBuilder sb = new StringBuilder();
		sb.append("select a01.pid, a01.pnm");
		sb.append("     , case isnull(a01.psex,'') when 'M' then '남' when 'F' then '여' end as psex ");
		sb.append("     , dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112)) as age");
		sb.append("     , substring(a01.resid,1,6)+'-'+substring(a01.resid,7,1)+'******' as resid");
		sb.append("     , isnull(a01.addr1,'') + ' ' + isnull(a01.addr2,'') as addr");
		sb.append("     , isnull(a01.htelno,'') as htelno");
		sb.append("     , isnull(a01.otelno,'') as otelno");
		sb.append("     , substring(convert(varchar,getdate(),112),1,4) as yy");
		sb.append("     , substring(convert(varchar,getdate(),112),5,2) as mm");
		sb.append("     , substring(convert(varchar,getdate(),112),7,2) as dd");
		sb.append("     , (select fld1qty from ta88 where mst1cd='a' and mst2cd='hospital' and mst3cd='1') as hosnm");
		sb.append("  from ta01 a01");
		sb.append(" where a01.pid='" + pid + "'");
		String sql = sb.toString();
		String rsString = sqlHelper.executeQuery(sql);
		returnString = rsString;
		return returnString;
	}

}
