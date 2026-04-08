import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class MFGetCertificatePaper implements MFGet {
	private static MFGetCertificatePaper mInstance=null;
	private MFGetCertificatePaper(){
		
	}
	
	public static MFGetCertificatePaper getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCertificatePaper();
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
		String bdiv = (String)param.get("bdiv");
		String nofill = (String)param.get("no_fill"); // 필드를 그래도 보여주기 위한 용도임. memr editor(c#)에서 사용하기 위함.
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "pid=" + pid + ", bededt=" + bededt);
		
		if (pid == null) pid = "";
		if (bededt == null) bededt = "";
		if (nofill == null) nofill = "";

		//SqlHelper sqlHelper;
		String returnString;
		try {
			//sqlHelper = new SqlHelper(hospitalId);
			// 증명서 파일을 가져온다.
			String paperString = getPaperString(hospitalId, ccfId);
			// 환자 정보를 추가한다.
			if (paperString != null) {
				if(!"Y".equalsIgnoreCase(nofill)){
					paperString = getPaperStringFillValue(paperString, hospitalId, ccfId, pid, bededt, bdiv);
				}
			}
			returnString = paperString;
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString = null;
		}
		return returnString;
	}
	
	private String getPaperString(String hospitalId, String ccfId) throws Exception {
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		if(interfaceTableYn){
			sql = "select ccf_name,ccf_filename from consent_form_mast where ccf_id=?";
		}else{
			sql = "select cdnm as ccf_name, fld1qty as ccf_filename from ta88 where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
		}
		para.put(1, ccfId);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		rs = new ResultSetHelper(rsString);
		String ccfName = rs.getString(0, "ccf_name");
		String ccfFilename = rs.getString(0, "ccf_filename");
		String fileName = ccfFilename;
		if("".equalsIgnoreCase(fileName)) fileName = ccfName;
		new LogWrite().debugWrite(getClass().getSimpleName(), "getPaperString", "file=" + "Form" + File.separator + fileName + ".htm");
		if(sqlHelper.isFile("ccf", "Form" + File.separator + fileName + ".htm")){
			returnString = sqlHelper.getFile("Form" + File.separator + fileName + ".htm");
		}else{
			returnString = sqlHelper.getFile("Form" + File.separator + fileName + ".html");
		}
		return returnString;
	}
	
	private String getPaperStringFillValue(String ccfString, String hospitalId, String ccfId, String pid, String bededt, String bdiv) throws Exception {
		String paperString = ccfString;
		//
		paperString = paperString.replace("<body>", "<body><div align='center'>");
		paperString = paperString.replace("</body>", "</div></body>");
		paperString = paperString.replace("<BODY>", "<BODY><div align='center'>");
		paperString = paperString.replace("</BODY>", "</div></BODY>");
		//
		String rsString = "";
		ResultSetHelper rs = null;
		// 환자 인적사항만으로 가증한 자료
		rsString = getTA01Value(hospitalId, pid);
		rs = new ResultSetHelper(rsString, false);
		if (rs.getReturnCode() <= 0) {
			paperString = paperString.replace("@PID@", "");
			paperString = paperString.replace("@PNM@", "");
			paperString = paperString.replace("@SEX@", "");
			paperString = paperString.replace("@AGE@", "");
			paperString = paperString.replace("@RESID@", "");
			paperString = paperString.replace("@ADDR@", "");
			paperString = paperString.replace("@HTELNO@", "");
			paperString = paperString.replace("@OTELNO@", "");
			paperString = paperString.replace("@YY@", "");
			paperString = paperString.replace("@MM@", "");
			paperString = paperString.replace("@DD@", "");
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
		}
		
		// 입원환자의 기본적인 자료
		if(bdiv.equals("2")){
			// 입원환자인 경우만
			rsString = getTA04Value(hospitalId, pid, bededt);
			rs = new ResultSetHelper(rsString, false);
			if (rs.getReturnCode() <= 0) {
				paperString = paperString.replace("@BDEDT@", "");
				paperString = paperString.replace("@BEDODT@", "");
				paperString = paperString.replace("@INSNM@", "");
				paperString = paperString.replace("@FAMRELCD@", "");
				paperString = paperString.replace("@P_RESID@", "");
				paperString = paperString.replace("@DPTNM@", "");
				paperString = paperString.replace("@DRNM@", "");
				paperString = paperString.replace("@WARD@", "");
				paperString = paperString.replace("@MADDR@", "");
				paperString = paperString.replace("@DXD@", "");
				paperString = paperString.replace("@IBDYY@", "");
				paperString = paperString.replace("@IBDMM@", "");
				paperString = paperString.replace("@IBDDD@", "");
			} else {
				paperString = paperString.replace("@BDEDT@", rs.getString(0, "bdedt"));
				paperString = paperString.replace("@BEDODT@", rs.getString(0, "bedodt"));
				paperString = paperString.replace("@INSNM@", rs.getString(0, "insnm"));
				paperString = paperString.replace("@FAMRELCD@", rs.getString(0, "famrelcd"));
				paperString = paperString.replace("@P_RESID@", rs.getString(0, "p_resid"));
				paperString = paperString.replace("@DPTNM@", rs.getString(0, "dptnm"));
				paperString = paperString.replace("@DRNM@", rs.getString(0, "drnm"));
				paperString = paperString.replace("@WARD@", rs.getString(0, "ward"));
				paperString = paperString.replace("@MADDR@", rs.getString(0, "maddr"));
				paperString = paperString.replace("@DXD@", rs.getString(0, "dxd"));
				paperString = paperString.replace("@IBDYY@", rs.getString(0, "ibdyy"));
				paperString = paperString.replace("@IBDMM@", rs.getString(0, "ibdmm"));
				paperString = paperString.replace("@IBDDD@", rs.getString(0, "ibddd"));
			}
		}else{
			// 외래환자
			paperString = paperString.replace("@BDEDT@", "");
			paperString = paperString.replace("@BEDODT@", "");
			paperString = paperString.replace("@INSNM@", "");
			paperString = paperString.replace("@FAMRELCD@", "");
			paperString = paperString.replace("@P_RESID@", "");
			paperString = paperString.replace("@DPTNM@", "");
			paperString = paperString.replace("@DRNM@", "");
			paperString = paperString.replace("@WARD@", "");
			paperString = paperString.replace("@MADDR@", "");
			paperString = paperString.replace("@DXD@", "");
			paperString = paperString.replace("@IBDYY@", "");
			paperString = paperString.replace("@IBDMM@", "");
			paperString = paperString.replace("@IBDDD@", "");
		}

		// 병원명을 출력하는 경우
		if(paperString.indexOf("@HOSNM@")!=-1){
			new LogWrite().debugWrite(getClass().getSimpleName(), "getPaperStringFillValue", "hosnm found");
			SqlHelper sqlHelper = new SqlHelper(hospitalId);
			paperString = paperString.replace("@HOSNM@", sqlHelper.getHospitalName());
		}
		// 추가자료가 필요한 경우
		if(paperString.indexOf("@RSVOP@")!=-1){
			// 수술명
			rsString = getTU01Value(hospitalId, pid, bededt);
			rs = new ResultSetHelper(rsString, false);
			if (rs.getReturnCode() <= 0) {
				paperString = paperString.replace("@RSVOP@", "");
			} else {
				paperString = paperString.replace("@RSVOP@", rs.getString(0, "rsvop"));
			}
		}
		//		
		return paperString;
	
	}
	
	private String getTA01Value(String hospitalId, String pid) throws Exception{
		// 환자마스터
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		if(interfaceTableYn){
			sql =
				"select a01.pat_id as pid" + 
			    "     , a01.pat_name as pnm" +
				"     , case isnull(a01.pat_sex,'') when 'M' then '남' when 'F' then '여' end as psex " +
			    "     , '' as age" +
			    "     , substring(a01.jumin_no,1,6)+'-'+substring(a01.jumin_no,7,1)+'******' as resid" +
			    "     , isnull(a01.address1,'') + ' ' + isnull(a01.address2,'') as addr" +
			    "     , isnull(a01.phone_no,'') as htelno" +
			    "     , a01.office_phone_no otelno" + 
			    "     , substring(convert(varchar,getdate(),112),1,4) as yy" +
			    "     , substring(convert(varchar,getdate(),112),5,2) as mm" +
			    "     , substring(convert(varchar,getdate(),112),7,2) as dd" +
			    "  from pat_mast a01" +
			    " where a01.pat_id=?";			
		}else{
			sql =
				"select a01.pid" + 
		        "     , a01.pnm" +
				"     , case isnull(a01.psex,'') when 'M' then '남' when 'F' then '여' end as psex " +
		        "     , dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112)) as age" +
		        "     , substring(a01.resid,1,6)+'-'+substring(a01.resid,7,1)+'******' as resid" +
		        "     , isnull(a01.addr1,'') + ' ' + isnull(a01.addr2,'') as addr" +
		        "     , isnull(a01.htelno,'') as htelno" +
		        "     , a01.otelno" + 
		        "     , substring(convert(varchar,getdate(),112),1,4) as yy" +
		        "     , substring(convert(varchar,getdate(),112),5,2) as mm" +
		        "     , substring(convert(varchar,getdate(),112),7,2) as dd" +
		        "  from ta01 a01" +
		        " where a01.pid=?";
		}
		para.put(1, pid);
		String rsString = sqlHelper.executeQuery(sql,para,null);
		returnString = rsString;
		return returnString;
	}
	
	private String getTA04Value(String hospitalId, String pid, String bededt) throws Exception {
		// 스마트.입원약정서
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		if(interfaceTableYn){
			sql =
			    "select substring(a04.bed_in_date,1,4)+'.'+substring(a04.bed_in_date,5,2)+'.'+substring(a04.bed_in_date,7,2) as bdedt" +
			    "     , substring(a04.bed_in_date,1,4) as ibdyy" +
			    "     , substring(a04.bed_in_date,5,2) as ibdmm" +
			    "     , substring(a04.bed_in_date,7,2) as ibddd" +
			    "     , case when isnull(a04.bed_out_date,'')='' then '' else substring(a04.bed_out_date,1,4)+'.'+substring(a04.bed_out_date,5,2)+'.'+substring(a04.bed_out_date,7,2) end as bedodt" +
			    "     , (select a09.dept_name from dept_mast a09 where a09.dept_code=a04.dept_code) as dptnm" +
			    "     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id) as drnm" +
			    "     , a04.ward_id+'-'+a04.room_id as ward" + // bed는 작성하지 않는다. 스마트병원이 이렇게 되어있음.
			    "     , (select top 1 t05.dise_name from in_pat_dise_hist t05 where t05.pat_id=a04.pat_id and t05.bed_in_date=a04.bed_in_date and t05.dise_no='1') as dxd" +
			    "     , isnull(a56.insu_address,'') as maddr" +
			    "     , isnull(a56.insu_name,'') as insnm" +
			    "     , isnull(a56.family_code,'') as famrelcd" +
			    "     , case when isnull(a56.insu_jumin_no,'')='' then '' else substring(a56.insu_jumin_no,1,6)+'-'+substring(a56.insu_jumin_no,7,1)+'******' end as p_resid" +
			    "  from in_pat_visit_hist a04 with (nolock) inner join pat_mast a01 with (nolock) on a01.pat_id=a04.pat_id" +
			    "                                           left join pat_insu_hist a56 with (nolock) on a56.pat_id=a04.pat_id and a56.insu_class=a04.insu_class and a56.start_date=(select max(z.start_date) from pat_insu_hist z where z.pat_id=a56.pat_id and z.insu_class=a56.insu_class)" +
			    " where a04.pat_id=? and a04.bed_in_date=?";
		}else{
			sql =
			    "select substring(a04.bededt,1,4)+'.'+substring(a04.bededt,5,2)+'.'+substring(a04.bededt,7,2) as bdedt" +
			    "     , substring(a04.bededt,1,4) as ibdyy" +
			    "     , substring(a04.bededt,5,2) as ibdmm" +
			    "     , substring(a04.bededt,7,2) as ibddd" +
			    "     , case when isnull(a04.bedodt,'')='' then '' else substring(a04.bedodt,1,4)+'.'+substring(a04.bedodt,5,2)+'.'+substring(a04.bedodt,7,2) end as bedodt" +
			    "     , (select a09.dptnm from ta09 a09 where a09.dptcd=a04.dptcd) as dptnm" +
			    "     , (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid) as drnm" +
			    "     , a04.wardid+'-'+a04.rmid as ward" + // bed는 작성하지 않는다. 스마트병원이 이렇게 되어있음.
			    "     , (select top 1 t05.dxd from tt05 t05 where t05.pid=a04.pid and t05.bdedt=a04.bededt and t05.seq='1') as dxd" +
			    "     , isnull(a56.maddr,'') as maddr" +
			    "     , isnull(a56.insnm,'') as insnm" +
			    "     , isnull(a56.famrelcd,'') as famrelcd" +
			    "     , case when isnull(a56.resid,'')='' then '' else substring(a56.resid,1,6)+'-'+substring(a56.resid,7,1)+'******' end as p_resid" +
			    "  from ta04 a04 with (nolock) inner join ta01 a01 with (nolock) on a01.pid=a04.pid" +
			    "                              left join ta56 a56 with (nolock) on a56.pid=a04.pid and a56.qlfycd=a04.qlfycd and a56.credt=(select max(z.credt) from ta56 z where z.pid=a56.pid and z.qlfycd=a56.qlfycd)" +
			    " where a04.pid=? and a04.bededt=?";
		}
		para.put(1, pid);    paraType.put(1, "C");
		para.put(2, bededt); paraType.put(2, "D");
		String rsString = sqlHelper.executeQuery(sql, para, paraType);
		returnString = rsString;
		return returnString;
	}
	
	private String getTU01Value(String hospitalId, String pid, String bededt) throws Exception {
		// 스마트.수술동의서
		// 수술명을 가져온다.
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String emrDateFormat = sqlHelper.getEmrDateFormat();
		if("yyyy-mm-dd".equalsIgnoreCase(emrDateFormat)){
			bededt = Utility.getFormattedDate(bededt, emrDateFormat);
		}
		
		String sql = "";
		if(interfaceTableYn){
			sql =
			    "select top 1 u01.op_date,u01.op_name,'' dacd" +
			    "  from pat_op_hist u01" +
			    " where u01.pat_id=?" +
			    "   and isnull(u01.cancel_flag,'')='0'" +
			    " order by op_date desc";
		}else{
			sql =
			    "select top 1 u01.opdt,u01.rsvop,u01.dacd" +
			    "  from tu01 u01" +
			    " where u01.pid=?" +
			    "   and isnull(u01.chgdt,'')=''" +
			    " order by opdt desc";
		}
		para.put(1, pid);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		returnString = rsString;
		return returnString;
	}
	
}
