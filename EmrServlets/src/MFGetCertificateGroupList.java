import java.sql.SQLException;
import java.util.HashMap;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetCertificateGroupList implements MFGet {
	private static MFGetCertificateGroupList mInstance=null;
	private MFGetCertificateGroupList(){
		
	}
	
	public static MFGetCertificateGroupList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCertificateGroupList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String returnString=null;

		SqlHelper sqlHelper;
		try {
			JSONArray result = new JSONArray();
			JSONArray status = new JSONArray();
			JSONArray rowData = new JSONArray();
			JSONObject columns = null;
			int rowCount=0;

			boolean etc_yn = false;
			
			TreeMap<String, String> mapGroup = new TreeMap<String, String>();
			mapGroup.clear();
			
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
			if(interfaceTableYn){
				sql = "select distinct isnull(ccf_group,'') as ccf_group from consent_form_mast order by ccf_group ";
			}else{
				sql = "select distinct isnull(fld2cd,'') as ccf_group from ta88 where mst1cd='EMR' and mst2cd='FORM' order by ccf_group ";
			}
			String rsString = sqlHelper.executeQuery(sql);
			ResultSetHelper rsGroup = new ResultSetHelper(rsString);
			int cnt = rsGroup.getRecordCount();
			for(int idx=0;idx<cnt;idx++){
				String ccfGroup = rsGroup.getString(idx, "ccf_group");
				if("".equalsIgnoreCase(ccfGroup)) ccfGroup = "기타"; // 빈값은 기본 "기타"
				String[] aGroup = ccfGroup.split(";"); // 여러 그룹에 포함될 수 있다(내과;외과)
				for(int i=0;i<aGroup.length;i++){
					if("기타".equalsIgnoreCase(aGroup[i])){
						etc_yn=true;
					}else if(mapGroup.containsKey(aGroup[i])==false){
						//rowCount++;
						//columns = new JSONObject();	
						//columns.put("ccf_group", aGroup[i]);
						//rowData.add(columns);						
						mapGroup.put(aGroup[i], "");
					}
				}
			}
			
			for(String ccfGroup : mapGroup.keySet()){
				rowCount++;
				columns = new JSONObject();	
				columns.put("ccf_group", ccfGroup);
				rowData.add(columns);						
			}
			
			// 2024.08.01 WOOIL - 기타를 맨 뒤로 오게 만든다.
			if(etc_yn==true){
				rowCount++;
				columns = new JSONObject();	
				columns.put("ccf_group", "기타");
				rowData.add(columns);						
			}
			
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",rowCount);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			result.add(rowData);
			
			returnString = result.toJSONString();
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
