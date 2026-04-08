import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MFPutCertificatePage implements MFPut {
	private static MFPutCertificatePage mInstance=null;
	private MFPutCertificatePage(){
		
	}
	
	public static MFPutCertificatePage getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificatePage();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String bfPage1Ccfid = (String)param.get("bf_page1_ccfid");
		String bfPage1SubPageList = (String)param.get("bf_page1_sub_page_list");
		String afPage1Ccfid = (String)param.get("af_page1_ccfid");
		String afPage1SubPageList = (String)param.get("af_page1_sub_page_list");
		String ccfGroup = (String)param.get("ccf_group");
		String dispCcfList = (String)param.get("disp_ccf_list");

		//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "bfPage1Ccfid="+bfPage1Ccfid+"");
		//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "bfPage1SubPageList="+bfPage1SubPageList+"");
		//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "afPage1Ccfid="+afPage1Ccfid+"");
		//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "afPage1SubPageList="+afPage1SubPageList+"");
		//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccfGroup="+ccfGroup+"");
		
		List<String> sqlList = new ArrayList<String>();
		List<HashMap<Integer, Object>> paraList = new ArrayList<HashMap<Integer, Object>>(); 

		SqlHelper sqlHelper;
		String returnString="";
		
		try{
			sqlHelper = new SqlHelper(hospitalId);
			
			// 이전 내역 취소하고
			if(!"".equalsIgnoreCase(bfPage1Ccfid)){
				String sql = "";
				sql += "update ta88 set fld2qty=?, fld3qty=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
				
				HashMap<Integer, Object>para=new HashMap<Integer,Object>();
				para.put(1, "");
				para.put(2, "");
				para.put(3, bfPage1Ccfid);
				
				sqlList.add(sql);
				paraList.add(para);
			}
			if(!"".equalsIgnoreCase(bfPage1SubPageList)){
				String []aryBfPage1SubPageList = bfPage1SubPageList.split(";");
				for(int i=0; i<aryBfPage1SubPageList.length; i++){
					String sql = "";
					sql += "update ta88 set fld2qty=?, fld3qty=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
					
					HashMap<Integer, Object>para=new HashMap<Integer,Object>();
					para.put(1, "");
					para.put(2, "");
					para.put(3, aryBfPage1SubPageList[i]);
					
					sqlList.add(sql);
					paraList.add(para);
				}
			}
			
			// 새로운 내역으로 갱신
			if(!"".equalsIgnoreCase(afPage1Ccfid)){
				String sql = "";
				sql += "update ta88 set fld2qty=?, fld3qty=?, fld2cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
				
				HashMap<Integer, Object>para=new HashMap<Integer,Object>();
				para.put(1, afPage1SubPageList);
				para.put(2, "");
				para.put(3, ccfGroup);
				para.put(4, afPage1Ccfid);
				
				sqlList.add(sql);
				paraList.add(para);
			}
			if(!"".equalsIgnoreCase(afPage1SubPageList)){
				String []aryAfPage1SubPageList = afPage1SubPageList.split(";");
				for(int i=0; i<aryAfPage1SubPageList.length; i++){
					String sql = "";
					sql += "update ta88 set fld2qty=?, fld3qty=?, fld2cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
					
					HashMap<Integer, Object>para=new HashMap<Integer,Object>();
					para.put(1, "");
					para.put(2, (i+2) + "Y");
					para.put(3, ccfGroup);
					para.put(4, aryAfPage1SubPageList[i]);
					
					sqlList.add(sql);
					paraList.add(para);
				}
			}
			
			// 출력 순저 재 지정
			if(!"".equalsIgnoreCase(dispCcfList)){
				String[] aryDispCcfList = dispCcfList.split(",");
				for(int row_no=0; row_no<aryDispCcfList.length; row_no++){
					String sql = "";
					sql += "update ta88 set fld1cd=? where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
					
					HashMap<Integer, Object>para=new HashMap<Integer,Object>();
					para.put(1, row_no+1);
					para.put(2, aryDispCcfList[row_no]);
	
					sqlList.add(sql);
					paraList.add(para);
				}
			}
			
			returnString = sqlHelper.executeUpdate(sqlList, paraList);
			
		
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;		
	}

}
