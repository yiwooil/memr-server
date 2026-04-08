import java.io.IOException;
import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;


public class ApkVersionServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String returnString = getVersionName(request);
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(returnString);
		out.close();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	
	public String getVersionName(HttpServletRequest request) {
		String paraKeys[] = new String[] {"apkname"};
		BCGet instance = BCGetApkVersionName.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getVersionName", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
}

