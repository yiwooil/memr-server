import java.sql.SQLException;
import java.util.HashMap;

public class MFGetOutPatientList implements MFGet {
	private static MFGetOutPatientList mInstance=null;
	private MFGetOutPatientList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetOutPatientList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetOutPatientList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("usrid");
		String exdt = (String)param.get("exdt");
		String dept = (String)param.get("dept");
		String pdrid = (String)param.get("pdrid");
		String sortOrder = (String)param.get("sortorder");
		String rsvInOnly = (String)param.get("rsv_in_only");
		
		if(sortOrder==null) sortOrder="1";
		if(rsvInOnly==null) rsvInOnly="";
		
		//String pid2=""; // 자인컴은 id2가 있음.
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();

		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			//boolean isJaincom = sqlHelper.isJaincom();
			String sql="";
			sql = getSql(exdt, dept, pdrid, sortOrder, rsvInOnly);

			para.put(++idx, exdt);
			if(!"".equals(dept)) para.put(++idx, dept);
			if(!"".equals(pdrid)) para.put(++idx, pdrid);
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

	private String getSql(String exdt, String dept, String pdrid, String sortOrder, String rsvInOnly){
		String key=sortOrder+",exdt";
		if(!"".equals(dept)) key+=",dptcd";
		if(!"".equals(pdrid)) key+=",pdrid";
		key+=","+rsvInOnly;
		if(sqlMap.containsKey(key)==false){
			String sql = "";
			sql += "select a01.pnm";
			sql += "     , a01.psex";
			sql += "     , s21.dptcd";
			sql += "     , s21.exdt as bededt";
			sql += "     , s21.pid";
			sql += "     , s21.hms as bedodt";
			sql += "     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age";
			sql += "     , case when s21.dptcd='ER' then '응급' else '외래' end as ward";
			sql += "     , (select a07.drnm from ta07 a07 where a07.drid=s21.drid) as pdrnm";
			sql += "     , s21.qfycd as qfycd";
			sql += "     , (select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=s21.qfycd) as qfycdnm";
			sql += "     , isnull((select top 1 dxd from ts06 s06 where s06.pid=s21.pid and s06.exdt=s21.exdt and s06.dptcd=s21.dptcd order by convert(numeric,case when isnull(s06.ptysq,'')='' then '99' else s06.ptysq end),s06.seq),'') as dxd";
			sql += "     , a01.bthdt";
			sql += "     , s21.hms";
			sql += "     , s21.drid";
			sql += "  from ts21 s21 inner join ta01 a01 on a01.pid=s21.pid ";
			sql += " where s21.exdt=?";
			sql += "   and isnull(s21.ccfg,'') in ('','0')";
			if(!"".equals(dept)){
				sql +=
					"   and s21.dptcd=?";
			}
			if(!"".equals(pdrid)){
				sql +=
					"   and s21.drid=?";
			}
			if("y".equalsIgnoreCase(rsvInOnly)){
				sql +=
					"   and exists (select * from tt02 t02 where t02.pid=s21.pid and t02.odt=s21.exdt and t02.dptcd=s21.dptcd and t02.pdrid=s21.drid)";
			}
			if("1".equals(sortOrder)){
				// 접수일시
				sql +=
					" order by s21.exdt,s21.hms desc,s21.dptcd,s21.drid,s21.pid";
			}else if("2".equals(sortOrder)){
				// 환자명
				sql +=
					    " order by a01.pnm,s21.exdt,s21.hms desc,s21.dptcd,s21.drid,s21.pid";
			}else if("3".equals(sortOrder)){
				// 진료과+접수일시
				sql +=
					    " order by s21.dptcd,s21.exdt,s21.hms desc,s21.drid,s21.pid";
			}else if("4".equals(sortOrder)){
				// 진료과+환자명
				sql +=
					    " order by a01.pnm,s21.dptcd,s21.exdt,s21.hms desc,s21.drid,s21.pid";
			}else{
				// 기본 = 접수일시
				sql +=
				    " order by s21.exdt,s21.hms desc,s21.dptcd,s21.drid,s21.pid";
			}
			sqlMap.put(key, sql);
		}
		return sqlMap.get(key);
	}	
}
