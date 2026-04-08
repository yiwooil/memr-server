import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SqlTestServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		String resultString = getSqlTest(request);
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();		
		out.print(resultString);
		out.close();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	private String getSqlTest(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","sql"};
		MFGet instance = MFGetSqlTest.getInstance();
		try {
			String returnString = instance.getData(Utility.getParaMap(paraKeys, request));
			String returnType=request.getParameter("returntype");
			if(returnType==null) returnType="";
			if("htmltable".equalsIgnoreCase(returnType)) returnString = Utility.jsonToHtmlTable(returnString);
			return returnString;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSqlTest", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
		
	}

}
