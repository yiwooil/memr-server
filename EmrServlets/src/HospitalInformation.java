import java.util.HashMap;


public class HospitalInformation {
	private static HashMap<String,Hospital> hosMap = null;
	public static Hospital getHospital(String hospitalId) throws Exception{
		if(hosMap==null) hosMap = new HashMap<String,Hospital>();
		if(!hosMap.containsKey(hospitalId)){
			Hospital hos = new Hospital();
			hos.hospitalId=hospitalId;
			hos.hospitalName="";
			hos.databaseUrl="";
			hos.homeUrl="";
			hos.maskYn="";
			hos.scanUrl="";
			hos.preSaveUrl="";
			hos.mp4Url="";
			hos.picUrl="";
			hos.interfaceTableYn="";
//			hos.tprTableSepYn="";
//			hos.tprTableBedInDateNoYn="";
//			hos.chartHistTableBedInDateNoYn="";
			hos.emrCompany="";
			hos.emrScanUrl="";
			hos.emrScanUrlFormat="";
			hos.emrDateFormat="";
			hos.emrTimeFormat="";
			hos.emrResidFormat="";
//			hos.hosLogoImageFile="";
//			hos.hosLogoImageUrl="";
			hos.inPatientListDoctDeptnmYn="";
			hos.inPatientListDoctPopupButtonHideYn="";
			hos.tprEditButtonHideYn="";
			hos.pwdSkpYn="";
			hos.jainComPidLen="7"; // 기본 7
			hos.fileNamePrefix="";
			hos.fileNamePrefixPresave="";
			hos.fileNamePrefixPic="";
			hos.fileNamePrefixMP4="";
			hos.patientSafeCheckYn="";
			hos.certificateHideYn="";
			hos.collapseYn="";// 2024.04.23 WOOIL - 동의서 리스트를 기본 펼치지 않을지 여부
			hos.useDrSignTable=""; // 2025.08.12 WOOIL - 의사 사인을 테이블에서 읽을지 여부
			hos.barcodeScannerYn=""; // 2026.01.29 WOOIL - 환자안전관리 화면에서 내장 카메라로 바코드를 읽을지 여부
			hos.nrChartAiYn=""; // 2026.03.20 WOOIL - 간호기록지에 AI기능 활성화 여부
			/*
			ServletHelper servletHelper = null;
			servletHelper = new ServletHelper();
			String servletIp = "HospitalInformationServlet?hospitalid=" +hospitalId;
			String info = servletHelper.getXml(servletIp);
			new LogWrite().debugWrite("HospitalInfomation", "getHospital", "info=" + info);
			*/
			HashMap<String,Object>param = new HashMap<String,Object>();
			param.put("hospitalid", hospitalId);
			param.put("xml_file_name", "config");
			MFGet instance = MFGetHospitalInformation.getInstance();
			String info = instance.getData(param);
			//new LogWrite().debugWrite("HospitalInfomation", "getHospital", "info=" + info);
			
			ResultSetHelper rsHelper = new ResultSetHelper(info);
			int count=rsHelper.getRecordCount();
			if(count>0){
				hos.hospitalName = rsHelper.getString(0,"hospital_name");
				hos.databaseUrl = rsHelper.getString(0,"database_url");
				hos.homeUrl = rsHelper.getString(0,"home_url");
				hos.maskYn = rsHelper.getString(0,"mask_yn");
				hos.scanUrl = rsHelper.getString(0,"scan_url");
				hos.preSaveUrl = rsHelper.getString(0,"presave_url");
				hos.mp4Url = rsHelper.getString(0,"mp4_url");
				hos.picUrl = rsHelper.getString(0,"pic_url");
				hos.interfaceTableYn = rsHelper.getString(0,"interface_table_yn");
//				hos.tprTableSepYn = rsHelper.getString(0,"tpr_table_sep_yn");
//				hos.tprTableBedInDateNoYn = rsHelper.getString(0,"tpr_table_bed_in_date_no_yn");
//				hos.chartHistTableBedInDateNoYn = rsHelper.getString(0,"chart_hist_table_bed_in_date_no_yn");
				hos.emrCompany = rsHelper.getString(0,"emr_company");
				hos.emrScanUrl = rsHelper.getString(0,"emr_scan_url");
				hos.emrScanUrlFormat = rsHelper.getString(0,"emr_scan_url_format");
				hos.emrDateFormat = rsHelper.getString(0,"emr_date_format");
				hos.emrTimeFormat = rsHelper.getString(0,"emr_time_format");
				hos.emrResidFormat = rsHelper.getString(0,"emr_resid_format");
//				hos.hosLogoImageFile = rsHelper.getString(0,"hos_logo_image_file");
//				hos.hosLogoImageUrl = rsHelper.getString(0,"hos_logo_image_url");
				hos.inPatientListDoctDeptnmYn = rsHelper.getString(0,"in_patient_list_doct_deptnm");
				hos.inPatientListDoctPopupButtonHideYn = rsHelper.getString(0,"in_patient_list_doct_popup_button_hide_yn");
				hos.tprEditButtonHideYn = rsHelper.getString(0,"tpr_edit_button_hide_yn");
				hos.pwdSkpYn = rsHelper.getString(0,"pwd_skp_yn");
				hos.jainComPidLen = rsHelper.getString(0,"jain_com_pid_len");
				hos.fileNamePrefix = rsHelper.getString(0,"filename_prefix");
				hos.fileNamePrefixPresave = rsHelper.getString(0,"filename_prefix_presave");
				hos.fileNamePrefixPic = rsHelper.getString(0,"filename_prefix_pic");
				hos.fileNamePrefixMP4 = rsHelper.getString(0,"filename_prefix_mp4");
				hos.patientSafeCheckYn = rsHelper.getString(0,"patient_safe_check_yn");
				hos.certificateHideYn = rsHelper.getString(0,"certificate_hide_yn");
				hos.collapseYn = rsHelper.getString(0,"collapse_yn");// 2024.04.23 WOOIL - 동의서 리스트를 기본 펼치지 않을지 여부
				hos.useDrSignTable = rsHelper.getString(0,"use_dr_sign_table"); // 2025.08.12 WOOIL - 의사 사인을 테이블에서 읽을지 여부
				hos.barcodeScannerYn = rsHelper.getString(0,"barcode_scanner_yn"); // 2026.01.29 WOOIL - 환자안전관리 화면에서 내장 카메라로 바코드를 읽을지 여부
				hos.nrChartAiYn = rsHelper.getString(0,"nr_chart_ai_yn"); // 2026.03.20 WOOIL - 간호기록지에 AI기능 활성화 여부
			}
			hosMap.put(hospitalId, hos);
		}
		return hosMap.get(hospitalId);
	}

}
