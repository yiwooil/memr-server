import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MFPutCertificateGroup implements MFPut {
	private static MFPutCertificateGroup mInstance=null;
	private MFPutCertificateGroup(){
		
	}
	
	public static MFPutCertificateGroup getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificateGroup();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String ccfId = (String)param.get("ccfid");
		String ccfGroup = (String)param.get("ccf_group");
		String hxType = (String)param.get("hx_type"); // 2024.09.04 WOOIL
		
		List<String> sqlList = new ArrayList<String>();
		List<HashMap<Integer, Object>> paraList = new ArrayList<HashMap<Integer, Object>>(); 
		
		SqlHelper sqlHelper = null;
		try{
			sqlHelper = new SqlHelper(hospitalId);
			
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			//HashMap<Integer, Object>para=new HashMap<Integer,Object>();
			//String sql = "";
			//if(interfaceTableYn){
			//	sql = "update consent_form_mast set ccf_group=? where ccf_id=?";
			//}else{
			//	sql = "update ta88 set fld2cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
			//}
			//para.put(1, ccfGroup);
			//para.put(2, ccfId);
			//
			//String rsString = sqlHelper.executeUpdate(sql, para);
			//return rsString;
			
			if(ccfGroup==null){}
			else{
				HashMap<Integer, Object>para=new HashMap<Integer,Object>();
				String sql = "update ta88 set fld2cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
				para.put(1, ccfGroup);
				para.put(2, ccfId);
				sqlList.add(sql);
				paraList.add(para);
			}
			
			// 2024.09.06 WOOIL - 클라이언트 모듈과 서버 모듈이 update되는 시점이 맞지 않아 null이 넘어오는 경우를 대비함.
			if(hxType==null){}
			else{
				HashMap<Integer, Object>para=new HashMap<Integer,Object>();
				String sql = "update ta88 set fld6cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
				para.put(1, hxType);
				para.put(2, ccfId);
				sqlList.add(sql);
				paraList.add(para);
			}
			String rsString = sqlHelper.executeUpdate(sqlList, paraList);
			return rsString;
		}catch(Exception ex){
			return ExceptionHelper.toJSONString(ex);
		}
	}
}
