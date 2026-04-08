import java.util.HashMap;


public class MFGetNoticeNewYN implements MFGet {
	private static MFGetNoticeNewYN mInstance=null;
	private MFGetNoticeNewYN(){
		
	}
	
	public static MFGetNoticeNewYN getInstance(){
		if(mInstance==null){
			mInstance = new MFGetNoticeNewYN();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String lastapdt =  (String)param.get("lastapdt");
		String lastseq =  (String)param.get("lastseq");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		StringBuilder returnString = new StringBuilder();
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			String sql = 
					"select *" +
				    "  from ta95 " +
					" where apdt>?" +
					"    or (apdt=? and seq>?)" +
				    " order by apdt desc, seq desc";
			para.put(++idx, lastapdt);
			para.put(++idx, lastapdt);
			para.put(++idx, lastseq);
			String rsString = sqlHelper.executeQuery(sql,para,null);
			rs = new ResultSetHelper(rsString);
			if (rs.getReturnCode()<0) {
				returnString.append("N");
			}
			else if (rs.getRecordCount()==0) {
				returnString.append("N");
			}
			else {
				returnString.append("Y");

			}
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString.append("N");
		}
		
		return returnString.toString();
	}

}
