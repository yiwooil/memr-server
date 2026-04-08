
import java.io.*;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.*;

public class LoginServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		String resultString = getUserYN(request);
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();		
		out.print(resultString);
		out.close();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	private String getUserYN(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getUserYN", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","password","ver"};
		MFGet instance = MFGetUserYN.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getUserYN", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getUserYN", "종료");
		}
		
	}
}
