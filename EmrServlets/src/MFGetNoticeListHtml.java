import java.util.HashMap;


public class MFGetNoticeListHtml implements MFGet {
	private static MFGetNoticeListHtml mInstance=null;
	private MFGetNoticeListHtml(){
		
	}
	
	public static MFGetNoticeListHtml getInstance(){
		if(mInstance==null){
			mInstance = new MFGetNoticeListHtml();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String serverName = (String)param.get("servername"); // 서버ip
		int serverPort = (Integer)param.get("serverport"); // 서버port
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		
		StringBuilder returnString = new StringBuilder();
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		
		try {

			sqlHelper = new SqlHelper(hospitalId);
			String sql = 
					"select *" +
				    "  from ta95 " +
				    " order by apdt desc, seq desc";
			String rsString = sqlHelper.executeQuery(sql);
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
				returnString.append("공지사항이  없습니다.");
				returnString.append("</body>");
				returnString.append("</html>");
			}
			else {
				returnString.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />");
				returnString.append("<html>");
				returnString.append("<head>");
				returnString.append("<title>공지사항</title>");
				returnString.append("</head>");
				returnString.append("<body>");
				returnString.append("<table>");
				for (int i=0;i<rs.getRecordCount();i++) {
					if (i>100) break; // 최대100건만
					String link = "http://" + serverName + ":" + serverPort + "/emrdroid/servlet/NoticeServlet?hospitalid=" + hospitalId + "&userid=" + userId + "&mode=1&apdt=" + rs.getString(i,"apdt") + "&seq=" +  rs.getString(i,"seq")+ "";
					returnString.append("<tr>");
					returnString.append("<td>");
					returnString.append(rs.getString(i,"apdt"));
					returnString.append("</td>");
					returnString.append("<td>");
					returnString.append("<a href=" + link + ">");
					returnString.append(rs.getString(i,"title"));
					returnString.append("</a>");
					returnString.append("</td>");
					returnString.append("</tr>");
				}
				returnString.append("</table>");
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

}
