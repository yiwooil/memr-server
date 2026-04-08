import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class CertificatePaperServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String mode = request.getParameter("mode");
		if (mode == null) mode = "0";
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "doGet", "mode="+mode);
		
		String resultString = "";
		if (mode.equals("0")) {
			// 증명서리스트조회
			resultString = getCertificateList(request);
		} else if (mode.equals("1")) {
			// 특정 증명서 내용 조회
			resultString = getCertificatePaper(request);
		} else if (mode.equals("2")) {
			// 저장할 파일명을 구한다.
			resultString = getFileName(request);
		} else if (mode.equals("3")) {
			// 사인된 증명서 이미지 저장
			// 특정 증명서 내용 조회
			resultString = saveCertificatePaper(request);
		} else if (mode.equals("4")){
			// 증명서리스트그룹조회
			resultString = getCertificateGroupList(request);
		} else if (mode.equals("5")){
			// 임시저장리스트조회
			resultString = getCertificatePreSavedList(request);
		} else if (mode.equals("6")){
			// 출력순서조정(하나위로)
			resultString = saveCertificateOrderUp(request);
		} else if (mode.equals("7")){
			// 출력순서조정(하나아래로)
			resultString = saveCertificateOrderDown(request);
		} else if (mode.equals("8")){
			// 그룹수정
			resultString = saveCertificateGroup(request);
		} else if (mode.equals("9")){
			// 그룹수정
			resultString = saveCertificateNew(request);
		} else if (mode.equals("10")){
			// 명칭수정
			resultString = saveCertificateName(request);
		} else if (mode.equals("11")){
			// 동의서에 출력될 환자정보
			resultString = getCcfValues(request);
		} else if (mode.equals("12")){
			// 동의서에 출력될 아이템 마스터 저장
			resultString = saveCcfItems(request);
		} else if (mode.equals("13")){
			// 동의서의 emr scan class 저장
			resultString = saveCertificateEmrScanClass(request);
		} else if (mode.equals("14")){
			// 임시저장리스트조회(모든환자)
			resultString = getCertificatePreSavedListAll(request);
		} else if (mode.equals("15")){
			// 2017.11.13 WOOIL - 동의서 삭제
			resultString = saveCertificateDel(request);
		} else if (mode.equals("16")){
			// 2022.03.03 WOOIL - 임시저장정보 읽기
			resultString = getCertificatePreSavedInfos(request);
		} else if (mode.equals("17")){
			// 2022.03.22 WOOIL - TG02의 PATH읽기
			resultString = getCertificatePaperPath(request);
		} else if (mode.equals("18")) {
			// 2023.01.06 WOOIL - 파일이 존재하는지 점검
			resultString = getFileExistsYN(request);
		} else if (mode.equals("19")) {
			// 2023.03.07 WOOIL - 수술이력조회
			resultString = getOpHx(request);
		} else if (mode.equals("20")) {
			// 2024.07.19 WOOIL - 동의서 아이템 리스트 조회(MEE에서 사용)
			resultString = getCcfItemList(request);
		} else if (mode.equals("21")) {
			// 2024.09.06 WOOIL - 여러 동의서를 한 동의서로 묶는다.
			resultString = saveCertificatePage(request);
		} else if (mode.equals("22")) {
			// 2026.04.01 WOOIL - 동의서가 삭제되었는지 검사한다.
			resultString = getCertificateDelCheck(request);
		} else {
			resultString = "unimplemented method.";
		}

		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private String getCertificateList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid"};
		MFGet instance = MFGetCertificateList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCertificateList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}

	private String getCertificatePaper(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getCertificatePaper", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid","pid","bededt","bdiv","no_fill"};
		MFGet instance = MFGetCertificatePaper.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCertificatePaper", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getCertificatePaper", "종료");
		}
	}

	private String getFileName(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","mst3cd","pid","bededt","presave","bdiv","apply_exdt"};
		MFGet instance = MFGetSaveCcfFileName.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getFileName", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String saveCertificatePaper(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","ccfId","pid","bededt","bdiv","filename","mp4filelist","picfilelist","apply_exdt","seq","sysdt","systm","rptnm","presave"
				                         ,"bf_presaved","bf_exdt","bf_seq","emr_scan_class","sub_page_list","sub_page_no"
				                         ,"dptcd","drid","qfycd","re_save_yn"};
		MFPut instance = MFPutCertificatePaper.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveCertificatePaper", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getCertificateGroupList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid"};
		MFGet instance = MFGetCertificateGroupList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCertificateGroupList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}

	private String getCertificatePreSavedList(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","pid"};
		MFGet instance = MFGetCertificatePreSavedList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCertificatePreSavedList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String getCertificatePreSavedListAll(HttpServletRequest request) {
		String paraKeys[] = new String[] {"hospitalid","userid","searchtext","sortorder","dept","pdrid"};
		MFGet instance = MFGetCertificatePreSavedListAll.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCertificatePreSavedListAll", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		}
	}
	
	private String saveCertificateOrderUp(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateOrderUp", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid"};
		MFPut instance = MFPutCertificateOrderUp.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveCertificateOrderUp", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateOrderUp", "종료");
		}
	}
	
	private String saveCertificateOrderDown(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateOrderDown", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid"};
		MFPut instance = MFPutCertificateOrderDown.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveCertificateOrderDown", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateOrderDown", "종료");
		}
	}
	
	private String saveCertificateGroup(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateGroup", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid","ccf_group","hx_type"};
		MFPut instance = MFPutCertificateGroup.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveCertificateGroup", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateGroup", "종료");
		}
	}
	
	private String saveCertificateNew(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateNew", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid","ccf_name","ccf_file","ccf_group","emr_scan_class"};
		MFPut instance = MFPutCertificateNew.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveCertificateNew", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateNew", "종료");
		}
	}
	
	private String saveCertificateName(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateName", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid","ccf_name"};
		MFPut instance = MFPutCertificateName.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveCertificateName", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateName", "종료");
		}
	}
	
	private String saveCertificateDel(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateDel", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid"};
		MFPut instance = MFPutCertificateDel.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveCertificateDel", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveCertificateDel", "종료");
		}
	}
	
	private String getCcfValues(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getCcfValues", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid","pid","bededt","bdiv","dptcd","bedodt","u01_pk_yn","u01_opdt","u01_dptcd","u01_opseq","u01_seq","dong_exdt"};
		MFGet instance = MFGetCcfValues.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCcfValues", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getCcfValues", "종료");
		}
	}
	
	private String saveCcfItems(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveCcfItems", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid","ccfitems"};
		MFPut instance = MFPutCcfItems.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveCcfItems", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveCcfItems", "종료");
		}
	}
	
	private String saveCertificateEmrScanClass(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveEmrScanClass", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","ccfid","emrscanclass"};
		MFPut instance = MFPutCertificateEmrScanClass.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveEmrScanClass", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveEmrScanClass", "종료");
		}
	}
	
	private String getCertificatePreSavedInfos(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getCertificatePreSavedFileInfo", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bdiv","exdt","seq","pre_saved_bdiv","re_save_yn"};
		MFGet instance = MFGetCertificatePreSavedInfos.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCertificatePreSavedFileInfo", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getCertificatePreSavedFileInfo", "종료");
		}
	}
	
	private String getCertificatePaperPath(HttpServletRequest request){
		new LogWrite().debugWrite(getClass().getSimpleName(), "getCertificatePaperPath", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bdiv","exdt","seq"};
		MFGet instance = MFGetCertificatePaperPath.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCertificatePaperPath", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getCertificatePaperPath", "종료");
		}
	}
	
	private String getFileExistsYN(HttpServletRequest request){
		new LogWrite().debugWrite(getClass().getSimpleName(), "getFileExistsYN", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid","file_name","file_type","pre_save"};
		MFGet instance = MFGetFileExistsYN.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getFileExistsYN", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getFileExistsYN", "종료");
		}
	}
	
	private String getOpHx(HttpServletRequest request){
		new LogWrite().debugWrite(getClass().getSimpleName(), "getOpHx", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bededt","bdiv"};
		MFGet instance = MFGetOpHx.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getOpHx", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getOpHx", "종료");
		}
	}
	
	private String getCcfItemList(HttpServletRequest request){
		new LogWrite().debugWrite(getClass().getSimpleName(), "getCcfItemList", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid"};
		MFGet instance = MFGetCcfItemList.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCcfItemList", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getCcfItemList", "종료");
		}
	}
	
	private String saveCertificatePage(HttpServletRequest request) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "saveEmrScanClass", "시작");
		String paraKeys[] = new String[] {"hospitalid","bf_page1_ccfid","bf_page1_sub_page_list","af_page1_ccfid","af_page1_sub_page_list","ccf_group","disp_ccf_list"};
		MFPut instance = MFPutCertificatePage.getInstance();
		try {
			return instance.putData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveEmrScanClass", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "saveEmrScanClass", "종료");
		}
	}
	
	
	
	private String getCertificateDelCheck(HttpServletRequest request){
		new LogWrite().debugWrite(getClass().getSimpleName(), "getCertificateDelCheck", "시작");
		String paraKeys[] = new String[] {"hospitalid","userid","pid","bdiv","exdt","seq","presaved_yn"};
		MFGet instance = MFGetCertificateDelCheck.getInstance();
		try {
			return instance.getData(Utility.getParaMap(paraKeys, request));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCertificateDelCheck", "Exception", e.getLocalizedMessage());
			return ExceptionHelper.toJSONString(e);
		} finally {
			new LogWrite().debugWrite(getClass().getSimpleName(), "getCertificateDelCheck", "종료");
		}
	}
}
