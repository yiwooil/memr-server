import java.util.HashMap;


public class MFPutCertificateName implements MFPut {
	private static MFPutCertificateName mInstance=null;
	private MFPutCertificateName(){
		
	}
	
	public static MFPutCertificateName getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificateName();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String ccfId = (String)param.get("ccfid");
		String ccfName = (String)param.get("ccf_name");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper = null;
		try{
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
			if(interfaceTableYn){
				sql = "update consent_form_mast set ccf_name=? where ccf_id=?";
			}else{
				sql = "update ta88 set cdnm=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
			}
			para.put(1, ccfName);
			para.put(2, ccfId);
			String rsString = sqlHelper.executeUpdate(sql, para);
			return rsString;
		}catch(Exception ex){
			return ExceptionHelper.toJSONString(ex);
		}
	}
}
