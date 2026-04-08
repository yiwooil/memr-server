import java.sql.SQLException;
import java.util.HashMap;


public class MFGetInPatientList implements MFGet {
	private static MFGetInPatientList mInstance=null;
	private MFGetInPatientList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetInPatientList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetInPatientList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("usrid");
		String sortOrder = (String)param.get("sortorder");
		String ward = (String)param.get("ward");
		String dept = (String)param.get("dept");
		String pdrid = (String)param.get("pdrid");
		String retver = (String)param.get("retver"); // 2025.01.10 WOOIL
		
		if(sortOrder==null) sortOrder="1"; // 오류방지용
		if(pdrid==null) pdrid=""; // 오류방지용
		if(dept==null) dept="";
		if(ward==null) ward="";
		if(retver==null) retver=""; // 2025.01.10 WOOIL
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		String returnString;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			//boolean isJaincom = sqlHelper.isJaincom();
			String sql="";
			//if(interfaceTableYn){
			//	boolean isOracle = sqlHelper.isOracle();
			//	if(isJaincom){
			//		sql = getSqlInterOracleJaincom(ward, dept, pdrid, sortOrder);
			//	}else if(isOracle){
			//		sql = getSqlInterOracle(ward, dept, pdrid, sortOrder);
			//	}else{
			//		sql = getSqlInter(ward, dept, pdrid, sortOrder);
			//	}
			//}else{
				sql = getSql(ward, dept, pdrid, sortOrder);
			//}
			
			if(!"".equals(ward)) para.put(++idx, ward);
			if(!"".equals(dept)) para.put(++idx, dept);
			if(!"".equals(pdrid)) para.put(++idx, pdrid);

