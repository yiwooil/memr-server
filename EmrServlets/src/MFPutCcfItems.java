import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MFPutCcfItems implements MFPut {
	private static MFPutCcfItems mInstance=null;
	private MFPutCcfItems(){
		
	}
	
	public static MFPutCcfItems getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCcfItems();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String ccfId = (String)param.get("ccfid");
		String ccfItems = (String)param.get("ccfitems");
		
		
		SqlHelper sqlHelper = new SqlHelper(hospitalId);
		
		// 기존내역삭제
		HashMap<Integer, Object>para = new HashMap<Integer, Object>();
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		//if(interfaceTableYn){
		//	sql = "delete from consent_form_value_mast where ccf_id=? ";
		//}else{
			sql = "delete from ta88a where mst1cd='EMR' and mst2cd='FORM' and mst3cd=? ";
		//}
		para.put(1, ccfId);
		
		List<HashMap<Integer, Object>>paraList2 = new ArrayList<HashMap<Integer, Object>>();
		String sql2 = "";
		//if(interfaceTableYn){
		//	sql2 = "insert into consent_form_value_mast(ccf_id,seq_no,ccf_field,ccf_x,ccf_y,ccf_w,ccf_h) values(?,?,?,?,?,?,?)";
		//}else{
			sql2 = "insert into ta88a(mst1cd,mst2cd,mst3cd,mst4cd,cdnm,fld1qty,fld2qty,fld3qty,fld4qty,fld1cd,fld2cd) values('EMR','FORM',?,?,?,?,?,?,?,?,?)";
		//}
		
		ccfItems += ":"; // 오류방지용
		String[] itemArray = ccfItems.split(":");
		int itemArrayCount = itemArray.length;
		for(int i=0;i<itemArrayCount;i++){
			String[] valueArray = (itemArray[i]+",end").split(",");
			String seqNo = Integer.toString(i+1);
			String ccfField = valueArray[0];
			String ccfX = valueArray[1];
			String ccfY = valueArray[2];
			String ccfW = valueArray[3];
			String ccfH = valueArray[4];
			String ccfAutoFit = valueArray[5];
			String ccfTypeName = valueArray[6];
			
			HashMap<Integer, Object>para2 = new HashMap<Integer, Object>();
			para2.put(1, ccfId);
			para2.put(2, seqNo);
			para2.put(3, ccfField);
			para2.put(4, ccfX);
			para2.put(5, ccfY);
			para2.put(6, ccfW);
			para2.put(7, ccfH);
			para2.put(8, ccfAutoFit);
			para2.put(9, ccfTypeName);
			
			paraList2.add(para2);
		}
		
		String retString = sqlHelper.executeUpdate(sql, para, sql2, paraList2);
		
		return retString;
	}
}
