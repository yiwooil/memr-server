import java.util.HashMap;


public class MFPutCertificateDel implements MFPut {
	private static MFPutCertificateDel mInstance=null;
	private MFPutCertificateDel(){
		
	}
	
	public static MFPutCertificateDel getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificateDel();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String ccfId = (String)param.get("ccfid");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper = null;
		try{
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
			if(interfaceTableYn){
				sql = "update consent_form_mast set del_yn='Y' where ccf_id=?";
			}else{
				sql = "update ta88 set fld4cd='Y' where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
			}
			para.put(1, ccfId);
			String rsString = sqlHelper.executeUpdate(sql, para);
			return rsString;
		}catch(Exception ex){
			return ExceptionHelper.toJSONString(ex);
		}
	}
}
