
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.simple.*;

public class TprServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String mode = request.getParameter("mode");
		
		if(mode==null) mode="0"; // 기본 
		
		String resultString = "";
		if(mode.equals("0")){
			// 조회
			resultString = getTpr(request);
		}else if(mode.equals("1")){
			// 저장
			resultString = saveTpr(request);
		}else if(mode.equals("2")){
			// 저장
			resultString = getTprOneRow(request);
		}else if(mode.equals("dmq")){
			// 2019.07.29 WOOIL - DM 조회기능 추가
			resultString = getDm(request);
		}else if(mode.equals("dmq1")){
			// 2019.08.06 WOOIL - DM 조회기능 추가
			resultString = getDmOneRow(request);
		}else if(mode.equals("dms")){
			// 2019.07.31 WOOIL - DM 저장기능 추가
			resultString = saveDm(request);
		}else if(mode.equals("ioq")){
			// 2019.07.29 WOOIL - DM 조회기능 추가
			resultString = getIo(request);
		}else if(mode.equals("ioq1")){
			// 2019.08.08 WOOIL - IO 조회기능 추가
			resultString = getIoOneRow(request);
		}else if(mode.equals("ios")){
			// 2019.08.08 WOOIL - IO 저장기능 추가
			resultString = saveIo(request);
		}
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	private String getTpr(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt"};
		MFGet instance = MFGetTprList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTpr", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String getTprOneRow(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","chkdate","chktime"};
		MFGet instance = MFGetTprOneRow.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTprOneRow", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String saveTpr(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","chkdate","chktime","bp","bpmax","bpmin","tmp","tmpcase","pr","rr","userid"};
		MFPut instance = MFPutTpr.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveTpr", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String getDm(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt"};
		MFGet instance = MFGetDmList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getDm", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String getDmOneRow(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","chkdate","chktime"};
		MFGet instance = MFGetDmOneRow.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTprOneRow", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String saveDm(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","chkdate","chktime","nvalue","userid"};
		MFPut instance = MFPutDm.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveDm", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String getIo(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt"};
		MFGet instance = MFGetIoList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getIo", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String getIoOneRow(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","chkdate","chktime"};
		MFGet instance = MFGetIoOneRow.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTprOneRow", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String saveIo(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","chkdate","chktime"
				,"oralc","oralv","patec","patev","bloodc","bloodv","urine","drsu","svoc","svov","stool","vomit","others"
				,"userid"};
		MFPut instance = MFPutIo.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveDm", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
}
