import java.sql.SQLException;
import java.util.HashMap;


public class MFGetDoctList implements MFGet {
	private static MFGetDoctList mInstance=null;
	private MFGetDoctList(){
		
	}
	
	public static MFGetDoctList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetDoctList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String dptcd = (String)param.get("dptcd");
		
		if(dptcd==null) dptcd="";
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn=sqlHelper.getInterfaceTableYn();
			String sql="";
			//if(interfaceTableYn){
			//	boolean isOracle = sqlHelper.isOracle();
			//	if(isOracle){
			//		sql = "";
			//		sql += "select rtrim(doctor_id) as drid,doctor_name as drnm";
			//		sql += "  from doctor_mast";
			//		sql += "	where doctor_name is not null";
			//		sql += " order by doctor_name";
			//	}else{
			//		sql = "";
			//		sql += "select doctor_id as drid,doctor_name as drnm";
			//		sql += "  from doctor_mast";
			//		sql += " where doctor_name is not null";
			//		sql += " order by doctor_name";
			//	}
			//}else{
				// 2019.07.25 WOOIL - 퇴사의사제외,DRG,DRGBAL제외
				sql = "";
				sql += "select drid,drnm,drengnm,gdrlcid,sdrlcid,dptcd,'sign_'+drid as drsign";
				sql += "  from ta07";
				sql += " where isnull(drnm,'')<>''";
				sql += "   and isnull(expdt,'')=''";
				sql += "   and drid not in ('DRG','DRGBAL')";
				if(!"".equals(dptcd)){
				sql += "   and dptcd='" + dptcd + "'"; // 특정진료과의 의사만
				}
				sql += " order by case when drid like 'AATEST%' then 1 else 0 end, drnm";
			//}
			String rsString=sqlHelper.executeQuery(sql);
			returnString=rsString;
		}catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
			//returnString=new ExceptionHelper(ex).toJSONString();
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
			//returnString=new ExceptionHelper(ex).toJSONString();
		}
		return returnString;		
	}

}
