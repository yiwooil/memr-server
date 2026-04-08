import java.sql.SQLException;
import java.util.HashMap;


public class MFGetErPatientOrder implements MFGet {
	private static MFGetErPatientOrder mInstance=null;
	private MFGetErPatientOrder(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetErPatientOrder getInstance(){
		if(mInstance==null){
			mInstance = new MFGetErPatientOrder();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String odivcd = (String)param.get("odivcd");
		String radOrderYn = (String)param.get("radorderyn");

		if (odivcd==null) odivcd="";
		if (radOrderYn==null) radOrderYn="";
		
		if(radOrderYn.equalsIgnoreCase("Y")){
			odivcd = "R";
		}else if(radOrderYn.equalsIgnoreCase("Z")){
			odivcd = "Q";
		}
		
		String pid2=""; // 자인컴은 id2가 있음.
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
		String returnString = "";
		SqlHelper sqlHelper;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			String sql="";
			sql = getSql(odivcd);
			
			para.put(++idx, pid); paraType.put(idx, "C");
			para.put(++idx, bededt); paraType.put(idx, "D");
			if(odivcd.equals("")==false){
				para.put(++idx, odivcd+"%"); paraType.put(idx, "C");
			}
			
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			//boolean isJaincom = sqlHelper.isJaincom();
			//String sql="";
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
			//		sql = getSqlInterOracleJaincom(odivcd,radOrderYn);
			//	}else if(isOracle){
			//		sql = getSqlInterOracle(odivcd,radOrderYn);
			//	}else{
			//		sql = getSqlInter(odivcd, radOrderYn);
			//	}
			//}else{
			//	sql = getSql(odivcd, radOrderYn);
			//}
			//para.put(++idx, pid); paraType.put(idx, "C");
			//if(isJaincom){
			//	para.put(++idx, pid2); paraType.put(idx, "C");
			//}
			//para.put(++idx, bededt); paraType.put(idx, "D");
			//if(odivcd.equals("")==false){
			//	para.put(++idx, odivcd+"%"); paraType.put(idx, "C");
			//}
			
			System.out.println(Utility.getCurrentDateTime() + " MFGetErPatientOrder - QUERY 시작(" + hospitalId + "," + sqlHelper.getHospitalName() + ")");
			String rsString = sqlHelper.executeQuery(sql,para,paraType);
			System.out.println(Utility.getCurrentDateTime() + " MFGetErPatientOrder - QUERY 시작(" + hospitalId + "," + sqlHelper.getHospitalName() + ")");
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
	
	private String getSql(String odivcd) {
		String key=odivcd;
		if(sqlMap.containsKey(key)==false){
			String sql = "";
			sql += "select v01.odt,v01.ono,isnull(v01.rmk,'') as rmk,v01.odivcd,v01.exdt,v01.dcfg,v01.bdiv,v01.exdrid" + "\n";
			sql += "     , a18.onm" + "\n";
			sql += "     , v01a.seq,v01a.oqty,isnull(v01a.ounit,'') as ounit,v01a.ordcnt,v01a.odaycnt,v01a.alwfg,v01a.fldcd1" + "\n";
			sql += "     , v61.ostscd" + "\n";
			sql += "     , isnull(a07.drnm,'') as exdrnm" + "\n";
			sql += "     , isnull(a13.empnm,'') as exdrempnm" + "\n";
			sql += "     , case v01.bdiv when '1' then '1' when '3' then '2' else '3' end as bdiv_order" + "\n";
			sql += "     , case when v01.usecd='5' then '1' else '' end as prnfg" + "\n";
			sql += "     , case when v01a.alwfg='0' then '급여' " + "\n";
			sql += "            when v01a.alwfg='1' then '비급' " + "\n";
			sql += "            when v01a.alwfg='2' then '비보' " + "\n";
			sql += "            when v01a.alwfg='4' then '백' " + "\n";
			sql += "            when v01a.alwfg='' then '급여' " + "\n";
			sql += "            when v01a.alwfg is null then '급여' " + "\n";
			sql += "            else v01a.alwfg " + "\n";
			sql += "       end as alwfgnm " + "\n";
			sql += "     , case when v01.odivcd='A' then '입원지시' " + "\n";
			sql += "            when v01.odivcd='B' then '혈액' " + "\n";
			sql += "            when v01.odivcd='C' then '협의진료' " + "\n";
			sql += "            when v01.odivcd='G' then '치료재료' " + "\n";
			sql += "            when left(v01.odivcd,1)='L' then '진단검사' " + "\n";
			sql += "            when v01.odivcd='MF' then '외용약' " + "\n";
			sql += "            when v01.odivcd='MI' then '주사약' " + "\n";
			sql += "            when v01.odivcd='MO' then '먹는약' " + "\n";
			sql += "            when v01.odivcd='O' then '재활치료' " + "\n";
			sql += "            when v01.odivcd='Q' then '기능검사' " + "\n";
			sql += "            when v01.odivcd='R' then '영상진단' " + "\n";
			sql += "            when v01.odivcd='S' then '메시지' " + "\n";
			sql += "            when v01.odivcd='T' then '처치' " + "\n";
			sql += "            when v01.odivcd='X' then '진단서/동의서' " + "\n";
			sql += "            else '' " + "\n";
			sql += "       end as odivcdnm " + "\n";
			sql += "  from tv01 v01 inner join tv01a v01a on v01a.hdid=v01.hdid " + "\n";
			sql += "                inner join tv61  v61  on v61.hdid=v01.hdid " + "\n";
			sql += "                inner join ta18 a18   on a18.ocd=v01a.ocd and a18.credt=(select max(x.credt) from ta18 x where x.ocd=a18.ocd and x.credt<=v01.odt) " + "\n";
			sql += "                left  join ta07 a07 on a07.drid=v01.exdrid" + "\n";
			sql += "                left  join ta13 a13 on a13.empid=v01.exdrid" + "\n";
			sql += " where v01.pid=?" + "\n";
			sql += "   and v01.bededt=?" + "\n";
			sql += "   and v01.bdiv='3'" + "\n";
			sql += "   and v01.odivcd not like 'H%' " + "\n"; // H로 시작하면 d/c 시키는 처방임.
			if(odivcd.equals("")==false) {
			sql += "   and v01.odivcd like ?" + "\n";
			}
			sql += " order by v01.odt desc,case v01.bdiv when '1' then '1' when '3' then '2' else '3' end,v01.ono,v01a.seq " + "\n"; // 외래,응급실,병동 순서 
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	//private String getSqlInter(String odivcd, String radOrderYn) {
	//	String key="Inter,"+odivcd+","+radOrderYn;
	//	if(sqlMap.containsKey(key)==false){
	//		String sql = 
	//				"select v01.order_date odt,v01.order_no ono,isnull(v01.remark,'') as rmk,v01.order_class odivcd,v01.exam_date exdt,v01.dc_flag dcfg,v01.in_out_flag bdiv,v01.doctor_id exdrid" +
	//				"     , a18.code_name onm" +
	//				"     , v01.order_seq seq,v01.order_quantity oqty,isnull(v01.order_unit,'') as ounit,v01.order_count ordcnt,v01.order_day odaycnt,v01.insu_pay_class alwfg,v01.diet_class fldcd1" +
	//				"     , v01.order_status ostscd" +
	//				"     , isnull(a07.doctor_name,'') as exdrnm" +
	//				"     , isnull(a13.emp_name,'') as exdrempnm" +
	//				"     , case v01.in_out_flag when '1' then '1' when '3' then '2' else '3' end as bdiv_order" +
	//				"     , v01.prn_flag as prnfg" +
	//				"     , isnull((select x.insu_pay_class_name from insu_pay_class_mast x where x.insu_pay_class=v01.insu_pay_class),'') as alwfgnm " +
	//				"     , isnull((select x.order_class_name from order_class_mast x where x.order_class=v01.order_class),'') as odivcdnm " +
	//				"  from er_pat_order_hist v01 inner join order_code_mast a18   on a18.order_code=v01.order_code and a18.start_date=(select max(x.start_date) from order_code_mast x where x.order_code=a18.order_code and x.start_date<=v01.order_date) " +
	//				"                left  join doctor_mast a07 on a07.doctor_id=v01.doctor_id" +
	//				"                left  join emp_mast a13 on a13.emp_id=v01.doctor_id" +
	//			    " where v01.pat_id=?" +
	//			    "   and v01.visit_date=?" +
	//			    "   and v01.in_out_flag='3'" +
	//			    "   and v01.order_class not like 'H%' "; // H로 시작하면 d/c 시키는 처방임.
	//		if(odivcd.equals("")==false) {
	//			sql +=
	//				"   and v01.order_class like ?";
	//		}
	//		if(radOrderYn.equalsIgnoreCase("Y")){
	//			sql +=
	//				"   and v01.rad_order_yn='Y'";
	//		}
	//		sql +=
	//				" order by v01.order_date desc,case v01.in_out_flag when '1' then '1' when '3' then '2' else '3' end,v01.order_no,v01.order_seq "; // 외래,응급실,병동 순서 
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}
	
	//private String getSqlInterOracle(String odivcd, String radOrderYn) {
	//	String key="InterOracle,"+odivcd+","+radOrderYn;
	//	if(sqlMap.containsKey(key)==false){
	//		String sql =
	//				"select * from ( " +
	//				"select v01.order_date odt,v01.order_no ono,v01.remark as rmk,v01.order_class odivcd,v01.exam_date exdt,v01.dc_flag dcfg,/*v01.in_out_flag*/'3' bdiv,v01.doctor_id exdrid" +
	//				"     , a18.code_name onm" +
	//				"     , v01.order_seq seq,v01.order_quantity oqty,v01.order_unit as ounit,v01.order_count ordcnt,v01.order_day odaycnt,nvl(v01.insu_pay_class,'0') as alwfg,v01.diet_class fldcd1" +
	//				"     , v01.order_status ostscd" +
	//				"     , a07.doctor_name as exdrnm" +
	//				"     , a13.emp_name as exdrempnm" +
	//				"     , /*case v01.in_out_flag when '1' then '1' when '3' then '2' else '3' end*/'2' as bdiv_order" +
	//				"     , v01.prn_flag prnfg" +
	//				"     , (select x.insu_pay_class_name from insu_pay_class_mast x where x.insu_pay_class=v01.insu_pay_class) as alwfgnm " +
	//				"     , (select x.order_class_name from order_class_mast x where x.order_class=v01.order_class) as odivcdnm " +
	//				"  from er_pat_order_hist v01 inner join order_code_mast a18 on a18.order_code=v01.order_code and a18.start_date=(select max(x.start_date) from order_code_mast x where x.order_code=a18.order_code and x.start_date<=v01.order_date) " +
	//				"                left  join doctor_mast a07 on a07.doctor_id=v01.doctor_id" +
	//				"                left  join emp_mast a13 on a13.emp_id=v01.doctor_id" +
	//			    " where v01.pat_id=?" +
	//			    "   and v01.visit_date=?";
	//		if(odivcd.equals("")==false) {
	//			sql +=
	//				"   and v01.order_class like ?";
	//		}
	//		if(radOrderYn.equalsIgnoreCase("Y")){
	//			sql +=
	//				"   and v01.rad_order_yn='Y'";
	//		}
	//		sql +=
	//				" ) v01 order by v01.odt desc,v01.bdiv_order,v01.ono,v01.seq "; // 외래,응급실,병동 순서 
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}
		
	//private String getSqlInterOracleJaincom(String odivcd, String radOrderYn) {
	//	String key="InterOracleJaincom,"+odivcd+","+radOrderYn;
	//	if(sqlMap.containsKey(key)==false){
	//		String sql =
	//				"select * from ( " +
	//				"select v01.order_date odt,v01.order_no ono,v01.remark as rmk,v01.order_class odivcd,v01.exam_date exdt,v01.dc_flag dcfg,/*v01.in_out_flag*/'3' bdiv,v01.doctor_id exdrid" +
	//				"     , (select a18.code_name from order_code_mast a18 where a18.order_code=v01.order_code and a18.start_date in (select max(x.start_date) from order_code_mast x where x.order_code=v01.order_code and x.start_date<=v01.order_date) and rownum<2) onm" +
	//				"     , v01.order_seq seq,v01.order_quantity oqty,v01.order_unit as ounit,v01.order_count ordcnt,v01.order_day odaycnt,nvl(v01.insu_pay_class,'0') as alwfg,v01.diet_class fldcd1" +
	//				"     , v01.order_status ostscd" +
	//				"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=v01.doctor_id and rownum<2) as exdrnm" +
	//				"     , (select a13.emp_name from emp_mast a13 where a13.emp_id=v01.doctor_id and rownum<2) as exdrempnm" +
	//				"     , /*case v01.in_out_flag when '1' then '1' when '3' then '2' else '3' end*/'2' as bdiv_order" +
	//				"     , v01.prn_flag prnfg" +
	//				"     , (select x.insu_pay_class_name from insu_pay_class_mast x where x.insu_pay_class=v01.insu_pay_class) as alwfgnm " +
	//				"     , (select x.order_class_name from order_class_mast x where x.order_class=v01.order_class) as odivcdnm " +
	//				"  from er_pat_order_hist v01 " +
	//			    " where v01.pat_id=?" +
	//				"   and v01.pat_id2=?" +
	//			    "   and v01.visit_date=?";
	//		if(odivcd.equals("")==false) {
	//			sql +=
	//				"   and v01.order_class like ?";
	//		}
	//		if(radOrderYn.equalsIgnoreCase("Y")){
	//			sql +=
	//				"   and v01.rad_order_yn='Y'";
	//		}
	//		sql +=
	//				" ) v01 order by v01.odt desc,v01.bdiv_order,v01.ono,v01.seq "; // 외래,응급실,병동 순서 
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}

}
