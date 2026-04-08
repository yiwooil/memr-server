import java.util.HashMap;

import org.json.JSONArray;

public class MFPutCvrConfirm implements MFPut {
	private static MFPutCvrConfirm mInstance=null;
	private MFPutCvrConfirm(){
		
	}
	
	public static MFPutCvrConfirm getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCvrConfirm();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userid = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String bdiv = (String)param.get("bdiv");
		String odt = (String)param.get("odt");
		String ono = (String)param.get("ono");
		
		if(userid==null) userid="";
		//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "userid=" + userid + ",pid=" + pid + ",bededt=" + bededt + ",bdiv="+bdiv+",odt="+odt+",ono="+ono);
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString="OK";
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			String sql =
					"update ta999_cvr" +
			        "   set cnfempid=?" +
					"     , cnfdt=convert(varchar,getdate(),112)" +
			        "     , cnftm=replace(convert(varchar,getdate(),24),':','')" +
					" where pid=?";
			if(!"1".equalsIgnoreCase(bdiv)){
				sql +=
				    "   and bededt=?";				
			}
			sql +=
					"   and bdiv=?" +
			        "   and odt=?" +
					"   and ono=?" +
					"";
			int idx=0;
			para.put(++idx, userid);
			para.put(++idx, pid);
			if(!"1".equalsIgnoreCase(bdiv)){
				para.put(++idx, bededt);
			}
			para.put(++idx, bdiv);
			para.put(++idx, odt);
			para.put(++idx, ono);

			String insResult=sqlHelper.executeUpdate(sql,para);
			
			JSONArray main = new JSONArray(insResult);
			JSONArray control = main.getJSONArray(0);
			int returnCode = control.getJSONObject(0).getInt("return_code");
			String returnDesc = control.getJSONObject(0).getString("return_desc");
			
			if("ok".equalsIgnoreCase(returnDesc)){
				returnString="";
				returnString+="<font size='28pt'>";
				returnString+="<br>";
				returnString+="<br>";
				returnString+="<br>";
				returnString+="<br>";
				returnString+="<br>";
				returnString+="<center>";
				returnString+="결과를 확인하였습니다(" + returnCode + "건).";
				returnString+="</center>";
				returnString+="</font>";
			}else{
				returnString=returnDesc;
			}
			
		}
		catch (Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "Exception", e.getLocalizedMessage() + " [userid=" + userid + ",pid=" + pid + ",bededt=" + bededt + ",bdiv="+bdiv+",odt="+odt+",ono="+ono+"]");
			returnString=e.getMessage();
		}
		return returnString;
	}

}
