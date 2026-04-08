import java.sql.SQLException;
import java.util.HashMap;

public class MFGetCertificatePaperPath implements MFGet {
	private static MFGetCertificatePaperPath mInstance=null;
	private MFGetCertificatePaperPath(){
		
	}
	
	public static MFGetCertificatePaperPath getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCertificatePaperPath();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userid = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String bdiv = (String)param.get("bdiv");
		String exdt = (String)param.get("exdt");
		String seq = (String)param.get("seq");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString = "";
		SqlHelper sqlHelper;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "select path from tg02 where pid=? and bdiv=? and exdt=? and seq=? and rptcd='ZZ01'";
			para.put(++idx, pid);
			para.put(++idx, bdiv);
			para.put(++idx, exdt);
			para.put(++idx, seq);
			String rsString = sqlHelper.executeQuery(sql,para,null);
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

}
