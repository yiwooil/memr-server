import java.sql.SQLException;
import java.util.HashMap;

public class MFGetOpHx implements MFGet {
	private static MFGetOpHx mInstance=null;
	private MFGetOpHx(){
		
	}
	
	public static MFGetOpHx getInstance(){
		if(mInstance==null){
			mInstance = new MFGetOpHx();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String bdiv = (String)param.get("bdiv");
		
		String returnString=null;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();

		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
		    sql += "select top 10 u01.opdt rsvopdt,u01.rsvop,u01.dacd rsvdacd";
		    sql += "     , (select a09.dptnm from ta09 a09 where a09.dptcd=u01.dptcd) as rsvopdptnm";
		    sql += "     , (select a07.drnm from ta07 a07 where a07.drid=u01.drid) as rsvopdrnm";
		    sql += "     , substring(opdt,1,4)+'³â'+substring(opdt,5,2)+'¿ù'+substring(opdt,7,2)+'ÀÏ' as rsvopdt_ymd";
		    sql += "     , u01.opdt, u01.dptcd, u01.opseq, u01.seq";
		    sql += "  from tu01 u01";
		    sql += " where u01.pid=?";
		    sql += "   and isnull(u01.chgdt,'')=''";
		    sql += " order by u01.opdt desc";
		    
		    para.put(1, pid);
		    
			String rsString = sqlHelper.executeQuery(sql, para, null);
			returnString = rsString;
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
