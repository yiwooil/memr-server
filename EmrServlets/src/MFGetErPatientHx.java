import java.sql.SQLException;
import java.util.HashMap;


public class MFGetErPatientHx implements MFGet {
	private static MFGetErPatientHx mInstance=null;
	private MFGetErPatientHx(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetErPatientHx getInstance(){
		if(mInstance==null){
			mInstance = new MFGetErPatientHx();
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
					sql = getSqlInterOracle();
				}else{
					sql = getSqlInter();
				}
			}else{
				sql = getSql();
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
					"     , s21.dptcd" +
					"     , s21.exdt as bededt" +
					"     , s21.pid" +
					"     , '' as bedodt" +
					"     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age" +
					"     , case when s21.dptcd='ER' then '응급' else '외래' end as ward" +
					"     , (select a07.drnm from ta07 a07 where a07.drid=s21.drid) as pdrnm" +
					"     , s21.qfycd as qfycd" +
					"     , (select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=s21.qfycd) as qfycdnm" +
					"     , isnull((select top 1 dxd from ts06 s06 where s06.pid=s21.pid and s06.exdt=s21.exdt and s06.dptcd=s21.dptcd order by convert(numeric,s06.ptysq),s06.seq),'') as dxd" +
					"     , a01.bthdt" +
				    "  from ts21 s21 inner join ta01 a01 on a01.pid=s21.pid " +
				    " where s21.pid=?" +
				    "   and s21.dptcd='ER'" +
				    "   and isnull(s21.ccfg,'') in ('','0')" +
				    " order by s21.pid,s21.exdt desc";
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
					"     , s21.dept_code dptcd" +
					"     , s21.visit_date as bededt" +
					"     , s21.pat_id pid" +
					"     , '' as bedodt" +
					"     , 0 as age" +
					"     , case when s21.dept_code='ER' then '응급' else '외래' end as ward" +
					"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=s21.doctor_id) as pdrnm" +
					"     , s21.insu_class as qfycd" +
					"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=s21.insu_class) as qfycdnm" +
					"     , isnull((select top 1 dise_name from er_pat_dise_hist s06 where s06.pat_id=s21.pat_id and s06.dise_date=s21.visit_date and s06.dept_code=s21.dept_code order by convert(numeric,s06.prim_seq),s06.dise_no),'') as dxd" +
					"     , a01.birth_date as bthdt" +
				    "  from er_pat_visit_hist s21 inner join pat_mast a01 on a01.pat_id=s21.pat_id " +
				    " where s21.pat_id=?" +
				    "   and isnull(s21.cancel_flag,'') in ('','0')" +
				    " order by s21.pat_id,s21.visit_date desc";
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
					"     , s21.dept_code dptcd" +
					"     , s21.visit_date as bededt" +
					"     , s21.pat_id pid" +
					"     , '' as bedodt" +
					"     , '' as age" +
					"     , '응급'  as ward" +
					"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=s21.doctor_id) as pdrnm" +
					"     , s21.insu_class as qfycd" +
					"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=s21.insu_class) as qfycdnm" +
					"     , (select s06.dise_name" +
					"          from out_pat_dise_hist s06" +
					"         where s06.pat_id=s21.pat_id" +
					"           and s06.dise_date=s21.visit_date" +
					"           and s06.dept_code=s21.dept_code" +
					"           and (s06.prim_seq,s06.dise_no) in (select min(cast(x.prim_seq as number)),min(x.dise_no)" +
					"                                                from out_pat_dise_hist x" +
					"                                               where x.pat_id=s06.pat_id" +
					"                                                 and x.dise_date=s06.dise_date" +
					"                                                 and x.dept_code=s06.dept_code" +
					"                                             )" +
					"       ) as dxd" +
					"     , a01.birth_date as bthdt" +
				    "  from er_pat_visit_hist s21 inner join pat_mast a01 on a01.pat_id=s21.pat_id " +
				    " where s21.pat_id=?" +
				    "   and (s21.cancel_flag='0' or s21.cancel_flag is null)" +
				    " order by s21.pat_id,s21.visit_date desc";
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
					"     , s21.dept_code dptcd" +
					"     , s21.visit_date as bededt" +
					"     , rtrim(s21.pat_id||s21.pat_id2) pid" +
					"     , '' as bedodt" +
					"     , '' as age" +
					"     , '응급'  as ward" +
					"     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=s21.doctor_id) as pdrnm" +
					"     , s21.insu_class as qfycd" +
					"     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=s21.insu_class) as qfycdnm" +
					"     , (select s06.dise_name" +
					"          from out_pat_dise_hist s06" +
					"         where s06.pat_id=s21.pat_id" +
					"           and s06.pat_id2=s21.pat_id2" +
					"           and s06.dise_date=s21.visit_date" +
					"           and s06.dept_code=s21.dept_code" +
					"           and (s06.prim_seq,s06.dise_no) in (select min(cast(x.prim_seq as number)),min(x.dise_no)" +
					"                                                from out_pat_dise_hist x" +
					"                                               where x.pat_id=s06.pat_id" +
					"                                                 and x.pat_id2=s06.pat_id2" +
					"                                                 and x.dise_date=s06.dise_date" +
					"                                                 and x.dept_code=s06.dept_code" +
					"                                             )" +
					"       ) as dxd" +
					"     , a01.birth_date as bthdt" +
				    "  from er_pat_visit_hist s21 inner join pat_mast a01 on a01.pat_id=s21.pat_id " +
				    " where s21.pat_id=?" +
				    "   and s21.pat_id2=?" +
				    "   and (s21.cancel_flag='0' or s21.cancel_flag is null)" +
				    " order by s21.pat_id,s21.visit_date desc";
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}
	
}
