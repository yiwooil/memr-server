import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MFPutDelEmrScan implements MFPut {
	private static MFPutDelEmrScan mInstance=null;
	private MFPutDelEmrScan(){
		
	}
	
	public static MFPutDelEmrScan getInstance(){
		if(mInstance==null){
			mInstance = new MFPutDelEmrScan();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userid = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String bdiv = (String)param.get("bdiv");
		String exdt = (String)param.get("exdt");
		String seq = (String)param.get("seq");
		String rptcd = (String)param.get("rptcd");
		String subPageList = (String)param.get("sub_page_list");
		
		SqlHelper sqlHelper = new SqlHelper(hospitalId);
		
		String returnString="";
		
		try{
			new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "pid="+pid+", bdiv="+bdiv+",exdt="+exdt+",seq="+seq+",rptcd="+rptcd+",subPageList="+subPageList);
			
			HashMap<Integer, Object>para = new HashMap<Integer,Object>();
			String sql = "update tg02" +
			             "   set deldt=convert(varchar,getdate(),112), deltm=substring(replace(convert(varchar,getdate(),114),':',''),1,6), delid=?" +
			             " where pid=? and bdiv=? and exdt=? and rptcd=? and (seq=?";
			if(!"".equals(subPageList)){
				// 여러 페이지로 이루어진 동의서이면
				String pageList[] = subPageList.split(";");
				for(int i=0;i<pageList.length;i++){
					sql += "        or seq=?";
				}
			}
			sql += "       )";
			int idx=0;
			para.put(++idx, userid);
			para.put(++idx, pid);
			para.put(++idx, bdiv);
			para.put(++idx, exdt);
			para.put(++idx, rptcd);
			para.put(++idx, seq);
			if(!"".equals(subPageList)){
				// 여러 페이지로 이루어진 동의서이면
				String pageList[] = subPageList.split(";");
				for(int i=0;i<pageList.length;i++){
					para.put(++idx, pageList[i]);
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
