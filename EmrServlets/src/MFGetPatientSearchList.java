import java.sql.SQLException;
import java.util.HashMap;


public class MFGetPatientSearchList implements MFGet {
	private static MFGetPatientSearchList mInstance=null;
	private MFGetPatientSearchList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetPatientSearchList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetPatientSearchList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("usrid");
		String sortOrder = (String)param.get("sortorder");
		String ward = (String)param.get("ward");
		String dept = (String)param.get("dept");
		String searchText = (String)param.get("searchtext");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			//boolean isJaincom = sqlHelper.isJaincom();
			//String sql="";
			//if(interfaceTableYn){
			//	boolean isOracle = sqlHelper.isOracle();
			//	//if(isJaincom){
			//	//	sql=getSqlInterOracleJaincom(sortOrder);
			//	//}else if(isOracle){
			//	//	sql=getSqlInterOracle(sortOrder);
			//	//}else{
			//		sql=getSqlInter(sortOrder);
			//	//}
			//}else{
			//	sql=getSql(sortOrder);
			//}
			//para.put(++idx, searchText + "%");
			//para.put(++idx, searchText + "%");
			//if(interfaceTableYn){
			//	// 응급실 별도조회
			//	para.put(++idx, searchText + "%");
			//}

			String sql=getSql(sortOrder);
			para.put(++idx, searchText + "%");
			para.put(++idx, searchText + "%");
			String rsString=sqlHelper.executeQuery(sql, para, null);
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
	
