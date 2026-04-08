import java.util.HashMap;

public class MFGetCertificateDelCheck implements MFGet {
	private static MFGetCertificateDelCheck mInstance=null;
	private MFGetCertificateDelCheck(){
		
	}
	
	public static MFGetCertificateDelCheck getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCertificateDelCheck();
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
		String seq = (String)param.get("seq");
		String presavedYn = (String)param.get("presaved_yn");
		
		if (pid == null) pid = "";
		if (bdiv == null) bdiv = "";
		if (exdt == null) exdt = "";
		if (seq == null) seq = "";
		if (presavedYn == null) presavedYn = "";
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "pid=" + pid + ", bdiv=" + bdiv+ ", exdt=" + exdt + ", seq=" + seq + ", presavedYn=" + presavedYn);
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString="";
		SqlHelper sqlHelper;
		
		try {
			String sql = "";
			if ("y".equalsIgnoreCase(presavedYn)) {
				sql += "select isnull(delfg,'') as delfg" + "\n";
				sql += "  from tg02t" + "\n";
				sql += " where pid=?" + "\n";
				sql += "   and bdiv=?" + "\n";
				sql += "   and exdt=?" + "\n";
				sql += "   and seq=?" + "\n";
			} else {
				sql += "select isnull(afexdt,'') as afexdt" + "\n";
				sql += "  from tg02" + "\n";
				sql += " where pid=?" + "\n";
				sql += "   and bdiv=?" + "\n";
				sql += "   and exdt=?" + "\n";
				sql += "   and seq=?" + "\n";
			}
			
			sqlHelper = new SqlHelper(hospitalId);
			para.put(1, pid);
			para.put(2, bdiv);
			para.put(3, exdt);
			para.put(4, seq);
			
			String rsString = sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}

}
