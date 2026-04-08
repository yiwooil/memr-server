import java.util.HashMap;


public class MFPutCertificateOrderUp implements MFPut {
	private static MFPutCertificateOrderUp mInstance=null;
	private MFPutCertificateOrderUp(){
		
	}
	
	public static MFPutCertificateOrderUp getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificateOrderUp();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String ccfId = (String)param.get("ccfid");
		
		//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "hospitalId=" + hospitalId + ", ccfId=" + ccfId);
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, Object>para1=new HashMap<Integer,Object>();
		HashMap<Integer, Object>para2=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper = null;
		ResultSetHelper rs = null;
		try{
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			// 이 동의서의 출력순서를 구한다.
			String sql = "";
			if(interfaceTableYn){
				sql =
					"select disp_order as fld1cd,case when isnull(ccf_group,'')='' then '기타' else ccf_group end ccf_group" +
				    "  from consent_form_mast" +
					" where ccf_id=?";
			}else{
				sql =
					"select fld1cd,case when isnull(fld2cd,'')='' then '기타' else fld2cd end ccf_group" +
				    "  from ta88" +
					" where mst1cd='EMR' and mst2cd='FORM'" +
				    "   and mst3cd=?";
			}
			para.put(1, ccfId);
			String rsString = sqlHelper.executeQuery(sql, para, null);
			//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "rsString=" + rsString);
			rs = new ResultSetHelper(rsString);
			if(rs.getReturnCode()<0){
				return rsString;
			}
			int orderIdx = rs.getInt(0, "fld1cd");
			String ccfGroup = rs.getString(0, "ccf_group");
			if(orderIdx==1){
			    return ExceptionHelper.toJSONString(-1, "맨 위에 있습니다. 처리할 수 없습니다.");
			}
			//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccfGroup=" + ccfGroup + ", orderIdx=" + orderIdx);
			// 이 동의서 위에 있는 동의서를 구한다.
			String upCcfId = "";
			int upOrderIdx = 0;
			if(interfaceTableYn){
				sql =
					"select ccf_id as mst3cd,disp_order as fld1cd" +
					"  from consent_form_mast" +
					" where convert(numeric,disp_order)<=?" +
					"   and case when isnull(ccf_group,'')='' then '기타' else ccf_group end=?" +
					" order by convert(numeric,disp_order) desc";
			}else{
				sql =
					"select mst3cd,fld1cd" +
					"  from ta88" +
					" where mst1cd='EMR' and mst2cd='FORM'" +
					"   and convert(numeric,fld1cd)<=?" +
					"   and case when isnull(fld2cd,'')='' then '기타' else fld2cd end=?" +
					" order by convert(numeric,fld1cd) desc";
			}
			para.clear();
			para.put(1, orderIdx-1);
			para.put(2, ccfGroup);
			rsString = sqlHelper.executeQuery(sql, para, null);
			rs = new ResultSetHelper(rsString);
			if(rs.getReturnCode()<0){
				return rsString;
			}
			if(rs.getRecordCount()>0){
				upCcfId = rs.getString(0, "mst3cd");
				upOrderIdx = rs.getInt(0, "fld1cd");
			}
			if("".equalsIgnoreCase(upCcfId)){
				return ExceptionHelper.toJSONString(-1, "맨 위에 있습니다. 처리할 수 없습니다.");
			}
			//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "upCcfId=" + upCcfId + ", upOrderIdx=" + upOrderIdx);
			// 출력순서를 변경한다.
			String sql1 = "";
			if(interfaceTableYn){
				sql1 = "update consent_form_mast set disp_order=? where ccf_id=?";
			}else{
				sql1 = "update ta88 set fld1cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
			}
			para1.put(1, upOrderIdx);
			para1.put(2, ccfId);
			//
			String sql2 = "";
			if(interfaceTableYn){
				sql2 = "update consent_form_mast set disp_order=? where ccf_id=?";
			}else{
				sql2 = "update ta88 set fld1cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
			}
			para2.put(1, orderIdx);
			para2.put(2, upCcfId);
			//
			String updString = "";
			updString = sqlHelper.executeUpdate(sql1, para1, sql2, para2);
			return updString;
		}catch(Exception ex){
			return ExceptionHelper.toJSONString(ex);
		}finally{
			;
		}

	}

}
