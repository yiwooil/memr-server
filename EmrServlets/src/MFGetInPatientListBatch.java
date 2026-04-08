import java.sql.SQLException;
import java.util.HashMap;


public class MFGetInPatientListBatch implements MFGet {
	private static MFGetInPatientListBatch mInstance=null;
	private MFGetInPatientListBatch(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetInPatientListBatch getInstance(){
		if(mInstance==null){
			//System.out.println(Utility.getCurrentDateTime() + " : MFGetInPatientList 새 인스턴스 생성");
			mInstance = new MFGetInPatientListBatch();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		String returnString;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			boolean isJaincom = sqlHelper.isJaincom();
			String sql="";
			if(interfaceTableYn){
				boolean isOracle = sqlHelper.isOracle();
				if(isJaincom){
					sql = getSqlInterOracleJaincom();
				}else if(isOracle){
					sql = getSqlInterOracle();
				}else{
					sql = getSqlInter();
				}
			}else{
				sql = getSql();
			}
			
			String rsString=sqlHelper.executeQuery(sql,para,null);
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
	public String getSql() {
		String key="sql";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select a01.pnm" +
					"     , a01.psex" +
					"     , a04.dptcd" +
					"     , a04.bededt" +
					"     , a04.pid" +
					"     , isnull(a04.bedodt,'') as bedodt" +
					"     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age" +
					"     , a04.wardid+(case when isnull(a04.rmid,'')<>'' then '-' else '' end)+isnull(a04.rmid,'')+(case when isnull(a04.bedid,'')<>'' then '-' else '' end)+isnull(a04.bedid,'') as ward" +
					"     , (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid) as pdrnm" +
					"     , a04.qlfycd as qfycd" +
					"     , (select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=a04.qlfycd) as qfycdnm" +
					"     , isnull((select top 1 dxd from tt05 t05 where t05.pid=a04.pid and t05.bdedt=a04.bededt order by convert(numeric,t05.ptysq),t05.seq),'') as dxd" +
					"     , a01.bthdt" +
					"     , a04.wardid,a04.rmid,a04.bedid,a04.pdrid" +
				    "  from ta04 a04 inner join ta01 a01 on a01.pid=a04.pid " +
				    " where (a04.bedodiv in ('0','') or a04.bedodiv is null)" +
				    "   /*and a04.wardid<>'er1'*/ ";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	public String getSqlInter() {
		String key="Inter";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select a01.pat_name as pnm" +
					"     , a01.pat_sex as psex" +
					"     , a04.dept_code as dptcd" +
					"     , a04.bed_in_date as bededt" +
					"     , a04.pat_id as pid" +
					"     , isnull(a04.bed_out_date,'') as bedodt" +
					"     , '' as age" +
					"     , a04.ward_id+(case when isnull(a04.room_id,'')<>'' then '-' else '' end)+isnull(a04.room_id,'')+(case when isnull(a04.bed_id,'')<>'' then '-' else '' end)+isnull(a04.bed_id,'') as ward" +
					"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id) as pdrnm" +
					"     , a04.insu_class as qfycd" +
					"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfycdnm" +
					"     , isnull((select top 1 dise_name from in_pat_dise_hist t05 where t05.pat_id=a04.pat_id and t05.bed_in_date=a04.bed_in_date order by convert(numeric,t05.prim_seq),t05.dise_no),'') as dxd" +
					"     , a01.birth_date as bthdt" +
					"     , a04.wardid,a04.rmid,a04.bedid,a04.pdrid" +
				    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
				    " where isnull(a04.bed_in_status,'') in ('0','')" +
				    "   /*and a04.wardid<>'er1'*/ ";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	public String getSqlInterOracle() {
		String key="InterOracle";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select a01.pat_name as pnm" +
					"     , a01.pat_sex as psex" +
					"     , a04.dept_code as dptcd" +
					"     , a04.bed_in_date as bededt" +
					"     , a04.pat_id as pid" +
					"     , a04.bed_out_date as bedodt" +
					"     , '' as age" +
					"     , case when a04.ward_id is not null and a04.room_id is not null and a04.bed_id is not null then a04.ward_id||'-'||a04.room_id||'-'||a04.bed_id" +
					"            when a04.ward_id is not null and a04.room_id is not null and a04.bed_id is null then a04.ward_id||'-'||a04.room_id" +
					"            when a04.ward_id is not null and a04.room_id is null and a04.bed_id is null then a04.ward_id" +
					"            else null" +
					"       end as ward" +
					"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id) as pdrnm" +
					"     , a04.insu_class as qfycd" +
					"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfycdnm" +
					"     , (select t05.dise_name" +
					"          from in_pat_dise_hist t05" +
					"         where t05.pat_id=a04.pat_id" +
					"           and t05.bed_in_date=a04.bed_in_date" +
					"           and (t05.prim_seq,t05.dise_no) in (select min(cast(x.prim_seq as number)),min(x.dise_no)" +
					"                                                from in_pat_dise_hist x" +
					"                                               where x.pat_id=t05.pat_id" +
					"                                                 and x.bed_in_date=t05.bed_in_date" +
					"                                             )" +
					"       ) as dxd" +
					"     , a01.birth_date as bthdt" +
					"     , a04.ward_id wardid,a04.room_id rmid,a04.bed_id bedid,a04.doctor_id pdrid" +
				    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
				    " where (a04.bed_in_status='0' or a04.bed_in_status is null)" +
				    "    ";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	public String getSqlInterOracleJaincom() {
		String key="InterOracleJaincom";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select a01.pat_name as pnm" +
					"     , a01.pat_sex as psex" +
					"     , a04.dept_code as dptcd" +
					"     , a04.bed_in_date as bededt" +
					"     , rtrim(a04.pat_id||a04.pat_id2) as pid" +
					"     , a04.bed_out_date as bedodt" +
					"     , '' as age" +
					"     , case when a04.ward_id is not null and a04.room_id is not null and a04.bed_id is not null then a04.ward_id||'-'||a04.room_id||'-'||a04.bed_id" +
					"            when a04.ward_id is not null and a04.room_id is not null and a04.bed_id is null then a04.ward_id||'-'||a04.room_id" +
					"            when a04.ward_id is not null and a04.room_id is null and a04.bed_id is null then a04.ward_id" +
					"            else null" +
					"       end as ward" +
					"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id and rownum<2) as pdrnm" +
					"     , a04.insu_class as qfycd" +
					"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfycdnm" +
					"     , (select t05.dise_name" +
					"          from in_pat_dise_hist t05" +
					"         where t05.pat_id=a04.pat_id" +
					"           and t05.pat_id2=a04.pat_id2" +
					"           and t05.bed_in_date=a04.bed_in_date" +
					"           and (t05.prim_seq,t05.dise_no) in (select min(cast(x.prim_seq as number)),min(x.dise_no)" +
					"                                                from in_pat_dise_hist x" +
					"                                               where x.pat_id=t05.pat_id" +
					"                                                 and x.pat_id2=t05.pat_id2" +
					"                                                 and x.bed_in_date=t05.bed_in_date" +
					"                                             )" +
					"           and rownum<2" +
					"       ) as dxd" +
					"     , a01.birth_date as bthdt" +
					"     , a04.ward_id wardid,a04.room_id rmid,a04.bed_id bedid,a04.doctor_id pdrid" +
				    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
				    " where (a04.bed_in_status='0' or a04.bed_in_status is null)" +
				    "    ";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}

}
