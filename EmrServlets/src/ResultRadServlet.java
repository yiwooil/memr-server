import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ResultRadServlet extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String mode = request.getParameter("mode");
		if (mode == null) mode = "0";
		
		String resultString = "";
		if ("0".equals(mode)) {
			resultString = getRadResultText(request);
		}else if ("1".equals(mode)) {
			resultString = getSpeResultText(request);
		}
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
		
	}
	
	private String getRadResultText(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","odt","ono","bdiv"};
		MFGet instance = MFGetRadResultText.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getRadResultText", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}

	private String getSpeResultText(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","odt","ono","bdiv"};
		MFGet instance = MFGetSpeResultText.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSpeResultText", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
}
