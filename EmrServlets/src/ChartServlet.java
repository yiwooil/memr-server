import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ChartServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String mode = request.getParameter("mode");
		String bdiv = request.getParameter("bdiv");
		
		if (mode==null) mode="0";
		if (bdiv==null) bdiv="2";
		
		String resultString = "";
		if (mode.equals("0")&&bdiv.equals("1")){
			// 외래처방조회
			resultString = getOrderOut(request);
		}else if (mode.equals("0")&&bdiv.equals("2")){
			// 입원처방조회(입원당일응급+외래)
			resultString = getOrder(request);
		}else if (mode.equals("0")&&bdiv.equals("3")){
			// 응급처방조회
			resultString = getOrderEr(request);
		}else if (mode.equals("1")){
			// 스캔정보조회
			resultString = getEmrScan(request);
		}else if (mode.equals("2")){
			// 기록지조회
			resultString = getChart(request);
		}else if (mode.equals("3")){
			// 투약기록지조회. 최초일자 최종일자
			resultString = getMedRecordMinMaxDodt(request);
		}else if (mode.equals("4")){
			// 투약기록지조회
			resultString = getMedRecord(request);
		}else if (mode.equals("5")){
			// 동의서조회
			resultString = getSignedCertificatePaperList(request);
		}else if (mode.equals("6")){
			// 동의서 녹음파일 리스트
			resultString = getSignedCertificateMP4List(request);
		}else if (mode.equals("7")){
			// 동의서 촬영파일 리스트
			resultString = getSignedCertificatePicList(request);
		}else if (mode.equals("8")){
			// 검체번호로 환자ID 찾기
			resultString = getPidBySpcno(request);
		}else if (mode.equals("9")){
			// 혈액번호로 환자ID 찾기
			resultString = getPidByBldno(request);
		}else if (mode.equals("10")){
			// 환자 안전관리 점검 결과 저장
			resultString = putPatientSafeCheckResult(request);
		}else if (mode.equals("11")){
			// 외래 환자인 경우 최근 내원일 6일 전 일자를 반환
			resultString = getExdtLate6(request);
		}else if (mode.equals("12")){
			// 스캔이미지 삭제
			resultString = delEmrScan(request);
		}else if (mode.equals("13")){
			// 임시저장 이미지 삭제
			resultString = delPreSaved(request);
		}else if (mode.equals("14")){
			// CVR문자 확인 여부 저장
			resultString = putCvrConfirm(request);
		}else if (mode.equals("15")){
			// Labor Redord 반환
			resultString = getLaborRecord(request);
		}else if (mode.equals("16")){
			// 간호기록지 반환
			resultString = getNrChart(request);
		}else if (mode.equals("17")){
			// 간호기록지 저장
			resultString = putNrChart(request);
		}
		
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(resultString);
		out.close();
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
	private String getOrder(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","odivcd","frdt","todt","radorderyn"};
		MFGet instance = MFGetInPatientOrder.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getOrder", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String getOrderOut(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","odivcd","radorderyn","frdt","todt"};
		MFGet instance = MFGetOutPatientOrder.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getOrderOut", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	private String getOrderEr(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","odivcd","radorderyn"};
		MFGet instance = MFGetErPatientOrder.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getOrderEr", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getEmrScan(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","frdt","todt"};
		MFGet instance = MFGetEmrScanList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getEmrScan", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getSignedCertificatePaperList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","frdt","todt"};
		MFGet instance = MFGetSignedCertificatePaperList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSignedCertificatePaperList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getSignedCertificateMP4List(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","exdt","seq"};
		MFGet instance = MFGetSignedCertificateMP4List.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSignedCertificateMP4List", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getSignedCertificatePicList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","exdt","seq"};
		MFGet instance = MFGetSignedCertificatePicList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSignedCertificatePicList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	/***
	 * getChart : 차트의 내용을 조회한다.
	 * @param hospitalId
	 * @param pid
	 * @param bededt
	 * @param frdt
	 * @param todt
	 * @return : exdt,c_case,rmk1,bdiv
	 */
	private String getChart(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","frdt","todt","bdiv"};
		MFGet instance = MFGetChartList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getChart", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	/***
	 * getMedRecord : 투약기록지를 조회한다.
	 * @param hospitalId
	 * @param pid
	 * @param bededt
	 * @param frdt
	 * @param todt
	 * @return : ocd,onm,unit,dodt,odrcnt
	 */
	private String getMedRecord(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","frdt","todt"};
		MFGet instance = MFGetMedRecord.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getMedRecord", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	/***
	 * getMedRecordMinMaxDodt : 투약기록지가 작성된 최소와 최대일자를 구한다.
	 * @param hospitalId
	 * @param pid
	 * @param bededt
	 * @param frdt
	 * @param todt
	 * @return : mindodt, maxdodt
	 */
	private String getMedRecordMinMaxDodt(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","frdt","todt"};
		MFGet instance = MFGetMedRecordMinMaxDate.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getMedRecordMinMaxDodt", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}	
	
	private String getPidBySpcno(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","spcno"};
		MFGet instance = MFGetPidBySpcno.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPidBySpcno", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}	
	
	private String getPidByBldno(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","bldno"};
		MFGet instance = MFGetPidByBldno.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPidBySpcno", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}	

	private String putPatientSafeCheckResult(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","chktype","chkdata","chkresult"};
		MFPut instance = MFPutPatientSafeCheckResult.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "putPatientSafeCheckResult", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}	
	
	private String getExdtLate6(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","exdt"};
		MFGet instance = MFGetExdtLate6.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getExdtLate6", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}	
	
	private String delEmrScan(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bdiv","exdt","seq","rptcd","sub_page_list"};
		MFPut instance = MFPutDelEmrScan.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "delEmrScan", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String delPreSaved(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","pre_saved_bdiv","exdt","seq","sub_page_list"};
		MFPut instance = MFPutDelPreSaved.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "delEmrScan", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String putCvrConfirm(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt","bdiv","odt","ono"};
		MFPut instance = MFPutCvrConfirm.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "putPatientSafeCheckResult", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getLaborRecord(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt"};
		MFGet instance = MFGetLaborRecord.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "MFGetLaborRecord", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}	
	
	private String getNrChart(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","pid","bededt","frdt","todt","bdiv"};
		MFGet instance = MFGetNrChartList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getNrChart", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String putNrChart(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt","bdiv","wdate","seq","wtime","result","pdrid"};
		MFPut instance = MFPutNrChart.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "putNrChart", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
}
