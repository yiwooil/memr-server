import java.sql.SQLException;
import java.util.HashMap;


public class MFGetInPatientHx implements MFGet {
	private static MFGetInPatientHx mInstance=null;
	private MFGetInPatientHx(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetInPatientHx getInstance(){
		if(mInstance==null){
			mInstance = new MFGetInPatientHx();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("usrid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");

		String pid2=""; // 자인컴은 id2가 있음.
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
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
					int pidLen = sqlHelper.getJainComPidLen(); // 2016.11.04 WOOIL
					String patientId = pid;
					if(patientId.length()<=pidLen){
						pid=patientId;
						pid2=" ";							
					}else{
						pid=patientId.substring(0, pidLen);
						pid2=patientId.substring(pidLen, pidLen+1);
					}
					sql = getSqlInterOracleJaincom();
				}else if(isOracle){
					sql=getSqlInterOracle();
				}else{
					sql=getSqlInter();
				}
			}else{
				sql=getSql();
			}
			para.put(++idx, pid);
			if(isJaincom) para.put(++idx, pid2);
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

	private String getSql(){
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
					"     , a04.wardid+case when isnull(a04.rmid,'')<>'' then '-' else '' end+isnull(a04.rmid,'')+case when isnull(a04.bedid,'')<>'' then '-' else '' end+isnull(a04.bedid,'') as ward" +
					"     , (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid) as pdrnm" +
					"     , a04.qlfycd as qfycd" +
					"     , (select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=a04.qlfycd) as qfycdnm" +
					"     , isnull((select top 1 dxd from tt05 t05 where t05.pid=a04.pid and t05.bdedt=a04.bededt order by convert(numeric,t05.ptysq),t05.seq),'') as dxd" +
					"     , a01.bthdt" +
				    "  from ta04 a04 inner join ta01 a01 on a01.pid=a04.pid " +
				    " where a04.pid=?" +
				    "   and a04.wardid<>'ER1'" +
				    " order by a04.pid,a04.bededt desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInter(){
		String key="Inter";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select a01.pat_name pnm" +
					"     , a01.pat_sex psex" +
					"     , a04.dept_code dptcd" +
					"     , a04.bed_in_date bededt" +
					"     , a04.pat_id pid" +
					"     , isnull(a04.bed_out_date,'') as bedodt" +
					"     , 0 as age" +
					"     , a04.ward_id+case when isnull(a04.room_id,'')<>'' then '-' else '' end+isnull(a04.room_id,'')+case when isnull(a04.bed_id,'')<>'' then '-' else '' end+isnull(a04.bed_id,'') as ward" +
					"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id) as pdrnm" +
					"     , a04.insu_class as qfycd" +
					"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfycdnm" +
					"     , isnull((select top 1 dise_name from in_pat_dise_hist t05 where t05.pat_id=a04.pat_id and t05.bed_in_date=a04.bed_in_date order by convert(numeric,t05.prim_seq),t05.dise_no),'') as dxd" +
					"     , a01.birth_date as bthdt" +
				    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
				    " where a04.pat_id=?" +
				    " order by a04.pat_id,a04.bed_in_date desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInterOracle(){
		String key="InterOracle";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select a01.pat_name pnm" +
					"     , a01.pat_sex psex" +
					"     , a04.dept_code dptcd" +
					"     , a04.bed_in_date bededt" +
					"     , a04.pat_id pid" +
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
				    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
				    " where a04.pat_id=?" +
				    " order by a04.pat_id,a04.bed_in_date desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
	private String getSqlInterOracleJaincom(){
		String key="InterOracleJaincom";
		if(sqlMap.containsKey(key)==false){
			String sql = 
					"select a01.pat_name pnm" +
					"     , a01.pat_sex psex" +
					"     , a04.dept_code dptcd" +
					"     , a04.bed_in_date bededt" +
					"     , rtrim(a04.pat_id||a04.pat_id2) pid" +
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
				    "  from in_pat_visit_hist a04 inner join pat_mast a01 on a01.pat_id=a04.pat_id " +
				    " where a04.pat_id=?" +
				    "   and a04.pat_id2=?" +
				    " order by a04.pat_id,a04.bed_in_date desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
}