			String rsString=sqlHelper.executeQuery(sql,para,null,retver);
			returnString=rsString;
		}catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	// sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	// 성능에 영향이 있을까???
	public String getSql(String ward, String dept, String pdrid, String sortOrder) {
		String key=ward+","+dept+","+pdrid+","+sortOrder;
		if(sqlMap.containsKey(key)==false){
			String sql = "";
			sql += "select a01.pnm";
			sql += "     , a01.psex";
			sql += "     , a04.dptcd";
			sql += "     , a04.bededt";
			sql += "     , a04.pid";
			sql += "     , isnull(a04.bedodt,'') as bedodt";
			sql += "     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age";
			sql += "     , a04.wardid+(case when isnull(a04.rmid,'')<>'' then '-' else '' end)+isnull(a04.rmid,'')+(case when isnull(a04.bedid,'')<>'' then '-' else '' end)+isnull(a04.bedid,'') as ward";
			sql += "     , (select a09.dptnm from ta09 a09 where a09.dptcd=a04.dptcd) as dptnm";
			sql += "     , (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid) as pdrnm";
			sql += "     , a04.qlfycd as qfycd";
			sql += "     , (select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=a04.qlfycd) as qfycdnm";
			sql += "     , isnull((select top 1 dxd from tt05 t05 where t05.pid=a04.pid and t05.bdedt=a04.bededt order by convert(numeric,case when isnumeric(t05.ptysq)=1 then t05.ptysq else '99' end),t05.seq),'') as dxd";
			sql += "     , a01.bthdt";
			sql += "     , a04.pdrid as drid";
			sql += "  from ta04 a04 inner join ta01 a01 on a01.pid=a04.pid ";
			sql += " where (a04.bedodiv in ('0','') or a04.bedodiv is null)";
			sql += "   /*and a04.wardid<>'er1'*/ ";
			if (ward.equals("")==false) {
				sql +=
					"   and a04.wardid=?";
			}
			if (dept.equals("")==false) {
				sql +=
					"   and a04.dptcd=?";
			}
			if (pdrid.equals("")==false) {
				sql +=
					"   and a04.pdrid=?";
			}
			if (sortOrder.equals("1")) {
				sql +=
					" order by a01.pnm ";
			}
			else if (sortOrder.equals("2")) {
				sql +=   
					" order by a04.wardid,a04.rmid,a04.bedid,a01.pnm ";
			}
			else if (sortOrder.equals("3")) {
				sql +=   
					" order by a04.dptcd,a01.pnm ";
			}
			else if (sortOrder.equals("4")) { 
				sql +=   
					" order by a04.bededt desc,a04.bedehm desc ";
			}
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	//public String getSqlInter(String ward, String dept, String pdrid, String sortOrder) {
	//	String key="Inter,"+ward+","+dept+","+pdrid+","+sortOrder;
	//	if(sqlMap.containsKey(key)==false){
	//		String sql = 
	//				"select a01.pat_name as pnm" +
	//				"     , a01.pat_sex as psex" +
	//				"     , a04.dept_code as dptcd" +
	//				"     , a04.bed_in_date as bededt" +
	//				"     , a04.pat_id as pid" +
	//				"     , isnull(a04.bed_out_date,'') as bedodt" +
	//				"     , '' as age" +
	//				"     , a04.ward_id+(case when isnull(a04.room_id,'')<>'' then '-' else '' end)+isnull(a04.room_id,'')+(case when isnull(a04.bed_id,'')<>'' then '-' else '' end)+isnull(a04.bed_id,'') as ward" +
	//				"     , (select a09.dept_name from dept_mast a09 where a09.dept_code=a04.dept_code) as dptnm" +
	//				"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id) as pdrnm" +
	//				"     , a04.insu_class as qfycd" +
	//				"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfycdnm" +
	//				"     , isnull((select top 1 dise_name from in_pat_dise_hist t05 where t05.pat_id=a04.pat_id and t05.bed_in_date=a04.bed_in_date order by convert(numeric,t05.prim_seq),t05.dise_no),'') as dxd" +
	//				"     , a01.birth_date as bthdt" +
	//			    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
	//			    " where isnull(a04.bed_in_status,'') in ('0','')" +
	//			    "   /*and a04.wardid<>'er1'*/ ";
	//		if (ward.equals("")==false) {
	//			sql +=
	//				"   and a04.ward_id=?";
	//		}
	//		if (dept.equals("")==false) {
	//			sql +=
	//				"   and a04.dept_code=?";
	//		}
	//		if (pdrid.equals("")==false) {
	//			sql +=
	//				"   and a04.doctor_id=?";
	//		}
	//		if (sortOrder.equals("1")) {
	//			sql +=
	//				" order by a01.pat_name ";
	//		}
	//		else if (sortOrder.equals("2")) {
	//			sql +=   
	//				" order by a04.ward_id,a04.room_id,a04.bed_id,a01.pat_name ";
	//		}
	//		else if (sortOrder.equals("3")) {
	//			sql +=   
	//				" order by a04.dept_code,a01.pat_name ";
	//		}
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}
	
	//public String getSqlInterOracle(String ward, String dept, String pdrid, String sortOrder) {
	//	String key="InterOracle,"+ward+","+dept+","+pdrid+","+sortOrder;
	//	if(sqlMap.containsKey(key)==false){
	//		String sql = 
	//				"select a01.pat_name as pnm" +
	//				"     , a01.pat_sex as psex" +
	//				"     , a04.dept_code as dptcd" +
	//				"     , a04.bed_in_date as bededt" +
	//				"     , a04.pat_id as pid" +
	//				"     , a04.bed_out_date as bedodt" +
	//				"     , '' as age" +
	//				"     , case when a04.ward_id is not null and a04.room_id is not null and a04.bed_id is not null then a04.ward_id||'-'||a04.room_id||'-'||a04.bed_id" +
	//				"            when a04.ward_id is not null and a04.room_id is not null and a04.bed_id is null then a04.ward_id||'-'||a04.room_id" +
	//				"            when a04.ward_id is not null and a04.room_id is null and a04.bed_id is null then a04.ward_id" +
	//				"            else null" +
	//				"       end as ward" +
	//				"     , (select a09.dept_name from dept_mast a09 where a09.dept_code=a04.dept_code) as dptnm" +
	//				"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id) as pdrnm" +
	//				"     , a04.insu_class as qfycd" +
	//				"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfycdnm" +
	//				"     , (select t05.dise_name" +
	//				"          from in_pat_dise_hist t05" +
	//				"         where t05.pat_id=a04.pat_id" +
	//				"           and t05.bed_in_date=a04.bed_in_date" +
	//				"           and (t05.prim_seq,t05.dise_no) in (select min(cast(x.prim_seq as number)),min(x.dise_no)" +
	//				"                                                from in_pat_dise_hist x" +
	//				"                                               where x.pat_id=t05.pat_id" +
	//				"                                                 and x.bed_in_date=t05.bed_in_date" +
	//				"                                             )" +
	//				"       ) as dxd" +
	//				"     , a01.birth_date as bthdt" +
	//			    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
	//			    " where (a04.bed_in_status='0' or a04.bed_in_status is null)" +
	//			    "    ";
	//		if (ward.equals("")==false) {
	//			sql +=
	//				"   and a04.ward_id=?";
	//		}
	//		if (dept.equals("")==false) {
	//			sql +=
	//				"   and a04.dept_code=?";
	//		}
	//		if (pdrid.equals("")==false) {
	//			sql +=
	//				"   and a04.doctor_id=?";
	//		}
	//		if (sortOrder.equals("1")) {
	//			sql +=
	//				" order by a01.pat_name ";
	//		}
	//		else if (sortOrder.equals("2")) {
	//			sql +=   
	//				" order by a04.ward_id,a04.room_id,a04.bed_id,a01.pat_name ";
	//		}
	//		else if (sortOrder.equals("3")) {
	//			sql +=   
	//				" order by a04.dept_code,a01.pat_name ";
	//		}
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}
	
	//public String getSqlInterOracleJaincom(String ward, String dept, String pdrid, String sortOrder) {
	//	String key="InterOracleJaincom,"+ward+","+dept+","+pdrid+","+sortOrder;
	//	if(sqlMap.containsKey(key)==false){
	//		String sql = 
	//				"select a01.pat_name as pnm" +
	//				"     , a01.pat_sex as psex" +
	//				"     , a04.dept_code as dptcd" +
	//				"     , a04.bed_in_date as bededt" +
	//				"     , rtrim(a04.pat_id||a04.pat_id2) as pid" +
	//				"     , a04.bed_out_date as bedodt" +
	//				"     , '' as age" +
	//				"     , case when a04.ward_id is not null and a04.room_id is not null and a04.bed_id is not null then a04.ward_id||'-'||a04.room_id||'-'||a04.bed_id" +
	//				"            when a04.ward_id is not null and a04.room_id is not null and a04.bed_id is null then a04.ward_id||'-'||a04.room_id" +
	//				"            when a04.ward_id is not null and a04.room_id is null and a04.bed_id is null then a04.ward_id" +
	//				"            else null" +
	//				"       end as ward" +
	//				"     , (select a09.dept_name from dept_mast a09 where a09.dept_code=a04.dept_code and rownum<2) as dptnm" +
	//				"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id and rownum<2) as pdrnm" +
	//				"     , a04.insu_class as qfycd" +
	//				"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfycdnm" +
	//				"     , (select t05.dise_name" +
	//				"          from in_pat_dise_hist t05" +
	//				"         where t05.pat_id=a04.pat_id" +
	//				"           and t05.pat_id2=a04.pat_id2" +
	//				"           and t05.bed_in_date=a04.bed_in_date" +
	//				"           and (t05.prim_seq,t05.dise_no) in (select min(cast(x.prim_seq as number)),min(x.dise_no)" +
	//				"                                                from in_pat_dise_hist x" +
	//				"                                               where x.pat_id=t05.pat_id" +
	//				"                                                 and x.pat_id2=t05.pat_id2" +
	//				"                                                 and x.bed_in_date=t05.bed_in_date" +
	//				"                                             )" +
	//				"           and rownum<2" +
	//				"       ) as dxd" +
	//				"     , a01.birth_date as bthdt" +
	//			    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
	//			    " where (a04.bed_in_status='0' or a04.bed_in_status is null)" +
	//			    "    ";
	//		if (ward.equals("")==false) {
	//			sql +=
	//				"   and a04.ward_id=?";
	//		}
	//		if (dept.equals("")==false) {
	//			sql +=
	//				"   and a04.dept_code=?";
	//		}
	//		if (pdrid.equals("")==false) {
	//			sql +=
	//				"   and a04.doctor_id=?";
	//		}
	//		if (sortOrder.equals("1")) {
	//			sql +=
	//				" order by a01.pat_name ";
	//		}
	//		else if (sortOrder.equals("2")) {
	//			sql +=   
	//				" order by a04.ward_id,a04.room_id,a04.bed_id,a01.pat_name ";
	//		}
	//		else if (sortOrder.equals("3")) {
	//			sql +=   
	//				" order by a04.dept_code,a01.pat_name ";
	//		}
	//		sqlMap.put(key, sql);
	//	}
	//	return sqlMap.get(key);
	//}

}
