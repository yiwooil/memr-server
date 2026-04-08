import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


public class InPatientInformationServlet extends HttpServlet  {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String bdiv = request.getParameter("bdiv");
		
		if(bdiv==null) bdiv="2"; // 기본 입원
		
		String resultString = "";
		if(bdiv.equals("2")) {
			resultString = getInPatientInformationDisplayString(request);
		}else if(bdiv.equals("1")||bdiv.equals("3")) {
			resultString = getOutPatientInformationDisplayString(request);
		}
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	private String getInPatientInformationDisplayString(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt"};
		MFGet instance = MFGetInPatientInfoString.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getInPatientInformationDisplayString", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getOutPatientInformationDisplayString(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt"};
		MFGet instance = MFGetOutPatientInfoString.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getOutPatientInformationDisplayString", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
}
