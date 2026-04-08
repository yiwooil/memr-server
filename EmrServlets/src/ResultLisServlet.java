import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ResultLisServlet  extends HttpServlet {
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String mode = request.getParameter("mode");
		if(mode==null) mode="";
		
		String resultString="";
		if("1".equals(mode)){
			// 접수내역을 먼저 읽는다.
			resultString = getLisAccept(request);
		}else{
			resultString = query(request);
		}
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
	}

	private String query(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","frdt","todt","spcno"};
		MFGet instance = MFGetLisResult.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "query", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getLisAccept(HttpServletRequest request){
		String paraKeys[] = new String[] {"hospitalid","pid","frdt","todt"};
		MFGet instance = MFGetLisAccept.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getLisAccept", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}

}
