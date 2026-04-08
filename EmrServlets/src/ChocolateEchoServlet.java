import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ChocolateEchoServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String hospitalId = request.getParameter("hospitalid");
		String echo = request.getParameter("echo");
		String returnString = getEchoString(hospitalId, echo);
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(returnString);
		out.close();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	private String getEchoString(String hospitalId, String echo) {
		return "";
		/*
		SqlHelper sqlHelper=null;
		String retString="";
		try {
			sqlHelper = new SqlHelper(hospitalId);
			retString = sqlHelper.getEchoString(echo);
		} catch (SQLException e) {
			retString = e.getMessage();
		}catch(Exception e){
			retString = e.getMessage();
		}
		return retString;
		*/
	}

}
