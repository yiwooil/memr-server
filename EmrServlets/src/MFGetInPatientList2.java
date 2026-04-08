import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetInPatientList2 implements MFGet {
	private static MFGetInPatientList2 mInstance=null;
	private MFGetInPatientList2(){
		
	}
	
	public static MFGetInPatientList2 getInstance(){
		if(mInstance==null){
			mInstance = new MFGetInPatientList2();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("usrid");
		String sortOrder = (String)param.get("sortorder");
		String ward = (String)param.get("ward");
		String dept = (String)param.get("dept");
		String pdrid = (String)param.get("pdrid");
		
		if(pdrid==null) pdrid=""; // 오류방지용

		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		int rowCount=0;
		
		Integer queryMode = D_TA04_list.PNM;
		if("1".equals(sortOrder)) queryMode = D_TA04_list.PNM;
		else if("2".equals(sortOrder)) queryMode = D_TA04_list.WARD;
		else if("3".equals(sortOrder)) queryMode = D_TA04_list.DPTCD;
		else queryMode = D_TA04_list.PNM;
		
		D_TA04_list ta04List = D_TA04_list.getInstance();
		Map<String,D_TA04> map = ta04List.get(hospitalId, queryMode);
		if(map==null){
			columns = new JSONObject();
			columns.put("return_code",-1);
			columns.put("return_desc","시스템 초기화 중입니다. 잠시 후 다시 시도하십시오.");
			status.add(columns);
			
		}else{
			for(String key : map.keySet()){
				D_TA04 ta04 = map.get(key);
				if(!"".equals(ward) && !ward.equalsIgnoreCase(ta04.wardid)) continue;
				if(!"".equals(dept) && !dept.equalsIgnoreCase(ta04.dptcd)) continue;
				if(!"".equals(pdrid) && !pdrid.equalsIgnoreCase(ta04.pdrid)) continue;
				
				rowCount++;
				columns = new JSONObject();
				columns.put("pid", ta04.pid);
				columns.put("bededt", ta04.bededt);
				columns.put("pnm", ta04.pnm);
				columns.put("psex", ta04.psex);
				columns.put("dptcd", ta04.dptcd);
				columns.put("bedodt", ta04.bedodt);
				columns.put("ward", ta04.ward);
				//columns.put("wardid", ta04.wardid);
				//columns.put("rmid", ta04.rmid);
				//columns.put("bedid", ta04.bedid);
				//columns.put("pdrid", ta04.pdrid);
				columns.put("pdrnm", ta04.pdrnm);
				columns.put("qfycd", ta04.qfycd);
				columns.put("qfycdnm", ta04.qfycdnm);
				columns.put("dxd", ta04.dxd);
				columns.put("bthdt", ta04.bthdt);
				columns.put("age", ta04.age);
				columns.put("turnno", ta04.turnno);
				
				rowData.add(columns);
				
			}
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",rowCount);
			columns.put("return_desc","ok");
			status.add(columns);
		}
		// 반환자료
		result.add(status);
		result.add(rowData);
		
		return result.toJSONString();
	}
}
