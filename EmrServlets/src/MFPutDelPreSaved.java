import java.util.HashMap;

public class MFPutDelPreSaved implements MFPut {
	private static MFPutDelPreSaved mInstance=null;
	private MFPutDelPreSaved(){
		
	}
	
	public static MFPutDelPreSaved getInstance(){
		if(mInstance==null){
			mInstance = new MFPutDelPreSaved();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userid = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String preSavedBdiv = (String)param.get("pre_saved_bdiv");
		String exdt = (String)param.get("exdt");
		String seq = (String)param.get("seq");
		String subPageList = (String)param.get("sub_page_list");
		
		SqlHelper sqlHelper = new SqlHelper(hospitalId);
		
		String returnString="";
		
		try{
			new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "pid="+pid+", preSavedBdiv="+preSavedBdiv+",exdt="+exdt+",seq="+seq+",subPageList="+subPageList);
			
			HashMap<Integer, Object>para = new HashMap<Integer,Object>();
			String sql = "update tg02t" +
			             "   set delfg=convert(varchar,getdate(),112) + ' ' + substring(replace(convert(varchar,getdate(),114),':',''),1,6) + ' ' + ?" +
			             " where pid=?" +
			             "   and bdiv=?" +
			             "   and exdt=?" +
			             "   and (seq=?";
			if(!"".equals(subPageList)){
				// 여러 페이지로 이루어진 동의서이면
				String pageList[] = subPageList.split(";");
				for(int i=0;i<pageList.length;i++){
					sql += "        or seq=?";
				}
			}
			sql += "       )";
			
			para.put(1, userid);
			para.put(2, pid);
			para.put(3, preSavedBdiv);
			para.put(4, exdt);
			para.put(5, seq);
			if(!"".equals(subPageList)){
				// 여러 페이지로 이루어진 동의서이면
				String pageList[] = subPageList.split(";");
				for(int i=0;i<pageList.length;i++){
					para.put(6+i, pageList[i]);
				}
			}
			
			String retString = sqlHelper.executeUpdate(sql, para);

			new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "retString="+retString);
			
			returnString = "y"; // 성공
			
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;			
	}

}
