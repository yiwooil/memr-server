import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ResetHospitalInformationServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doPost(request,response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String ret = resetHospitalInformation(request);
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(ret);
		out.close();
	}
	
	// 혹시 병원정보가 변경되면 SqlHelper에 있는 정보를 변경시키는 서블릿임.
	private String resetHospitalInformation(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid"};
		BCGet instance = BCGetHospitalInformation.getInstance();
		try {
			HashMap<String,Object>param = Utility.getParaMap(paraKeys, request);
			param.put("reset","Y"); // 재조회모드
			return instance.getData(param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "resetHospitalInformation", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
}