	private String getSql(String sortOrder){
		String key="sql";
		if(sqlMap.containsKey(key)==false){
			String sql = "";
			sql += "select a01.pnm" + "\r\n";
			sql += "     , a01.psex" + "\r\n";
			sql += "     , a04.dptcd" + "\r\n";
			sql += "     , a04.bededt" + "\r\n";
			sql += "     , a04.pid" + "\r\n";
			sql += "     , isnull(a04.bedodt,'') as bedodt" + "\r\n";
			sql += "     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age" + "\r\n";
			sql += "     , a04.wardid+case when isnull(a04.rmid,'')<>'' then '-' else '' end+isnull(a04.rmid,'')+case when isnull(a04.bedid,'')<>'' then '-' else '' end+isnull(a04.bedid,'') as ward" + "\r\n";
			sql += "     , (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid) as pdrnm" + "\r\n";
			sql += "     , a04.qlfycd as qfycd" + "\r\n";
			sql += "     , (select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=a04.qlfycd) as qfycdnm" + "\r\n";
			sql += "     , isnull((select top 1 dxd from tt05 t05 where t05.pid=a04.pid and t05.bdedt=a04.bededt order by convert(numeric,t05.ptysq),t05.seq),'') as dxd" + "\r\n";
			sql += "     , a01.bthdt" + "\r\n";
			sql += "  from ta04 a04 inner join ta01 a01 on a01.pid=a04.pid " + "\r\n";
			sql += " where a04.bededt = (select max(z.bededt) from ta04 z where z.pid=a04.pid)" + "\r\n";
			sql += "   /*isnull(a04.bedodiv,'') in ('0','')*/" + "\r\n";
			sql += "   /*and a04.wardid<>'er1'*/ " + "\r\n";
			sql += "   and a01.pnm like ?" + "\r\n";
			sql += " union all " + "\r\n";
			sql += "select a01.pnm" + "\r\n";
			sql += "     , a01.psex" + "\r\n";
			sql += "     , s21.dptcd" + "\r\n";
			sql += "     , s21.exdt as bededt" + "\r\n";
			sql += "     , s21.pid" + "\r\n";
			sql += "     , '' as bedodt" + "\r\n";
			sql += "     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age" + "\r\n";
			sql += "     , case when s21.dptcd='ER' then '응급' else '외래' end as ward" + "\r\n";
			sql += "     , (select a07.drnm from ta07 a07 where a07.drid=s21.drid) as pdrnm" + "\r\n";
			sql += "     , s21.qfycd as qfycd" + "\r\n";
			sql += "     , (select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=s21.qfycd) as qfycdnm" + "\r\n";
			sql += "     , isnull((select top 1 dxd from ts06 s06 where s06.pid=s21.pid and s06.exdt=s21.exdt and s06.dptcd=s21.dptcd order by convert(numeric,s06.ptysq),s06.seq),'') as dxd" + "\r\n";
			sql += "     , a01.bthdt" + "\r\n";
			sql += "  from ts21 s21 inner join ta01 a01 on a01.pid=s21.pid " + "\r\n";
			sql += " where s21.exdt=(select max(z.exdt) from ts21 z where z.pid=s21.pid and isnull(z.ccfg,'') in ('','0') and z.dptcd<>'ER')" + "\r\n";
			sql += "   and s21.dptcd<>'ER'" + "\r\n";
			sql += "   and isnull(s21.ccfg,'') in ('','0')" + "\r\n";
			sql += "   and a01.pnm like ?" + "\r\n";

			sql += " order by pnm,bededt desc ";
			
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	//private String getSqlInter(String sortOrder){
	//	String key="sql,Inter";
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
	//				"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id) as pdrnm" +
	//				"     , a04.insu_class as qfycd" +
	//				"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfycdnm" +
	//				"     , isnull((select top 1 dise_name from in_pat_dise_hist t05 where t05.pat_id=a04.pat_id and t05.bed_in_date=a04.bed_in_date order by convert(numeric,t05.prim_seq),t05.dise_no),'') as dxd" +
	//				"     , a01.birth_date as bthdt" +
	//			    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
	//			    " where a04.bed_in_date = (select max(z.bed_in_date) from in_pat_visit_hist z where z.pat_id=a04.pat_id)" +
	//			    "   /*and isnull(a04.bed_in_status,'') in ('0','')*/" +
	//				"   and a01.pat_name like ?" +
	//				" union all " +
	//				"select a01.pat_name pnm" +
	//				"     , a01.pat_sex psex" +
	//				"     , s21.dept_code dptcd" +
	//				"     , s21.visit_date as bededt" +
	//				"     , s21.pat_id pid" +
	//				"     , '' as bedodt" +
	//				"     , 0 as age" +
	//				"     , case when s21.dept_code='ER' then '응급' else '외래' end as ward" +
	//				"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=s21.doctor_id) as pdrnm" +
	//				"     , s21.insu_class as qfycd" +
	//				"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=s21.insu_class) as qfycdnm" +
	//				"     , isnull((select top 1 dise_name from out_pat_dise_hist s06 where s06.pat_id=s21.pat_id and s06.dise_date=s21.visit_date and s06.dept_code=s21.dept_code order by convert(numeric,s06.prim_seq),s06.dise_no),'') as dxd" +
	//				"     , a01.birth_date as bthdt" +
	//			    "  from out_pat_visit_hist s21 inner join pat_mast a01 on a01.pat_id=s21.pat_id " +
	//			    " where s21.visit_date=(select max(z.visit_date) from out_pat_visit_hist z where z.pat_id=s21.pat_id and isnull(s21.cancel_flag,'') in ('','0'))" +
	//			    "   and a01.pat_name like ?" +
	//			    "   and isnull(s21.cancel_flag,'') in ('','0')" +
	//			    " union all " +
	//				"select a01.pat_name pnm" +
	//				"     , a01.pat_sex psex" +
	//				"     , s21.dept_code dptcd" +
	//				"     , s21.visit_date as bededt" +
	//				"     , s21.pat_id pid" +
	//				"     , '' as bedodt" +
	//				"     , 0 as age" +
	//				"     , case when s21.dept_code='ER' then '응급' else '외래' end as ward" +
	//				"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=s21.doctor_id) as pdrnm" +
	//				"     , s21.insu_class as qfycd" +
	//				"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=s21.insu_class) as qfycdnm" +
	//				"     , isnull((select top 1 dise_name from er_pat_dise_hist s06 where s06.pat_id=s21.pat_id and s06.dise_date=s21.visit_date and s06.dept_code=s21.dept_code order by convert(numeric,s06.prim_seq),s06.dise_no),'') as dxd" +
	//				"     , a01.birth_date as bthdt" +
	//			    "  from er_pat_visit_hist s21 inner join pat_mast a01 on a01.pat_id=s21.pat_id " +
	//			    " where s21.visit_date = (select max(z.visit_date) from er_pat_visit_hist z where z.pat_id=s21.pat_id and isnull(z.cancel_flag,'') in ('','0'))" +
	//			    "   and isnull(s21.cancel_flag,'') in ('','0')" +
	//			    "   and a01.pat_name like ?" +
	//			    " order by pnm,bededt desc";
	//		sqlMap.put(key, sql);
	//	}
	//		
	//	return sqlMap.get(key);
	//}
}
