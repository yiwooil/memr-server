import java.sql.SQLException;
import java.util.HashMap;


public class MFGetDeptList implements MFGet {
	private static MFGetDeptList mInstance=null;
	private MFGetDeptList(){
		
	}
	
	public static MFGetDeptList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetDeptList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn=sqlHelper.getInterfaceTableYn();
			String sql="";
			if(interfaceTableYn){
				boolean isOracle = sqlHelper.isOracle();
				if(isOracle){
					sql = "select rtrim(dept_code) as deptcd,dept_name as deptnm from dept_mast order by dept_name";
				}else{
					sql = "select dept_code as deptcd,dept_name as deptnm from dept_mast order by dept_name";
				}
			}else{
				sql = "select dptcd as deptcd,dptnm as deptnm from ta09 where dptdiv='1' order by case when dptcd like 'TEST%' then 1 else 0 end, dptnm";
			}
			String rsString=sqlHelper.executeQuery(sql);
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

}
