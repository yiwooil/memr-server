import java.sql.SQLException;
import java.util.HashMap;

public class MFGetCertificatePreSavedInfos implements MFGet {
	private static MFGetCertificatePreSavedInfos mInstance=null;
	private MFGetCertificatePreSavedInfos(){
		
	}
	
	public static MFGetCertificatePreSavedInfos getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCertificatePreSavedInfos();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String bdiv = (String)param.get("bdiv");
		String exdt = (String)param.get("exdt");
		String seq= (String)param.get("seq");
		String preSavedBdiv = (String)param.get("pre_saved_bdiv");
		String reSaveYn = (String)param.get("re_save_yn");

		if (reSaveYn == null) reSaveYn = "";
		
		// 2026.02.04 WOOIL - 임시 저장동의서만 bdiv를 변경한다.
		if (!"Y".equalsIgnoreCase(reSaveYn)) bdiv = preSavedBdiv;
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "pid=" + pid + ", bdiv=" + bdiv + ", exdt=" + exdt + ", seq=" + seq + ", preSavedBdiv=" + preSavedBdiv + ", reSaveYn=" + reSaveYn);
		
		String returnString="";
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			String sql = "";
			if ("Y".equalsIgnoreCase(reSaveYn)){
				sql = "select path from tg02 where pid=? and bdiv=? and exdt=? and seq=? and rptcd='ZZ01'";
			}else{
				sql = "select path from tg02t where pid=? and bdiv=? and exdt=? and seq=? and rptcd='ZZ01'";
			}
			para.put(1, pid);
			para.put(2, bdiv);
			para.put(3, exdt);
			para.put(4, seq);
			String rsString = sqlHelper.executeQuery(sql,para,null);
			new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "rsString=" + rsString);
			returnString = rsString;
		} catch (SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}

}
