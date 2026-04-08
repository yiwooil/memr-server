import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class NoticeServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String mode = request.getParameter("mode");
		
		String resultString="";

		if (mode.equals("0")) {
			// 공지사항 전체리스트
			resultString = getNoticeList(request);
		}
		else if (mode.equals("1")) {
			// 공지사항 내용
			resultString = getNoticeDetail(request);
		}
		else if (mode.equals("2")) {
			// 새로운 공지사항이 있는지 없는지?
			resultString = getNoticeYN(request);
		}
		else if (mode.equals("3")) {
			// 공지사항 전체리스트. 리스트 용
			resultString = getNoticeList2(request);
		}
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	private String getNoticeList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid"};
		MFGet instance = MFGetNoticeListHtml.getInstance();
		try {
			HashMap<String,Object>param = Utility.getParaMap(paraKeys, request);
			param.put("servername", request.getServerName()); // 서버ip
			param.put("serverport", request.getServerPort()); // 서버port
			return instance.getData(param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getNoticeList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getNoticeList2(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid"};
		MFGet instance = MFGetNoticeList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getNoticeList2", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getNoticeDetail(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","apdt","seq"};
		MFGet instance = MFGetNoticeDetail.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getNoticeDetail", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
		
	}
	private String getNoticeYN(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","lastapdt","lastseq"};
		MFGet instance = MFGetNoticeNewYN.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getNoticeYN", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}

	}
}
