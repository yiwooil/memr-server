import java.util.HashMap;


public class MFPutCertificateNew implements MFPut {
	private static MFPutCertificateNew mInstance=null;
	private MFPutCertificateNew(){
		
	}
	
	public static MFPutCertificateNew getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificateNew();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String ccfId = (String)param.get("ccfid");
		String ccfName = (String)param.get("ccf_name");
		String ccfFile = (String)param.get("ccf_file");
		String ccfGroup = (String)param.get("ccf_group");
		String emrScanClass = (String)param.get("emr_scan_class");
		
		if("".equalsIgnoreCase(ccfGroup)) ccfGroup = "기타";
		
		boolean isNew=false;
		if("".equalsIgnoreCase(ccfId)) isNew=true; // ccfid가 없으면 신규저장모드임.
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper = null;
		ResultSetHelper rs = null;
		try{
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			int maxIdx=0;
			if(isNew==true){
				// ccfid를 구한다.
				int maxMst3cd=0;
				String sql = "";
				if(interfaceTableYn){
					sql =
						"select isnull(max(convert(numeric,ccf_id)),0) as max_mst3cd" +
						"  from consent_form_mast";
				}else{
					sql =
						"select isnull(max(convert(numeric,mst3cd)),0) as max_mst3cd" +
						"  from ta88" +
						" where mst1cd='EMR'" +
						"   and mst2cd='FORM'";
				}
				String rsString = sqlHelper.executeQuery(sql);
				rs = new ResultSetHelper(rsString);
				if(rs.getReturnCode()<0){
					return rsString;
				}else{
					maxMst3cd = rs.getInt(0, "max_mst3cd");
				}
				ccfId = Integer.toString(maxMst3cd+1);
				// 마지막 출력순서를 구한다.
				if(interfaceTableYn){
					sql =
						"select isnull(max(convert(numeric,disp_order)),0) as max_idx" +
					    "  from consent_form_mast" +
						" where case when isnull(ccf_group,'')='' then '기타' else ccf_group end = ?";
				}else{
					sql =
						"select isnull(max(convert(numeric,fld1cd)),0) as max_idx" +
					    "  from ta88" +
						" where mst1cd='EMR'" +
					    "   and mst2cd='FORM'" +
						"   and case when isnull(fld2cd,'')='' then '기타' else fld2cd end = ?";
				}
				para.put(1, ccfGroup);
				rsString = sqlHelper.executeQuery(sql, para,null);
				rs = new ResultSetHelper(rsString);
				if(rs.getReturnCode()<0){
					return rsString;
				}else if(rs.getRecordCount()==0){
					maxIdx = 0;
				}else{
					maxIdx = rs.getInt(0, "max_idx");
				}
			}
			// 저장
			String exeString = "";
			if(isNew==true){
				String sql = "";
				if(interfaceTableYn){
					sql = "insert into consent_form_mast(ccf_id,ccf_name,ccf_filename,disp_order,ccf_group,emr_scan_class) values(?,?,?,?,?,?)";
				}else{
					sql = "insert into ta88(mst1cd,mst2cd,mst3cd,cdnm,fld1qty,fld1cd,fld2cd,fld3cd) values('EMR','FORM',?,?,?,?,?,?)";
				}
				para.clear();
				para.put(1, ccfId);
				para.put(2, ccfName);
				para.put(3, ccfFile);
				para.put(4, maxIdx+1);
				para.put(5, ccfGroup);
				para.put(6, emrScanClass);
				exeString = sqlHelper.executeUpdate(sql, para);
			}else{
				String sql = "";
				if(interfaceTableYn){
					sql = "update consent_form_mast set ccf_name=?,ccf_filename=? where ccf_id=?";
				}else{
					sql = "update ta88 set cdnm=?,fld1qty=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
				}
				para.clear();
				para.put(1, ccfName);
				para.put(2, ccfFile);
				para.put(3, ccfId);
				exeString = sqlHelper.executeUpdate(sql, para);
			}
			return exeString;
		}catch(Exception ex){
			return ExceptionHelper.toJSONString(ex);
		}
	}
}
