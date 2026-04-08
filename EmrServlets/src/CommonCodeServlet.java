import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CommonCodeServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String mode = request.getParameter("mode");
		
		
		String resultString = "";
		if(mode.equals("0")) {
			// 병동리스트
			resultString=getWardList(request);
		} else if (mode.equals("1")) {
			// 진료과리스트
			resultString=getDeptList(request);
		} else if (mode.equals("2")) {
			// 의사리스트
			resultString=getDoctList(request);
		} else if (mode.equals("3")) {
			// scan class 리스트
			resultString=getScanClassList(request);
		} else if (mode.equals("4")) {
			resultString=getStructedText(request);
		}
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();		
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	private String getWardList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid"};
		MFGet instance = MFGetWardList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getWardList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}	
	}
	
	private String getDeptList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid"};
		MFGet instance = MFGetDeptList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getDeptList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}	
	}
	
	private String getDoctList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","dptcd"};
		MFGet instance = MFGetDoctList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getDoctList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}	
	}
	
	private String getScanClassList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid"};
		MFGet instance = MFGetScanClassList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getScanClassList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}	
	}

	private String getStructedText(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt","bdiv","wdate","wtime","currenttext"};
		MFGet instance = MFGetStructedText.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getStructedText", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}	
	}
}
