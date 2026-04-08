import java.util.HashMap;


public class MFGetNoticeDetail  implements MFGet {
	private static MFGetNoticeDetail mInstance=null;
	private MFGetNoticeDetail(){
		
	}
	
	public static MFGetNoticeDetail getInstance(){
		if(mInstance==null){
			mInstance = new MFGetNoticeDetail();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String apdt = (String)param.get("apdt");
		String seq = (String)param.get("seq");
		
		HashMap<Integer, Object>para = new HashMap<Integer, Object>();
		HashMap<Integer, String>paraType = new HashMap<Integer, String>();
		
		StringBuilder returnString = new StringBuilder();
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql="";
			if(interfaceTableYn){
				sql = getSqlInter();
			}else{
				sql = getSql();
			}
			para.put(1, apdt); paraType.put(1, "D");
			para.put(2, seq); paraType.put(1, "N");
			String rsString = sqlHelper.executeQuery(sql,para,paraType);
			rs = new ResultSetHelper(rsString);
			
			if (rs.getReturnCode()<0) {
				returnString.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
				returnString.append("<html>");
				returnString.append("<head>");
				returnString.append("<title>공지사항</title>");
				returnString.append("</head>");
				returnString.append("<body>");
				returnString.append(rs.getReturnDesc());
				returnString.append("</body>");
				returnString.append("</html>");
			}
			else if (rs.getRecordCount()==0) {
				returnString.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
				returnString.append("<html>");
				returnString.append("<head>");
				returnString.append("<title>공지사항</title>");
				returnString.append("</head>");
				returnString.append("<body>");
				returnString.append("내용이  없습니다.");
				returnString.append("</body>");
				returnString.append("</html>");
			}
			else {
				String comment=rs.getString(0,"comment");
				// 엔터문자처리
				comment = comment.replace("\n", "<br>");
				// 공백문자처리
				comment = comment.replace(" ", "&nbsp;");
				comment = comment.replace("%", "&#37;");
				
				returnString.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
				returnString.append("<html>");
				returnString.append("<head>");
				returnString.append("<title>공지사항</title>");
				returnString.append("</head>");
				returnString.append("<body>");
				returnString.append("<font bold=true>");
				returnString.append(rs.getString(0,"title"));
				returnString.append("</font>");
				returnString.append("<hr>");
				returnString.append("<font face=Droid Sans>");
				returnString.append(comment);
				returnString.append("</font>");
				returnString.append("</body>");
				returnString.append("</html>");
			}
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
			returnString.append("<html>");
			returnString.append("<head>");
			returnString.append("<title>공지사항</title>");
			returnString.append("</head>");
			returnString.append("<body>");
			returnString.append(ex.getMessage());
			returnString.append("</body>");
			returnString.append("</html>");
		}
		
		return returnString.toString();
	}
	
	private String getSql(){
		String sql = 
				"select *" +
			    "  from ta95 " +
			    " where apdt=? " +
			    "   and seq=? ";
		return sql;
	}
	
	private String getSqlInter(){
		String sql = 
				"select title, notice_comment as comment" +
			    "  from notice_hist " +
			    " where notice_date=? " +
			    "   and notice_no=? ";
		return sql;
	}

}
