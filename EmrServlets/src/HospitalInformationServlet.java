import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class HospitalInformationServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String hospitalId = request.getParameter("hospitalid");
		String mode = request.getParameter("mode");
		if (mode == null) mode = "0";
		String resultString = "";
		if("0".equalsIgnoreCase(mode)){
			// basecamp에 등록되어있는 병원정보를 반화한다.
			resultString = getBCHospitalInformation(request);
		}else if("1".equalsIgnoreCase(mode)){
			// 각 병원 서버에 등록되어있는 병원정보에서 병원리스트를 반환한다.
			resultString = getMFHospitalList(request);
		}else if("2".equalsIgnoreCase(mode)){
			// 각 병원 서버에 등록되어있는 병원정보를 반환한다.
			resultString = getMFHospitalInformation(request);
		}
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	private String getBCHospitalInformation(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getBCHospitalInformation", "시작");
		String paraKeys[] = new String[] {"hospitalid"};
		BCGet instance = BCGetHospitalInformation.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getBCHospitalInformation", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getBCHospitalInformation", "종료");
		}
	}
	
	private String getMFHospitalList(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getHospitalList", "시작");
		String paraKeys[] = new String[] {"hospitalid"}; // 그냥
		MFGet instance = MFGetHospitalList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getHospitalList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getHospitalList", "종료");
		}
	}
	
	private String getMFHospitalInformation(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getMFHospitalInformation", "시작");
		String paraKeys[] = new String[] {"hospitalid"};
		MFGet instance = MFGetHospitalInformation.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getMFHospitalInformation", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getMFHospitalInformation", "종료");
		}
	}
}
