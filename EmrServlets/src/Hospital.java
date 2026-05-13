
public class Hospital {
	public String hospitalId;
	public String hospitalName;
	public String databaseUrl;
	public String homeUrl;
	public String maskYn;
	public String scanUrl;
	public String preSaveUrl;
	public String mp4Url;
	public String picUrl;
	public String interfaceTableYn;
//	public String tprTableSepYn; // 비트에서는 tpr이 각각의 테이블로 분리되어있음.
//	public String tprTableBedInDateNoYn; // 비트에서는 tpr에 입원일이 없음.
//	public String chartHistTableBedInDateNoYn; // 비트에서는 차트에 입원일이 없음.
	public String emrCompany; // emr 업체
	public String emrScanUrl; // emr 업체가 지정한 동의서이미지 폴더. 값이 없으면 저장되지 않는다.
	public String emrScanUrlFormat; // emr업체가 지정한 동의서 폴더 생성 규칙
	public String emrDateFormat; // 비트에서 일자를 yyyy-mm-dd 로 관리하여 이를 처리하기 위한 용도
	public String emrTimeFormat; // 비트에서 시간을 hh:mm:ss 로 관리하여 이를 처리하기 위한 용도
	public String emrResidFormat; // 비트에서 주민번호를 123456-1234567로 관리하여 이를 처리하기 위한 용도
//	public String hosLogoImageFile;
//	public String hosLogoImageUrl;
	public String inPatientListDoctDeptnmYn; // 재원환자리스트조회시 의사컬럼에 부서명을 출력할지 여부(비트용)
	public String inPatientListDoctPopupButtonHideYn; // 재원환자리스트조회화면에 의사검색버튼 숨김여부
	public String tprEditButtonHideYn; // TPR화면에서 등록버튼 숨김여부
	public String pwdSkpYn; // 로그인시 비밀번호 체크를 하지 않는지여부
	public String jainComPidLen; // 자인컴인 경우 환자1의 길이(병원마다 다름) 
	public String fileNamePrefix; // TG02의 PATH에 저장할 때 추가할 경로. 파일을 저장하는 물리적인 위치는 아님.
	public String fileNamePrefixPresave; // TG02T의 PATH에 저장할 때 추가할 경로. 파일을 저장하는 물리적인 위치는 아님.
	public String fileNamePrefixPic; // TG02PIC의 PATH에 저장할 때 추가할 경로. 파일을 저장하는 물리적인 위치는 아님.
	public String fileNamePrefixMP4; // TG02MP4의 PATH에 저장할 때 추가할 경로. 파일을 저장하는 물리적인 위치는 아님.
	public String patientSafeCheckYn; // 환자안전관리를 사용하는지 여부
	public String certificateHideYn; // 동의서를 사용하지 않을지 여부
	public String collapseYn; // 2024.04.23 WOOIL - 동의서 리스트를 기본 펼치지 않을지 여부
	public String useDrSignTable; // 2025.08.12 WOOIL - 의사 사인을 테이블에서 읽을지 여부
	public String barcodeScannerYn; // 2026.01.29 WOOIL - 환자안전관리 화면에서 내장 카메라로 바코드를 읽을지 여부
	public String nrChartAiYn; // 2026.03.20 WOOIL - 간호기록지에 AI기능 활성화 여부
	public String presavedConsentFormListCollapseYn; // 2026.05.13 WOOIL - 임시저장동의서리서트 조회시 동의서+환자명인 경우 동의서별로 접혀서 조회되는지 여부
}
