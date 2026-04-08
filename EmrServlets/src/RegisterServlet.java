import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RegisterServlet extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String mode = request.getParameter("mode");
		
		String resultString="";
		
		if (mode.equals("check")) {
			resultString = checkWifiMacAddress(request);
		}
		else if (mode.equals("register")) {
			String check = checkLicenseKeyNo(request);
			if (check.equals("yes")) {
				resultString = registerDevice(request);
			}
			else if (check.equals("no")) {
				resultString = "사용할 수 없는 인증키값입니다.";
			}
			else {
				resultString = check;
			}
		}
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(resultString);
		out.close();
	}
	// check wifi mac address
	private String checkWifiMacAddress(HttpServletRequest request) {
		String paraKeys[] = new String[] {"wifimacaddress"};
		BCGet instance = BCCheckWifiMacAddress.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "checkWifiMacAddress", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}

	}
	// 등록
	private String registerDevice(HttpServletRequest request) {
		String paraKeys[] = new String[] {"wifimacaddress","licensekeyno"};
		BCPut instance = BCPutRegisterDevice.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "registerDevice", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}

	}
	// 사용가능한 인증키인지 점검
	private String checkLicenseKeyNo(HttpServletRequest request) {
		String paraKeys[] = new String[] {"licensekeyno"};
		BCGet instance = BCCheckLicenseKeyNo.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "checkLicenseKeyNo", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}

	}

}
