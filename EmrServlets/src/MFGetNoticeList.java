import java.util.HashMap;


public class MFGetNoticeList implements MFGet {
	private static MFGetNoticeList mInstance=null;
	private MFGetNoticeList(){
		
	}
	
	public static MFGetNoticeList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetNoticeList();
		}
		return mInstance;
	}
	
	@Override
	public String getData(HashMap<String,Object>param) throws Exception{
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		
		String returnString = "";
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		
		try {

			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql="";
			if(interfaceTableYn){
				sql = getSqlInter();
				// oracle은 isnull 이아니고 nvl을 사용한다.
				boolean isOracle = sqlHelper.isOracle();
				if(isOracle){
					sql = sql.replace("isnull(", "nvl(");
				}
			}else{
				sql = getSql();
			}
			String rsString = sqlHelper.executeQuery(sql);
			returnString=rsString;
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		
		return returnString.toString();
	}
	
	private String getSql(){
		String sql = 
				"select a.apdt,a.seq,a.title,a.psid,isnull(b.drnm,'') as drnm,isnull(c.empnm,'') as empnm" +
			    "  from ta95 a left join ta07 b on b.drid=a.psid " +
			    "              left join ta13 c on c.empid=a.psid " +
			    " order by a.apdt desc, a.seq desc";
		return sql;
	}
	
	private String getSqlInter(){
		String sql = 
				"select a.notice_date apdt,a.notice_no seq,a.title title,a.emp_id psid,isnull(b.doctor_name,'') as drnm,isnull(c.emp_name,'') as empnm" +
			    "  from notice_hist a left join doctor_mast b on b.doctor_id=a.emp_id " +
			    "              left join emp_mast c on c.emp_id=a.emp_id " +
			    " order by a.notice_date desc, a.notice_no desc";
		return sql;
	}
}
