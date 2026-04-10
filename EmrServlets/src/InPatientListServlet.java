
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONObject;
import org.json.simple.*;

public class InPatientListServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String mode = request.getParameter("mode");
		if (mode==null) mode="0";
		
		String resultString = "";
		if(mode.equals("0")) {
			// 재원
			resultString=getPatientList(request);
		}else if(mode.equals("1")) {
			// 환자명으로 검색
			resultString=getSearchInPatientList(request);
		}else if(mode.equals("2")) {
			// 입원이력
			resultString=getPatientHosHxIn(request);
		}else if(mode.equals("3")) {
			// 외래이력
			resultString=getPatientHosHxOut(request);
		}else if(mode.equals("4")) {
			// 응급이력
			resultString=getPatientHosHxEr(request);
		}else if(mode.equals("5")) {
			// 환자명으로 검색(입원외래구분없이)
			resultString=getSearchPatientList(request);
		}else if(mode.equals("outp")){
			// 외래 접수환자리스트
			resultString=getOutPatientList(request);
		}else if(mode.equals("6")) {
			// 환자ID로 현재 재원환자인지 검색하고 정보를 반환한다.
			resultString=getInPatientInfo(request);
		}else if(mode.equals("7")) {
			// 취소된 외래 접수내역인지 검사
			resultString=getOutPatientCancelCheck(request);
		}
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	private String getPatientList(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getPatientList", "시작");
		//String modeOld = request.getParameter("modeold");
		//if(modeOld==null) modeOld="";
		String paraKeys[] = new String[] {"hospitalid","userid","sortorder","ward","dept","pdrid","retver"/*,"pageno"*/};
		MFGet instance = null;
		//if("Y".equals(modeOld)){
			instance = MFGetInPatientList.getInstance();
		//}else{
		//	instance = MFGetInPatientList2.getInstance();
		//}
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPatientList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getPatientList", "종료");
		}
	}
	private String getSearchInPatientList(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getSearchInPatientList", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","sortorder","ward","dept","searchtext","exdt","exdtto","searchiofg"};
		MFGet instance = MFGetInPatientSearchList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSearchInPatientList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getSearchInPatientList", "종료");
		}
	}
	private String getPatientHosHxIn(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getPatientHosHxIn", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt"};
		MFGet instance = MFGetInPatientHx.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPatientHosHxIn", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getPatientHosHxIn", "종료");
		}
	}
	private String getPatientHosHxOut(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getPatientHosHxOut", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt"};
		MFGet instance = MFGetOutPatientHx.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPatientHosHxOut", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getPatientHosHxOut", "종료");
		}
	}
	private String getPatientHosHxEr(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getPatientHosHxEr", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt"};
		MFGet instance = MFGetErPatientHx.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPatientHosHxEr", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getPatientHosHxEr", "종료");
		}
	}
	private String getSearchPatientList(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getSearchPatientList", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","sortorder","ward","dept","searchtext"};
		MFGet instance = MFGetPatientSearchList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSearchPatientList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getSearchPatientList", "종료");
		}
	}
	private String getOutPatientList(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getOutPatientList", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","sortorder","exdt","dept","pdrid","sortorder","rsv_in_only"};
		MFGet instance = null;
		instance = MFGetOutPatientList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getOutPatientList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getOutPatientList", "종료");
		}
	}
	private String getInPatientInfo(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getInPatientInfo", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid"};
		MFGet instance = null;
		instance = MFGetInPatientInfo.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getInPatientInfo", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getInPatientInfo", "종료");
		}
	}
	private String getOutPatientCancelCheck(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getOutPatientCancelCheck", "시작");
		String paraKeys[] = new String[] {"hospitalid","pid","exdt","dptcd","hms"};
		MFGet instance = null;
		instance = MFGetOutPatientCancelCheck.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getOutPatientCancelCheck", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getOutPatientCancelCheck", "종료");
		}
	}
}
