import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MFGetCcfItemList implements MFGet  {
	private static MFGetCcfItemList mInstance=null;
	private MFGetCcfItemList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetCcfItemList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCcfItemList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("usrid");

		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		int rowCount=0;
		
		String[] ary = new String[] { 
				"pid", "pnm", "resid", "bthdt", "bthdt_mdy", "bthdt_dmy", "age", "psex", "addr", "htelno", "otelno", "ntelno", "bededt", "bedodt", "qfynm", 
				"ibdyy", "ibdmm", "ibddd", "dptcd", "dptnm", "drnm", "drnm_eng", "drsign", "gdrlcid", "sdrlcid", "logindrnm", "logindrsign", "logingdrlcid", "loginsdrlcid", 
				"ward", "wardnm", "rmid", "dxd", 
				"rsvop", "rsvdacd", "rsvopdt", "rsvopdptnm", "rsvopdrnm", "rsvopdt_ymd", "rsvop_2nd", "rsvdacd_2nd",
				"yy", "mm", "dd", "mm_eng", "hhhh", "mmmm", "ssss", "yyyymmdd", "yyyymmdd_2" };
		
		for(int i=0 ; i<ary.length ; i++){
			rowCount++;
			columns = new JSONObject();	
			columns.put("ccf_field", ary[i]);
			columns.put("ccf_field_text", getFieldText(ary[i]));
			rowData.add(columns);
		}
		
        // 나무병원 전용
        if ("0051".equalsIgnoreCase(hospitalId)) {
            String[] ary_0051 = null;
            ary_0051 = new String[] { 
                "dischk1", "disetc1", 
                "dischk2", "disetc2", 
                "dischk3", "disetc3", 
                "dischk4", "disetc4", 
                "dischk5", "disetc5", 
                "dischk6", "disetc6", 
                "dischk7", "disetc7", 
                "dischk8", "disetc8", 
                "dischk9", "disetc9", 
                "dischk10", "disetc10", 
                "dischk11", "disetc11", 
                "dischk12", "disetc12", 
                "opration","opretc", 
                "medchk1", "medetc1", 
                "medchk2", "medetc2", 
                "medchk3", "medetc3", 
                "specchk1", "specetc1", 
                "specchk5", "specetc5", 
                "specchk2", 
                "specchk3",
                "esigned",
                "nonmed", "stopdt",
                "namu_rsvdt"
                 };
    		for(int i=0 ; i<ary_0051.length ; i++){
    			rowCount++;
    			columns = new JSONObject();	
    			columns.put("ccf_field", ary_0051[i]);
    			columns.put("ccf_field_text", getFieldText(ary_0051[i]));
    			rowData.add(columns);
    		}
        }
        // 백두병원 전용
        if ("0134".equalsIgnoreCase(hospitalId)){
            String[] ary_0134 = null;
            ary_0134 = new String[] { 
                "bd_1591_5",
                "bd_1591_9",
                "bd_1591_22",
                "bd_1591_23",
                "bd_1591_11",
                "bd_1591_14",
                "bd_1591_15",
                "bd_1591_24",

                // 2022.01.02 WOOIL - 이하 마취전 환자 평가표
                "bd2_rptdt",
                "bd2_rpttm",
                "bd2_opdt",
                "bd2_opnm",
                "bd2_dxnm",
                "bd2_preop1",
                "bd2_preop2",
                "bd2_preop3",
                "bd2_preop4",
                "bd2_preop5",
                "bd2_preop6",
                "bd2_preop7",
                "bd2_bmi",
                "bd2_gumsa1",
                "bd2_gumsa2",
                "bd2_gumsa3",
                "bd2_gumsa4",
                "bd2_gumsa5",
                "bd2_gumsa6",
                "bd2_gumsa7",
                "bd2_gumsa8",
                "bd2_gumsa9",
                "bd2_gumsa10",
                "bd2_gumsa11",
                "bd2_gumsa12",
                "bd2_gumsa13",
                "bd2_rh",
                "bd2_dise1",
                "bd2_dise2",
                "bd2_dise3",
                "bd2_dise4",
                "bd2_dise5",
                "bd2_dise6",
                "bd2_dise7",
                "bd2_disetxt",
                "bd2_ophis",
                "bd2_ophistxt",
                "bd2_bigo",
                "bd2_asa1",
                "bd2_asa2",
                "bd2_asa3",
                "bd2_asa4",
                "bd2_asa5",
                "bd2_asa6",
                "bd2_mallampati1",
                "bd2_mallampati2",
                "bd2_mallampati3",
                "bd2_mallampati4",
                "bd2_aneplan1",
                "bd2_aneplan2",
                "bd2_aneplan3",
                "bd2_aneplan4",
                "bd2_aneplan5",
                "bd2_aneplan6",
                "bd2_drug_allergy1",
                "bd2_drug_allergy2",
                "bd2_medication1",
                "bd2_medication2",
                "bd2_drug_his",
                "bd2_neck_ex1",
                "bd2_neck_ex2",
                "bd2_mouth1",
                "bd2_mouth2",
                "bd2_teeth_ex1",
                "bd2_teeth_ex2",
                "bd2_alcoh1",
                "bd2_alcoh2",
                "bd2_alcoh3",
                "bd2_smoke1",
                "bd2_smoke2",
                "bd2_smoke3",
                "bd2_neck",
                "bd2_anedrnm"
                 };
    		for(int i=0 ; i<ary_0134.length ; i++){
    			rowCount++;
    			columns = new JSONObject();	
    			columns.put("ccf_field", ary_0134[i]);
    			columns.put("ccf_field_text", getFieldText(ary_0134[i]));
    			rowData.add(columns);
    		}
        }
        
        // 2023.12.12 WOOIL - 비급여 동의서
        String[] ary_bi_dong = new String[] { 
            "bi_no_1","bi_onm_1","bi_gumak_1",
            "bi_no_2","bi_onm_2","bi_gumak_2",
            "bi_no_3","bi_onm_3","bi_gumak_3",
            "bi_no_4","bi_onm_4","bi_gumak_4",
            "bi_no_5","bi_onm_5","bi_gumak_5",
            "bi_no_6","bi_onm_6","bi_gumak_6",
            "bi_no_7","bi_onm_7","bi_gumak_7",
            "bi_no_8","bi_onm_8","bi_gumak_8",
            "bi_no_9","bi_onm_9","bi_gumak_9",
            "bi_no_10","bi_onm_10","bi_gumak_10",
            "bi_no_11","bi_onm_11","bi_gumak_11",
            "bi_no_12","bi_onm_12","bi_gumak_12",
            "bi_no_13","bi_onm_13","bi_gumak_13",
            "bi_no_14","bi_onm_14","bi_gumak_14",
            "bi_no_15","bi_onm_15","bi_gumak_15",
            "bi_gumak_tot"
        };
		for(int i=0 ; i<ary_bi_dong.length ; i++){
			rowCount++;
			columns = new JSONObject();	
			columns.put("ccf_field", ary_bi_dong[i]);
			columns.put("ccf_field_text", getFieldText(ary_bi_dong[i]));
			rowData.add(columns);
		}
		
		// 2026.04.21 WOOIL - PDF문서용
        String[] ary_pdf = new String[] { 
                "pdf_field"
        };
		for(int i=0 ; i<ary_pdf.length ; i++){
			rowCount++;
			columns = new JSONObject();	
			columns.put("ccf_field", ary_pdf[i]);
			columns.put("ccf_field_text", getFieldText(ary_pdf[i]));
			rowData.add(columns);
		}
		
		

		// 리턴값과 메시지
		columns = new JSONObject();
		columns.put("return_code",rowCount);
		columns.put("return_desc","ok");
		status.add(columns);
		// 반환자료
		result.add(status);
		result.add(rowData);
		
		return result.toJSONString();
	}
	
    private String getFieldText(String field)
    {
        if (field == "pid") return "환자ID";
        if (field == "pnm") return "환자명";
        if (field == "psex") return "성별";
        if (field == "age") return "나이";
        if (field == "resid") return "주민등록번호";
        if (field == "bthdt") return "생년월일";
        if (field == "bthdt_mdy") return "생년월일(월.일.년)";
        if (field == "bthdt_dmy") return "생년월일(일.월.년)";
        if (field == "addr") return "주소";
        if (field == "htelno") return "전화번호1";
        if (field == "otelno") return "전화번호2";
        if (field == "ntelno") return "전화번호3";
        if (field == "yy") return "년도(현재)";
        if (field == "mm") return "월(현재)";
        if (field == "dd") return "일(현재)";
        if (field == "mm_eng") return "영문월(현재)";
        if (field == "hhhh") return "시(현재)";
        if (field == "mmmm") return "분(현재)";
        if (field == "ssss") return "초(현재)";
        if (field == "yyyymmdd") return "현재일(년.월.일)";
        if (field == "yyyymmdd_2") return "현재일(yyyy년 mm월 dd일)";
        if (field == "bededt") return "입원일자(8자리)";
        if (field == "bedodt") return "퇴원일자(8자리)";
        if (field == "qfynm") return "자격명";
        if (field == "insnm") return "보호자명";
        if (field == "famrelnm") return "관계";
        if (field == "p_resid") return "보호자주민등록번호";
        if (field == "dptcd") return "진료과코드";
        if (field == "dptnm") return "진료과";
        if (field == "drnm") return "주치의";
        if (field == "drnm_eng") return "주치의(영문명)";
        if (field == "logindrnm") return "로그인의사(>주치의)";
        if (field == "drsign") return "주치의sign";
        if (field == "logindrsign") return "로그인의사sign(>주치의)";
        if (field == "gdrlcid") return "주치의면허번호";
        if (field == "logingdrlcid") return "로그인의사면허번호(>주치의)";
        if (field == "sdrlcid") return "주치의전문의번호";
        if (field == "loginsdrlcid") return "로그인의사전문의번호(>주치의)";
        if (field == "ward") return "병실(병동-병실)";
        if (field == "wardnm") return "병동명"; // 2024.07.19 WOOIL - 추가
        if (field == "rmid") return "병실ID"; // 2024.07.19 WOOIL - 추가
        if (field == "maddr") return "보호자주소";
        if (field == "ibdyy") return "년도(입원)";
        if (field == "ibdmm") return "월(입원)";
        if (field == "ibddd") return "일(입원)";
        if (field == "rsvop") return "수술명";
        if (field == "rsvdacd") return "수술전진단";
        if (field == "rsvopdt") return "수술(예정)일(8자리)";
        if (field == "rsvopdt_ymd") return "수술(예정)일(년월일표시)";
        if (field == "rsvopdptnm") return "수술과";
        if (field == "rsvopdrnm") return "수술의명";
        if (field == "dxd") return "주진단명";

        if (field == "rsvop_2nd") return "수술명(두번째)"; // 2023.10.19 WOOIL - 추가
        if (field == "rsvdacd_2nd") return "수술 진단명(두번째)"; // 2024.09.24 WOOIL - 추가

        // 나무병월에서 사용하는 필드
        if (field == "disetc1") return "고혈압내용";
        if (field == "disetc2") return "당뇨병내용";
        if (field == "disetc3") return "심장질환내용";
        if (field == "disetc4") return "호흡기계질활내용";
        if (field == "disetc5") return "신장질환내용";
        if (field == "disetc6") return "암내용";
        if (field == "disetc7") return "간질환내용";
        if (field == "disetc8") return "뇌혈관질환내용";
        if (field == "disetc9") return "녹내장내용";
        if (field == "disetc10") return "전립선비대내용";
        if (field == "disetc11") return "혈액응고질환내용";
        if (field == "disetc12") return "기타질환내용";
        if (field == "opration") return "수술력 유체크";
        if (field == "opretc") return "수술력 유내용";
        if (field == "medetc1") return "항혈소판내용";
        if (field == "medetc2") return "경구혈당내용";
        if (field == "medetc3") return "복용약물기타내용";
        if (field == "specetc1") return "알레르기내용";
        if (field == "specetc5") return "기타기타내용";
        if (field == "dischk1") return "고혈압체크";
        if (field == "dischk2") return "당뇨병체크";
        if (field == "dischk3") return "심장질환체크";
        if (field == "dischk4") return "호흡기질환체크";
        if (field == "dischk5") return "신장질환체크";
        if (field == "dischk6") return "암체크";
        if (field == "dischk7") return "간질환체크";
        if (field == "dischk8") return "뇌혈관질환체크";
        if (field == "dischk9") return "녹내장체크";
        if (field == "dischk10") return "전립선비대체크";
        if (field == "dischk11") return "혈액응고질환체크";
        if (field == "dischk12") return "기타질환체크";
        if (field == "medchk1") return "항혈소판체크";
        if (field == "medchk2") return "경구혈당체크";
        if (field == "medchk3") return "복용약물기타체크";
        if (field == "specchk1") return "알레르기체크";
        if (field == "specchk2") return "기도이상체크";
        if (field == "specchk3") return "턱관절장애체크";
        if (field == "specchk5") return "기타기타체크";
        if (field == "esigned") return "(전자서명됨)";
        if (field == "nonmed") return "해당사항없음"; // 2021.01.21 WOOIL
        if (field == "stopdt") return "일전중지"; // 2021.01.21 WOOIL
        if (field == "namu_rsvdt") return "내시경예약일"; // 2021.02.15 WOOIL

        // 백두병원 용
        if (field == "bd_1591_5") return "SP.환자성명"; // 2021.07.27 WOOIL
        if (field == "bd_1591_9") return "SP.의사명"; // 2021.07.27 WOOIL
        if (field == "bd_1591_22") return "SP.성별"; // 2021.07.27 WOOIL
        if (field == "bd_1591_23") return "SP.나이"; // 2021.07.27 WOOIL
        if (field == "bd_1591_11") return "SP.수술일"; // 2021.07.27 WOOIL
        if (field == "bd_1591_14") return "SP.수술명"; // 2021.07.27 WOOIL
        if (field == "bd_1591_15") return "SP.내용"; // 2021.07.27 WOOIL
        if (field == "bd_1591_24") return "SP.주치의"; // 2021.07.27 WOOIL

        // 2023.01.02 WOOIL - 백두병원 마취전 환자 평가표
        if (field == "bd2_rptdt") return "BD2.작성일자";
        if (field == "bd2_rpttm") return "BD2.작성시간";
        if (field == "bd2_opdt") return "BD2.수술일자";
        if (field == "bd2_opnm") return "BD2.수술명";
        if (field == "bd2_dxnm") return "BD2.진단명";
        if (field == "bd2_preop1") return "BD2.혈압(수축기)";
        if (field == "bd2_preop2") return "BD2.혈압(이완기)";
        if (field == "bd2_preop3") return "BD2.맥박";
        if (field == "bd2_preop4") return "BD2.호흡";
        if (field == "bd2_preop5") return "BD2.체온";
        if (field == "bd2_preop6") return "BD2.신장";
        if (field == "bd2_preop7") return "BD2.체중";
        if (field == "bd2_bmi") return "BD2.BMI";
        if (field == "bd2_gumsa1") return "BD2.Chest X-ray";
        if (field == "bd2_gumsa2") return "BD2.EKG";
        if (field == "bd2_gumsa3") return "BD2.Echo";
        if (field == "bd2_gumsa4") return "BD2.Abd sono";
        if (field == "bd2_gumsa5") return "BD2.PFT";
        if (field == "bd2_gumsa6") return "BD2.Hb/Hct";
        if (field == "bd2_gumsa7") return "BD2.BUN/Cr";
        if (field == "bd2_gumsa8") return "BD2.HIV";
        if (field == "bd2_gumsa9") return "BD2.PT/aPTT";
        if (field == "bd2_gumsa10") return "BD2.HbsAg/Ab";
        if (field == "bd2_gumsa11") return "BD2.VDRL";
        if (field == "bd2_gumsa12") return "BD2.GOT/GPT";
        if (field == "bd2_gumsa13") return "BD2.HCV/Ab";
        if (field == "bd2_rh") return "BD2.혈액형";
        if (field == "bd2_dise1") return "BD2.DM";
        if (field == "bd2_dise2") return "BD2.HTN";
        if (field == "bd2_dise3") return "BD2.Asthma";
        if (field == "bd2_dise4") return "BD2.Thyroid dz";
        if (field == "bd2_dise5") return "BD2.Allergy";
        if (field == "bd2_dise6") return "BD2.없음";
        if (field == "bd2_dise7") return "BD2.기타";
        if (field == "bd2_disetxt") return "BD2.기타내용";
        if (field == "bd2_ophis") return "BD2.과거수술특이사항없음";
        if (field == "bd2_ophistxt") return "BD2.과거수술";
        if (field == "bd2_bigo") return "BD2.과거수술비고";
        if (field == "bd2_asa1") return "BD2.ASA class1";
        if (field == "bd2_asa2") return "BD2.ASA class2";
        if (field == "bd2_asa3") return "BD2.ASA class3";
        if (field == "bd2_asa4") return "BD2.ASA class4";
        if (field == "bd2_asa5") return "BD2.ASA class5";
        if (field == "bd2_asa6") return "BD2.ASA Emergency";
        if (field == "bd2_mallampati1") return "BD2.상기도 Class1";
        if (field == "bd2_mallampati2") return "BD2.상기도 Class2";
        if (field == "bd2_mallampati3") return "BD2.상기도 Class3";
        if (field == "bd2_mallampati4") return "BD2.상기도 Class4";
        if (field == "bd2_aneplan1") return "BD2.마취계획 Gen";
        if (field == "bd2_aneplan2") return "BD2.마취계획 Spinal";
        if (field == "bd2_aneplan3") return "BD2.마취계획 Epidural";
        if (field == "bd2_aneplan4") return "BD2.마취계획 BPB";
        if (field == "bd2_aneplan5") return "BD2.마취계획 IV Gen";
        if (field == "bd2_aneplan6") return "BD2.마취계획 기타";
        if (field == "bd2_drug_allergy1") return "BD2.약물 알레르기 NO";
        if (field == "bd2_drug_allergy2") return "BD2.약물 알레르기 YES";
        if (field == "bd2_medication1") return "BD2.Medication NO";
        if (field == "bd2_medication2") return "BD2.Medication YES";
        if (field == "bd2_drug_his") return "BD2.Drug Hx 비고";
        if (field == "bd2_neck_ex1") return "BD2.Neck Good";
        if (field == "bd2_neck_ex2") return "BD2.Neck Poor";
        if (field == "bd2_mouth1") return "BD2.Mouth Good";
        if (field == "bd2_mouth2") return "BD2.Mouth Poor";
        if (field == "bd2_teeth_ex1") return "BD2.Teeth Good";
        if (field == "bd2_teeth_ex2") return "BD2.Teeth Poor";
        if (field == "bd2_alcoh1") return "BD2.음주 NO";
        if (field == "bd2_alcoh2") return "BD2.음주 YES";
        if (field == "bd2_alcoh3") return "BD2.음주 내용";
        if (field == "bd2_smoke1") return "BD2.흡연 NO";
        if (field == "bd2_smoke2") return "BD2.흡연 YES";
        if (field == "bd2_smoke3") return "BD2.흡연 내용";
        if (field == "bd2_neck") return "BD2.기도상태 비고";
        if (field == "bd2_anedrnm") return "BD2.마취의성명";

        // 2023.12.12 WOOIL - 비급여 사용 동의서
        if (field == "bi_no_1") return "비급여 동의서 no 1";
        if (field == "bi_onm_1") return "비급여 동의서 명칭 1";
        if (field == "bi_gumak_1") return "비급여 동의서 금액 1";
        if (field == "bi_no_2") return "비급여 동의서 no 2";
        if (field == "bi_onm_2") return "비급여 동의서 명칭 2";
        if (field == "bi_gumak_2") return "비급여 동의서 금액 2";
        if (field == "bi_no_3") return "비급여 동의서 no 3";
        if (field == "bi_onm_3") return "비급여 동의서 명칭 3";
        if (field == "bi_gumak_3") return "비급여 동의서 금액 3";
        if (field == "bi_no_4") return "비급여 동의서 no 4";
        if (field == "bi_onm_4") return "비급여 동의서 명칭 4";
        if (field == "bi_gumak_4") return "비급여 동의서 금액 4";
        if (field == "bi_no_5") return "비급여 동의서 no 5";
        if (field == "bi_onm_5") return "비급여 동의서 명칭 5";
        if (field == "bi_gumak_5") return "비급여 동의서 금액 5";
        if (field == "bi_no_6") return "비급여 동의서 no 6";
        if (field == "bi_onm_6") return "비급여 동의서 명칭 6";
        if (field == "bi_gumak_6") return "비급여 동의서 금액 6";
        if (field == "bi_no_7") return "비급여 동의서 no 7";
        if (field == "bi_onm_7") return "비급여 동의서 명칭 7";
        if (field == "bi_gumak_7") return "비급여 동의서 금액 7";
        if (field == "bi_no_8") return "비급여 동의서 no 8";
        if (field == "bi_onm_8") return "비급여 동의서 명칭 8";
        if (field == "bi_gumak_8") return "비급여 동의서 금액 8";
        if (field == "bi_no_9") return "비급여 동의서 no 9";
        if (field == "bi_onm_9") return "비급여 동의서 명칭 9";
        if (field == "bi_gumak_9") return "비급여 동의서 금액 9";
        if (field == "bi_no_10") return "비급여 동의서 no 10";
        if (field == "bi_onm_10") return "비급여 동의서 명칭 10";
        if (field == "bi_gumak_10") return "비급여 동의서 금액 10";
        if (field == "bi_no_11") return "비급여 동의서 no 11";
        if (field == "bi_onm_11") return "비급여 동의서 명칭 11";
        if (field == "bi_gumak_11") return "비급여 동의서 금액 11";
        if (field == "bi_no_12") return "비급여 동의서 no 12";
        if (field == "bi_onm_12") return "비급여 동의서 명칭 12";
        if (field == "bi_gumak_12") return "비급여 동의서 금액 12";
        if (field == "bi_no_13") return "비급여 동의서 no 13";
        if (field == "bi_onm_13") return "비급여 동의서 명칭 13";
        if (field == "bi_gumak_13") return "비급여 동의서 금액 13";
        if (field == "bi_no_14") return "비급여 동의서 no 14";
        if (field == "bi_onm_14") return "비급여 동의서 명칭 14";
        if (field == "bi_gumak_14") return "비급여 동의서 금액 14";
        if (field == "bi_no_15") return "비급여 동의서 no 15";
        if (field == "bi_onm_15") return "비급여 동의서 명칭 15";
        if (field == "bi_gumak_15") return "비급여 동의서 금액 15";
        if (field == "bi_gumak_tot") return "비급여 동의서 금액 합계";
        
        if (field == "pdf_field") return "PDF 필드";

        return field;
    }

}
