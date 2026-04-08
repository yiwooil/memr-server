import java.util.HashMap;


public class MFPutCertificateEmrScanClass implements MFPut {
	private static MFPutCertificateEmrScanClass mInstance=null;
	private MFPutCertificateEmrScanClass(){
		
	}
	
	public static MFPutCertificateEmrScanClass getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificateEmrScanClass();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String ccfId = (String)param.get("ccfid");
		String emrScanClass = (String)param.get("emrscanclass");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper = null;
		try{
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
			if(interfaceTableYn){
				sql = "update consent_form_mast set emr_scan_class=? where ccf_id=?";
			}else{
				sql = "update ta88 set fld3cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
			}
			para.put(1, emrScanClass);
			para.put(2, ccfId);
			String rsString = sqlHelper.executeUpdate(sql, para);
			return rsString;
		}catch(Exception ex){
			return ExceptionHelper.toJSONString(ex);
		}
	}
}
