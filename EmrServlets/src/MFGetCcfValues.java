import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetCcfValues implements MFGet {
	private static MFGetCcfValues mInstance=null;
	private MFGetCcfValues(){
		
	}
	
	public static MFGetCcfValues getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCcfValues();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String ccfId = (String)param.get("ccfid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String bdiv = (String)param.get("bdiv");
		String dptcd = (String)param.get("dptcd");
		String bedodt = (String)param.get("bedodt"); // 2021.08.10 WOOIL - 퇴원일(외래 진료일시)
		String u01_pk_yn = (String)param.get("u01_pk_yn"); // 2023.03.07 WOOIL - 수술신청테이블을 PK로 읽을지 여부
		String u01_opdt = (String)param.get("u01_opdt"); // 2023.03.07 WOOIL - 수술신청테이블 PK
		String u01_dptcd = (String)param.get("u01_dptcd"); // 2023.03.07 WOOIL - 수술신청테이블 PK
		String u01_opseq = (String)param.get("u01_opseq"); // 2023.03.07 WOOIL - 수술신청테이블 PK
		String u01_seq = (String)param.get("u01_seq"); // 2023.03.07 WOOIL - 수술신청테이블 PK
		String dong_exdt = (String)param.get("dong_exdt"); // 2024.08.29 WOOIL - 비급여 동의서. 특정 진료일자의 비급여만 불러오도록
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "1) pid=" + pid + ", ccfId=" + ccfId + ", bdiv=" + bdiv + ", dptcd=" + dptcd + ", bededt=" + bededt + ", bedodt=" + bedodt + ", u01_pk_yn=" + u01_pk_yn + ",  userId=" + userId);
		
		if(bdiv==null) bdiv="2";
		if(dptcd==null) dptcd="";
		if(bedodt==null) bedodt="";
		if(u01_pk_yn==null) u01_pk_yn="";
		if(dong_exdt==null) dong_exdt="";
		
		if("Y".equalsIgnoreCase(u01_pk_yn)){
			new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "1-1) u01_opdt=" + u01_opdt + ", u01_dptcd=" + u01_dptcd + ", u01_opseq=" + u01_opseq + ", u01_seq=" + u01_seq);
		}
		
		// 2024.11.05 WOOIL - 응급실 내역인 경우 TS21에서 자료를 일기 위함.
		//                    응급실 경유 입원환자의 응급실 때의 동의서를 작성하기를 원하는 병원이 있어서 이렇게 했음(지인메이).
		//                    응급실 접수 내역인 경우 bedodt에 진료일시가 들어온다. hhmmdd임.
		//                *** 환자검색 화면에서 검색한 환자만 bedodt에 진료일시가 들어온다(응급실 환자인 경우). ***
		//                *** 입원환자리스트에는 bedodt가 공백으로 들어온다(응급실 환자인 경우). ***
		boolean isEr = false;
		if("2".equalsIgnoreCase(bdiv)&&"ER".equalsIgnoreCase(dptcd)&&bedodt.length()==6) isEr=true; 
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "2) pid=" + pid + ", ccfId=" + ccfId + ", bdiv=" + bdiv + ", dptcd=" + dptcd + ", bededt=" + bededt + ", bedodt=" + bedodt + ", u01_pk_yn=" + u01_pk_yn+", isEr="+isEr);
		
		SqlHelper sqlHelper = new SqlHelper(hospitalId);
		
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		int rowCount=0;
		
		String ccfValueMast = getCcfValueMast(hospitalId, ccfId);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "ccfValueMast=" + ccfValueMast);
		
		String ta01String = ""; // 환자기본인적사항
		String ta04String = ""; // 환지입원내역
		String ts21String = ""; // 외래접수내역
		String tu01String = ""; // 환자수술내역
		String tk71ivString = ""; // 나무병원 문진내역
		String tq03NamuString = ""; // 나무병원 내시경예약일
		String tk91Value1591String = ""; // 백두병원 마취과 special permission
		String emr082_2String = ""; // 2023.01.02 WOOIL - 백두병원 마취전환자평가서
		String biDongString = ""; // 2023.12.11 WOOIL 비급여동의서
		
		ResultSetHelper rsTA01 = null;
		ResultSetHelper rsTA04 = null;
		ResultSetHelper rsTS21 = null;
		ResultSetHelper rsTU01 = null;
		ResultSetHelper rsTK71IV = null;
		ResultSetHelper rsTQ03Namu = null;
		ResultSetHelper rsTK91Value1591 = null;
		ResultSetHelper rsEmr082_2 = null;
		ResultSetHelper rsBiDong = null;
		
		ResultSetHelper rsCcfValueMast = new ResultSetHelper(ccfValueMast);
		int ccfCount = rsCcfValueMast.getRecordCount();
		for(int ccfIdx=0;ccfIdx<ccfCount;ccfIdx++){
			String ccfField = rsCcfValueMast.getString(ccfIdx, "ccf_field");
			String ccfX  = rsCcfValueMast.getString(ccfIdx, "ccf_x");
			String ccfY  = rsCcfValueMast.getString(ccfIdx, "ccf_y");
			String ccfW  = rsCcfValueMast.getString(ccfIdx, "ccf_w");
			String ccfH  = rsCcfValueMast.getString(ccfIdx, "ccf_h");
			String ccfAutoFit = rsCcfValueMast.getString(ccfIdx, "ccf_auto_fit"); // 2024.04.26 WOOIL -
			String ccfValue = "";
			
			
			String ccfFieldType = getCcfFieldType(ccfField);
			//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "ccfField=" + ccfField + ", ccfFieldType=" + ccfFieldType);
			if(!"".equals(pid)&&"ta01".equalsIgnoreCase(ccfFieldType)){
				if(rsTA01==null){
					ta01String = getTA01Value(hospitalId, pid);
					rsTA01 = new ResultSetHelper(ta01String, false);
				}
				if(rsTA01.getRecordCount()>0){
					if(sqlHelper.getInterfaceTableYn() && "age".equalsIgnoreCase(ccfField)){
						// 이런 경우 나이를 따로 계산하여야한다.
						int age = Utility.getAgeYear(rsTA01.getString(0, "bthdt"),-1);
						if(age<0){
							ccfValue = ""; // 나이변환중 오류가 발생한 경우임.
						}else{
							ccfValue = Integer.toString(age);
						}
					}else{
						if("bthdt".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getFormattedDate(rsTA01.getString(0, ccfField.toLowerCase()));
						}else if("bthdt_mdy".equalsIgnoreCase(ccfField)){
							// 2024.12.16 WOOIL - 생년월일을 MM.DD.YYYY형식으로 반환(외국인용)
							ccfValue = Utility.getFormattedDate_mdy(rsTA01.getString(0, "bthdt"));
						}else if("bthdt_dmy".equalsIgnoreCase(ccfField)){
							// 2026.02.26 WOOIL - 생년월일을 DD.MM.YYYY형식으로 반환(외국인용)
							ccfValue = Utility.getFormattedDate_dmy(rsTA01.getString(0, "bthdt"));
						}else if("resid".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getFormattedResidWithMark(rsTA01.getString(0, ccfField.toLowerCase()));
						}else if("mm_eng".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getMmEng(rsTA01.getString(0, "mm"));
						}else{
							ccfValue = rsTA01.getString(0, ccfField.toLowerCase());
						}
					}
				}else{
					ccfValue = "";
				}
			}else if(!"".equals(pid)&&"ta04".equalsIgnoreCase(ccfFieldType)){
				if(bdiv.equals("2")&&isEr==false){
					// 입원인 경우(응급실 제외)
					if(rsTA04==null){
						ta04String = getTA04Value(hospitalId, userId, pid, bededt);
						rsTA04 = new ResultSetHelper(ta04String, false);
					}
					if(rsTA04.getRecordCount()>0){
						if("bededt".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getFormattedDate(rsTA04.getString(0, ccfField.toLowerCase()));
						}else if("ibdyy".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getYy(rsTA04.getString(0, "bededt"));
						}else if("ibdmm".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getMm(rsTA04.getString(0, "bededt"));
						}else if("ibddd".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getDd(rsTA04.getString(0, "bededt"));
						}else if("bedodt".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getFormattedDate(rsTA04.getString(0, ccfField.toLowerCase()));
						}else{
							ccfValue = rsTA04.getString(0, ccfField.toLowerCase());
						}
					}else{
						ccfValue = "";
					}
				}else{
					// 외래인 경우(응급실 포함)
					if(rsTS21==null){
						ts21String = getTS21Value(hospitalId, userId, pid, bededt, dptcd, bedodt);
						rsTS21 = new ResultSetHelper(ts21String, false);
					}
					if(rsTS21.getRecordCount()>0){
						if("bededt".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getFormattedDate(bededt);
						}else if("ibdyy".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getYy(bededt);
						}else if("ibdmm".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getMm(bededt);
						}else if("ibddd".equalsIgnoreCase(ccfField)){
							ccfValue = Utility.getDd(bededt);
						}else if("bedodt".equalsIgnoreCase(ccfField)){
							ccfValue = "";
						}else{
							ccfValue = rsTS21.getString(0, ccfField.toLowerCase());
						}
					}else{
						ccfValue = "";
					}
				}
			}else if(!"".equals(pid)&&"tu01".equalsIgnoreCase(ccfFieldType)){
				if(rsTU01==null){
					if("Y".equalsIgnoreCase(u01_pk_yn)){
						tu01String = getTU01Value(hospitalId, pid, u01_opdt, u01_dptcd, u01_opseq, u01_seq);
					}else{
						tu01String = getTU01Value(hospitalId, pid, bededt);
					}
					rsTU01 = new ResultSetHelper(tu01String, false);
				}
				if(rsTU01.getRecordCount()>0){
					ccfValue = rsTU01.getString(0, ccfField.toLowerCase());
				}else{
					ccfValue = "";
				}
			}else if(!"".equals(pid)&&"tu01_2nd".equalsIgnoreCase(ccfFieldType)){
				if(rsTU01==null){
					if("Y".equalsIgnoreCase(u01_pk_yn)){
						tu01String = getTU01Value(hospitalId, pid, u01_opdt, u01_dptcd, u01_opseq, u01_seq);
					}else{
						tu01String = getTU01Value(hospitalId, pid, bededt);
					}
					rsTU01 = new ResultSetHelper(tu01String, false);
				}
				if(rsTU01.getRecordCount()>1){
					ccfValue = rsTU01.getString(1, ccfField.toLowerCase());
				}else{
					ccfValue = "";
				}
			}else if(!"".equals(pid)&&"tk71iv".equalsIgnoreCase(ccfFieldType)){
				// 나무병원용
				if(rsTK71IV==null){
					tk71ivString = getTK71IVValue(hospitalId, pid);
					rsTK71IV = new ResultSetHelper(tk71ivString, false);
				}
				if(rsTK71IV.getRecordCount()>0){
					ccfValue = rsTK71IV.getString(0, ccfField.toLowerCase());
				}else{
					ccfValue = "";
				}
			}else if(!"".equals(pid)&&"tq03_namu".equalsIgnoreCase(ccfFieldType)){
				// 나무병원용
				if(rsTQ03Namu==null){
					tq03NamuString = getTQ03NamuValue(hospitalId, pid);
					rsTQ03Namu = new ResultSetHelper(tq03NamuString, false);
				}
				if(rsTQ03Namu.getRecordCount()>0){
					ccfValue = rsTQ03Namu.getString(0, ccfField.toLowerCase());
				}else{
					ccfValue = "";
				}
			}else if(!"".equals(pid)&&"tk91_value_1591".equalsIgnoreCase(ccfFieldType)){
				// 백두병원 마취과 special permission 용
				if(rsTK91Value1591==null){
					tk91Value1591String = getTK91Value1591(hospitalId, pid);
					rsTK91Value1591 = new ResultSetHelper(tk91Value1591String, false);
				}
				if(rsTK91Value1591.getRecordCount()>0){
					ccfValue = rsTK91Value1591.getString(0, ccfField.toLowerCase());
				}else{
					ccfValue = "";
				}
			}else if(!"".equals(pid)&&"emr082_2".equalsIgnoreCase(ccfFieldType)){
				// 2023.01.02 WOOIL - 백두병원 마취전환자평가서 용
				if(rsEmr082_2==null){
					emr082_2String = getEmr082_2Value(hospitalId, pid);
					rsEmr082_2 = new ResultSetHelper(emr082_2String, false);
				}
				if(rsEmr082_2.getRecordCount()>0){
					ccfValue = rsEmr082_2.getString(0, ccfField.toLowerCase());
				}else{
					ccfValue = "";
				}
			}else if(!"".equals(pid)&&"bi_dong".equalsIgnoreCase(ccfFieldType)){
				// 비급여 동의서
				// 처방을 읽어 비급여 코드만 찾아내 항목과 금액을 가져온다.
				if(rsBiDong==null){
					biDongString = getBiDongValue(hospitalId, pid, bededt, bdiv, dptcd, dong_exdt);
					rsBiDong = new ResultSetHelper(biDongString, false);
				}
				if(rsBiDong.getRecordCount()>0){
					ccfValue = rsBiDong.getString(0, ccfField.toLowerCase());
				}else{
					ccfValue = "";
				}
			}else if("hosnm".equalsIgnoreCase(ccfField)){
				ccfValue = sqlHelper.getHospitalName();
			}else if("esigned".equalsIgnoreCase(ccfField)){
				ccfValue = "(전자서명됨)";
			}else{
				ccfValue = "";
			}
			//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "ccfField=" + ccfField + ", ccfFieldType=" + ccfFieldType + ", ccfValue=" + ccfValue);
			
			rowCount++;
			columns = new JSONObject();	
			columns.put("ccf_field", ccfField);
			columns.put("ccf_x", ccfX);
			columns.put("ccf_y", ccfY);
			columns.put("ccf_w", ccfW);
			columns.put("ccf_h", ccfH);
			columns.put("ccf_auto_fit", ccfAutoFit); // 2024.04.26 WOOIL -
			columns.put("ccf_value", ccfValue);
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
		
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "result=" + result.toJSONString());
		
		return result.toJSONString();
	}
	
	private String getCcfFieldType(String ccfField){
		String ccfField4 = "";
		if(ccfField.length()>=4) ccfField4 = ccfField.substring(0, 4);
		
		String retString="";
		if("pid".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("pnm".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("psex".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("age".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("bthdt".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("bthdt_mdy".equalsIgnoreCase(ccfField)) retString="ta01"; // 2024.12.16 WOOIL
		else if("bthdt_dmy".equalsIgnoreCase(ccfField)) retString="ta01"; // 2026.06.26 WOOIL
		else if("resid".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("addr".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("htelno".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("otelno".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("ntelno".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("yy".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("mm".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("dd".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("mm_eng".equalsIgnoreCase(ccfField)) retString="ta01"; // 2026.06.26 WOOIL
		else if("hhhh".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("mmmm".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("ssss".equalsIgnoreCase(ccfField)) retString="ta01";
		else if("yyyymmdd".equalsIgnoreCase(ccfField)) retString="ta01"; // 2025.12.01 WOOIL
		else if("yyyymmdd_2".equalsIgnoreCase(ccfField)) retString="ta01"; // 2026.02.10 WOOIL - "2026년 02월 10일"로 나오게
		
		else if("bededt".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("bedodt".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("qfynm".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("insnm".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("famrelcd".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("p_resid".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("dptcd".equalsIgnoreCase(ccfField)) retString="ta04"; // 2026.01.27 WOOIL - 진료과코드
		else if("dptnm".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("drnm".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("drnm_eng".equalsIgnoreCase(ccfField)) retString="ta04"; // 2026.02.10 WOOIL - 주치의 영문명
		else if("ward".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("wardnm".equalsIgnoreCase(ccfField)) retString="ta04"; // 2024.07.18 WOOIL - 병동명
		else if("rmid".equalsIgnoreCase(ccfField)) retString="ta04"; // 2024.07.18 WOOIL - 병실ID
		else if("maddr".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("dxd".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("ibdyy".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("ibdmm".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("ibddd".equalsIgnoreCase(ccfField)) retString="ta04";
		else if("drsign".equalsIgnoreCase(ccfField)) retString="ta04"; // 2022.04.27 WOOIL - 의사 사인
		else if("logindrnm".equalsIgnoreCase(ccfField)) retString="ta04"; // 2024.03.04 WOOIL - 로그인의사 명(실제처리는 화면에서 한다.) 
		else if("logindrsign".equalsIgnoreCase(ccfField)) retString="ta04"; // 2024.05.30 WOOIL - 로그인의사 사인(실제처리는 화면에서 한다.)
		else if("gdrlcid".equalsIgnoreCase(ccfField)) retString="ta04"; // 2025.08.06 WOOIL - 주치의 면허번호
		else if("sdrlcid".equalsIgnoreCase(ccfField)) retString="ta04"; // 2025.08.06 WOOIL - 주치의 전문의 번호
		else if("logingdrlcid".equalsIgnoreCase(ccfField)) retString="ta04"; // 2025.08.06 WOOIL - 로그인의사 면허번호
		else if("loginsdrlcid".equalsIgnoreCase(ccfField)) retString="ta04"; // 2025.08.06 WOOIL - 로그인의사 전문의 번호
		
		
		else if("rsvop".equalsIgnoreCase(ccfField)) retString="tu01";
		else if("rsvdacd".equalsIgnoreCase(ccfField)) retString="tu01";
		else if("rsvopdt".equalsIgnoreCase(ccfField)) retString="tu01"; // 2021.07.29 WOOIL - 수술(예정)일자
		else if("rsvopdptnm".equalsIgnoreCase(ccfField)) retString="tu01"; // 2022.06.03 WOOIL - 수술과명
		else if("rsvopdrnm".equalsIgnoreCase(ccfField)) retString="tu01"; // 2024.01.30 WOOIL - 수술의명
		else if("rsvopdt_ymd".equalsIgnoreCase(ccfField)) retString="tu01"; // 2021.07.29 WOOIL - 수술(예정)일자(yyyy년mm월dd일)
		
		else if("rsvop_2nd".equalsIgnoreCase(ccfField)) retString="tu01_2nd"; // 2023.10.19 WOOIL - 당일 두 번째 수술명(진주바른)
		else if("rsvdacd_2nd".equalsIgnoreCase(ccfField)) retString="tu01_2nd"; // 2024.09.24 WOOIL - 당일 두 번째 수술 진단명(진주바른)
		
		// 이하 나무병원 용
		else if("disetc1".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc2".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc3".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc4".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc5".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc6".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc7".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc8".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc9".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc10".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc11".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("disetc12".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("opration".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("opretc".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("medetc1".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("medetc2".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("medetc3".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("specetc1".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("specetc5".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk1".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk2".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk3".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk4".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk5".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk6".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk7".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk8".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk9".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk10".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk11".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("dischk12".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("medchk1".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("medchk2".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("medchk3".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("specchk1".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("specchk2".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("specchk3".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("specchk5".equalsIgnoreCase(ccfField)) retString="tk71iv";
		else if("nonmed".equalsIgnoreCase(ccfField)) retString="tk71iv"; // 2021.01.21 WOOIL
		else if("stopdt".equalsIgnoreCase(ccfField)) retString="tk71iv"; // 2021.01.21 WOOIL
		else if("namu_rsvdt".equalsIgnoreCase(ccfField)) retString="tq03_namu"; // 2021.02.15 WOOIL
		
		// 2021.07.27 WOOIL - 이하 백두병원 용
		else if("bd_1591_5".equalsIgnoreCase(ccfField)) retString="tk91_value_1591"; // 환자성명 DKPNM,5
		else if("bd_1591_8".equalsIgnoreCase(ccfField)) retString="tk91_value_1591"; // 의사명 DKDRNM,8
		else if("bd_1591_22".equalsIgnoreCase(ccfField)) retString="tk91_value_1591"; // 성별 DKPSEX,22
		else if("bd_1591_23".equalsIgnoreCase(ccfField)) retString="tk91_value_1591"; // 나이 DKPAGE,23
		else if("bd_1591_11".equalsIgnoreCase(ccfField)) retString="tk91_value_1591"; // 수술일 DF_OPDT,11
		else if("bd_1591_14".equalsIgnoreCase(ccfField)) retString="tk91_value_1591"; // 수술명 DF_OPNM,14
		else if("bd_1591_15".equalsIgnoreCase(ccfField)) retString="tk91_value_1591"; // 내용 ___,15
		else if("bd_1591_24".equalsIgnoreCase(ccfField)) retString="tk91_value_1591"; // 주치의 DKDRNM,24
		
		// 2022.01.02 WOOIL - 백두병원 마취전 환자 평가서 용
		else if("bd2_".equalsIgnoreCase(ccfField4)) retString="emr082_2";
		
		// 2023.12.11 WOOIL - 비급여 동의서에 출력될 비급여내역
		else if("bi_no_1".equalsIgnoreCase(ccfField)) retString="bi_dong"; // 순번 
		else if("bi_no_2".equalsIgnoreCase(ccfField)) retString="bi_dong"; 
		else if("bi_no_3".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_4".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_5".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_6".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_7".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_8".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_9".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_10".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_11".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_12".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_13".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_14".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_no_15".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_1".equalsIgnoreCase(ccfField)) retString="bi_dong"; // 비급여 명칭(처방명)
		else if("bi_onm_2".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_3".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_4".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_5".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_6".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_7".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_8".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_9".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_10".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_11".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_12".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_13".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_14".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_onm_15".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_1".equalsIgnoreCase(ccfField)) retString="bi_dong"; // 비급여 급액
		else if("bi_gumak_2".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_3".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_4".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_5".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_6".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_7".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_8".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_9".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_10".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_11".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_12".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_13".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_14".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_15".equalsIgnoreCase(ccfField)) retString="bi_dong";
		else if("bi_gumak_tot".equalsIgnoreCase(ccfField)) retString="bi_dong"; // 합계
		
		return retString;
	}
	
	private String getCcfValueMast(String hospitalId, String ccfId) throws Exception{
		SqlHelper sqlHelper;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		//if(interfaceTableYn){
		//	sql = "select ccf_field,ccf_x,ccf_y,ccf_w,ccf_h from consent_form_value_mast where ccf_id=?";
		//}else{
		//	sql = "select cdnm ccf_field,fld1qty ccf_x,fld2qty ccf_y,fld3qty ccf_w,fld4qty ccf_h from ta88a where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
		//}
		 // 2024.04.26 WOOIL - ccf_auto_fit 추가
		sql = "";
		sql += "select cdnm as ccf_field" + "\n";
		sql += "     , fld1qty as ccf_x" + "\n";
		sql += "     , fld2qty as ccf_y" + "\n";
		sql += "     , fld3qty as ccf_w" + "\n";
		sql += "     , fld4qty as ccf_h" + "\n";
		sql += "     , isnull(fld1cd,'') as ccf_auto_fit" + "\n";
		sql += "  from ta88a" + "\n";
		sql += " where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
		para.put(1, ccfId);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		return rsString;
	}
	
	private String getTA01Value(String hospitalId, String pid) throws Exception{
		// 환자마스터
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		//if(interfaceTableYn){
		//	sql =
		//		"select a01.pat_id as pid" + 
		//	    "     , a01.pat_name as pnm" +
		//		"     , case isnull(a01.pat_sex,'') when 'M' then '남' when 'F' then '여' end as psex " +
		//		"     , a01.birth_date as bthdt" +
		//	    "     , '' as age" +
		//	    "     , a01.jumin_no as resid" +
		//	    "     , isnull(a01.address1,'') + ' ' + isnull(a01.address2,'') as addr" +
		//	    "     , isnull(a01.phone_no,'') as htelno" +
		//	    "     , a01.office_phone_no otelno" + 
		//	    "     , a01.mobile_no ntelno" +
		//	    "     , substring(convert(varchar,getdate(),112),1,4) as yy" +
		//	    "     , substring(convert(varchar,getdate(),112),5,2) as mm" +
		//	    "     , substring(convert(varchar,getdate(),112),7,2) as dd" +
		//	    "     , substring(convert(varchar,getdate(),24),1,2) as hhhh" +
		//	    "     , substring(convert(varchar,getdate(),24),4,2) as mmmm" +
		//	    "     , substring(convert(varchar,getdate(),24),7,2) as ssss" +
		//	    "  from pat_mast a01" +
		//	    " where a01.pat_id=?";			
		//}else{
			sql = "";
			sql += "select a01.pid" + "\n"; 
			sql += "     , a01.pnm" + "\n";
			sql += "     , case isnull(a01.psex,'') when 'M' then '남' when 'F' then '여' end as psex " + "\n";
			sql += "     , a01.bthdt as bthdt" + "\n";
			sql += "     , dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112)) as age" + "\n";
			sql += "     , a01.resid as resid" + "\n";
			sql += "     , isnull(a01.addr1,'') + ' ' + isnull(a01.addr2,'') as addr" + "\n";
			sql += "     , isnull(a01.htelno,'') as htelno" + "\n";
			sql += "     , a01.otelno" + "\n"; 
			sql += "     , a01.ntelno" + "\n";
			sql += "     , substring(convert(varchar,getdate(),112),1,4) as yy" + "\n";
			sql += "     , substring(convert(varchar,getdate(),112),5,2) as mm" + "\n";
			sql += "     , substring(convert(varchar,getdate(),112),7,2) as dd" + "\n";
			sql += "     , substring(convert(varchar,getdate(),24),1,2) as hhhh" + "\n";
			sql += "     , substring(convert(varchar,getdate(),24),4,2) as mmmm" + "\n";
			sql += "     , substring(convert(varchar,getdate(),24),7,2) as ssss" + "\n";
			sql += "     , substring(convert(varchar,getdate(),112),1,4)+'.'+substring(convert(varchar,getdate(),112),5,2)+'.'+substring(convert(varchar,getdate(),112),7,2) as yyyymmdd" + "\n";
			sql += "     , substring(convert(varchar,getdate(),112),1,4)+'년 '+substring(convert(varchar,getdate(),112),5,2)+'월 '+substring(convert(varchar,getdate(),112),7,2)+'일' as yyyymmdd_2" + "\n";
			sql += "  from ta01 a01" + "\n";
			sql += " where a01.pid=?" + "\n";
		//}
		para.put(1, pid);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTA01Value", "sql=" + sql);
		String rsString = sqlHelper.executeQuery(sql,para,null);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTA01Value", "rsString=" + rsString);
		returnString = rsString;
		return returnString;
	}
	
	private String getTA04Value(String hospitalId, String userId, String pid, String bededt) throws Exception {
		// 스마트.입원약정서
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		sqlHelper = new SqlHelper(hospitalId);
		//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		// 2024.05.30 WOOIL - 6자리 의사ID를 7자리로 변경하기 위한 작업
		//                    7자리여야 사인을 가져올 수 있다.
		String userDrId = "";
		if(userId.toUpperCase().startsWith("AA")){
			userDrId = userId;
			sql = "";
			sql += "select drid from ta07 where drid like '" + userId + "%' order by drid";
			String rsString = sqlHelper.executeQuery(sql, null, null);
			ResultSetHelper rs = new ResultSetHelper(rsString);
			int rsCount = rs.getRecordCount();
			if(rsCount>0) userDrId = rs.getString(0,  "drid");
		}
		
		//if(interfaceTableYn){
		//	sql =
		//	    "select a04.bed_in_date as bededt" +
		//	    "     , isnull(a04.bed_out_date,'') as bedodt" +
		//	    "     , (select a09.dept_name from dept_mast a09 where a09.dept_code=a04.dept_code) as dptnm" +
		//	    "     , (select a07.doctor_name from doctor_mast a07 where a07.doctor_id=a04.doctor_id) as drnm" +
		//	    "     , a04.ward_id+'-'+a04.room_id as ward" + // bed는 작성하지 않는다. 스마트병원이 이렇게 되어있음.
		//	    "     , (select top 1 t05.dise_name from in_pat_dise_hist t05 where t05.pat_id=a04.pat_id and t05.bed_in_date=a04.bed_in_date and t05.dise_no='1') as dxd" +
		//	    "     , isnull(a56.insu_address,'') as maddr" +
		//	    "     , isnull(a56.insu_name,'') as insnm" +
		//	    "     , isnull(a56.family_code,'') as famrelcd" +
		//	    "     , (select a88.insu_class_name from insu_class_mast a88 where a88.insu_class=a04.insu_class) as qfynm" +
		//	    "     , case when isnull(a56.insu_jumin_no,'')='' then '' else substring(a56.insu_jumin_no,1,6)+'-'+substring(a56.insu_jumin_no,7,1)+'******' end as p_resid" +
		//	    "     , 'sign_'+a04.doctor_id as drsign" +
		//	    "  from in_pat_visit_hist a04 with (nolock) inner join pat_mast a01 with (nolock) on a01.pat_id=a04.pat_id" +
		//	    "                                           left join pat_insu_hist a56 with (nolock) on a56.pat_id=a04.pat_id and a56.insu_class=a04.insu_class and a56.start_date=(select max(z.start_date) from pat_insu_hist z where z.pat_id=a56.pat_id and z.insu_class=a56.insu_class)" +
		//	    " where a04.pat_id=? and a04.bed_in_date=?";
		//}else{
			sql = "";
			sql += "select a04.bededt as bededt" + "\n";
			sql += "     , isnull(a04.bedodt,'') as bedodt" + "\n";
			sql += "     , a04.dptcd as dptcd" + "\n"; // 2026.01.27 WOOIL - 진료과코드
			sql += "     , (select a09.dptnm from ta09 a09 where a09.dptcd=a04.dptcd) as dptnm" + "\n";
			sql += "     , (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid) as drnm" + "\n";
			sql += "     , (select a07.drengnm from ta07 a07 where a07.drid=a04.pdrid) as drnm_eng" + "\n";
			sql += "     , case when '" + userId + "' like 'AA%'" + "\n";
			sql += "            then (select top 1 a94.usrnm from ta94 a94 where a94.usrid='" + userId + "')" + "\n";
			sql += "            else (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid)" + "\n";
			sql += "       end as logindrnm" + "\n";
			sql += "     , (select a07.gdrlcid from ta07 a07 where a07.drid=a04.pdrid) as gdrlcid" + "\n"; // 2025.08.06 WOOIL - 면허번호
			sql += "     , (select a07.sdrlcid from ta07 a07 where a07.drid=a04.pdrid) as sdrlcid" + "\n"; // 2025.08.06 WOOIL - 전문의 번호
			sql += "     , case when '" + userId + "' like 'AA%'" + "\n"; // 2025.08.06 WOOIL - 로그인의사 면허번호
			sql += "            then (select a07.gdrlcid from ta07 a07 where a07.drid='" + userDrId + "')" + "\n";
			sql += "            else (select a07.gdrlcid from ta07 a07 where a07.drid=a04.pdrid)" + "\n";
			sql += "       end as logingdrlcid" + "\n";
			sql += "     , case when '" + userId + "' like 'AA%'" + "\n"; // 2025.08.06 WOOIL - 로그인의사 전문의 번호
			sql += "            then (select a07.sdrlcid from ta07 a07 where a07.drid='" + userDrId + "')" + "\n";
			sql += "            else (select a07.sdrlcid from ta07 a07 where a07.drid=a04.pdrid)" + "\n";
			sql += "       end as loginsdrlcid" + "\n";
			sql += "     , a04.wardid+'-'+a04.rmid as ward" + "\n";// bed는 작성하지 않는다. 스마트병원이 이렇게 되어있음.
			sql += "     , (select a09.dptnm from ta09 a09 (nolock) where a09.dptcd=a04.wardid) as wardnm" + "\n"; // 2024.07.18 WOOIL - 병동명
			sql += "     , a04.rmid" + "\n"; // 2024.07.18 WOOIL - 병실ID 
			sql += "     , (select top 1 t05.dxd from tt05 t05 where t05.pid=a04.pid and t05.bdedt=a04.bededt order by convert(numeric,t05.ptysq),t05.seq) as dxd" + "\n";
			sql += "     , isnull(a56.maddr,'') as maddr" + "\n";
			sql += "     , isnull(a56.insnm,'') as insnm" + "\n";
			sql += "     , isnull(a56.famrelcd,'') as famrelcd" + "\n";
			sql += "     , (select a88.cdnm from ta88 a88 where a88.mst1cd='A'and mst2cd='26' and a88.mst3cd=a04.qlfycd) as qfynm" + "\n";
			sql += "     , case when isnull(a56.resid,'')='' then '' else substring(a56.resid,1,6)+'-'+substring(a56.resid,7,1)+'******' end as p_resid" + "\n";
			sql += "     , 'sign_'+a04.pdrid as drsign" + "\n";
			sql += "     , case when '" + userId + "' like 'AA%'" + "\n";
			sql += "            then 'logindrsign_" + userDrId + "'" + "\n";
			sql += "            else 'logindrsign_'+a04.pdrid" + "\n";
			sql += "       end as logindrsign" + "\n";
			sql += "  from ta04 a04 with (nolock) inner join ta01 a01 with (nolock) on a01.pid=a04.pid" + "\n";
			sql += "                              left join ta56 a56 with (nolock) on a56.pid=a04.pid and a56.qlfycd=a04.qlfycd and a56.credt=(select max(z.credt) from ta56 z where z.pid=a56.pid and z.qlfycd=a56.qlfycd)" + "\n";
			sql += " where a04.pid=? and a04.bededt=?";
		//}
		para.put(1, pid);    paraType.put(1, "C");
		para.put(2, bededt); paraType.put(2, "D");
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTA04Value", "sql=" + sql);
		String rsString = sqlHelper.executeQuery(sql, para, paraType);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTA04Value", "rsString=" + rsString);
		returnString = rsString;
		return returnString;
	}
	
	private String getTS21Value(String hospitalId, String userId, String pid, String exdt, String dptcd, String hms) throws Exception {
		// 스마트.입원약정서
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		// 2024.05.30 WOOIL - 6자리 의사ID를 7자리로 변경하기 위한 작업
		//                    7자리여야 사인을 가져올 수 있다.
		String userDrId = "";
		if(userId.toUpperCase().startsWith("AA")){
			userDrId = userId;
			sql = "";
			sql += "select drid from ta07 where drid like '" + userId + "%' order by drid";
			String rsString = sqlHelper.executeQuery(sql, null, null);
			ResultSetHelper rs = new ResultSetHelper(rsString);
			int rsCount = rs.getRecordCount();
			if(rsCount>0) userDrId = rs.getString(0,  "drid");
		}
		
		if(interfaceTableYn){
		}else{
			sql = "";
			sql += "select '' as bededt" + "\n";
			sql += "     , '' as bedodt" + "\n";
			sql += "     , s21.dptcd as dptcd" + "\n";
			sql += "     , (select a09.dptnm from ta09 a09 where a09.dptcd=s21.dptcd) as dptnm" + "\n";
			sql += "     , (select a07.drnm from ta07 a07 where a07.drid=s21.drid) as drnm" + "\n";
			sql += "     , (select a07.drengnm from ta07 a07 where a07.drid=s21.drid) as drnm_eng" + "\n"; // 2026.02.10 WOOIL - 의사영문명
			sql += "     , case when '" + userId + "' like 'AA%'" + "\n";
			sql += "            then (select top 1 a94.usrnm from ta94 a94 where a94.usrid='" + userId + "')" + "\n";
			sql += "            else (select a07.drnm from ta07 a07 where a07.drid=s21.drid)" + "\n";
			sql += "       end as logindrnm" + "\n";
			
			sql += "     , (select a07.gdrlcid from ta07 a07 where a07.drid=s21.drid) as gdrlcid" + "\n"; // 2025.08.06 WOOIL - 면허번호
			sql += "     , (select a07.sdrlcid from ta07 a07 where a07.drid=s21.drid) as sdrlcid" + "\n"; // 2025.08.06 WOOIL - 전문의 번호
			sql += "     , case when '" + userId + "' like 'AA%'" + "\n"; // 2025.08.06 WOOIL - 로그인의사 면허번호
			sql += "            then (select a07.gdrlcid from ta07 a07 where a07.drid='" + userDrId + "')" + "\n";
			sql += "            else (select a07.gdrlcid from ta07 a07 where a07.drid=s21.drid)" + "\n";
			sql += "       end as logingdrlcid" + "\n";
			sql += "     , case when '" + userId + "' like 'AA%'" + "\n"; // 2025.08.06 WOOIL - 로그인의사 전문의 번호
			sql += "            then (select a07.sdrlcid from ta07 a07 where a07.drid='" + userDrId + "')" + "\n";
			sql += "            else (select a07.sdrlcid from ta07 a07 where a07.drid=s21.drid)" + "\n";
			sql += "       end as loginsdrlcid" + "\n";
			
			sql += "     , '' as ward" + "\n"; // bed는 작성하지 않는다. 스마트병원이 이렇게 되어있음.
			sql += "     , '' as wardnm" + "\n";
			sql += "     , '' as rmid" + "\n";
			sql += "     , (select top 1 s06.dxd from ts06 s06 where s06.pid=s21.pid and s06.exdt=s21.exdt and s06.dptcd=s21.dptcd order by convert(numeric,s06.ptysq),s06.seq) as dxd" + "\n";
			sql += "     , isnull(a56.maddr,'') as maddr" + "\n";
			sql += "     , isnull(a56.insnm,'') as insnm" + "\n";
			sql += "     , isnull(a56.famrelcd,'') as famrelcd" + "\n";
			sql += "     , (select a88.cdnm from ta88 a88 where a88.mst1cd='A'and mst2cd='26' and a88.mst3cd=s21.qfycd) as qfynm" + "\n";
			sql += "     , case when isnull(a56.resid,'')='' then '' else substring(a56.resid,1,6)+'-'+substring(a56.resid,7,1)+'******' end as p_resid" + "\n";
			sql += "     , 'sign_'+s21.drid as drsign" + "\n";
			sql += "     , case when '" + userId + "' like 'AA%'" + "\n";
			sql += "            then 'logindrsign_" + userDrId + "'" + "\n";
			sql += "            else 'logindrsign_'+s21.drid" + "\n";
			sql += "       end as logindrsign" + "\n";
			sql += "  from ts21 s21 with (nolock) inner join ta01 a01 with (nolock) on a01.pid=s21.pid" + "\n";
			sql += "                              left join ta56 a56 with (nolock) on a56.pid=s21.pid and a56.qlfycd=s21.qfycd and a56.credt=(select max(z.credt) from ta56 z where z.pid=a56.pid and z.qlfycd=a56.qlfycd)" + "\n";
			sql += " where s21.pid=? and s21.exdt=? and s21.dptcd=? and isnull(s21.ccfg,'') in ('','0')" + "\n";
			if(!"".equalsIgnoreCase(hms)){
				sql +=
				   "   and s21.hms=?" + "\n";
			}
		}
		para.put(1, pid);   paraType.put(1, "C");
		para.put(2, exdt);  paraType.put(2, "D");
		para.put(3, dptcd); paraType.put(3, "C");
		if(!"".equalsIgnoreCase(hms)){
			para.put(4, hms); paraType.put(4, "C");
		}

		String rsString = sqlHelper.executeQuery(sql, para, paraType);

		returnString = rsString;
		return returnString;
	}
	
	private String getTU01Value(String hospitalId, String pid, String bededt) throws Exception {
		// 스마트.수술동의서
		// 수술명을 가져온다.
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		//String emrDateFormat = sqlHelper.getEmrDateFormat();
		//if("yyyy-mm-dd".equalsIgnoreCase(emrDateFormat)){
		//	bededt = Utility.getFormattedDate(bededt, emrDateFormat);
		//}
		
		String sql = "";
		//if(interfaceTableYn){
		//	sql =
		//	    "select top 1 u01.op_date,u01.op_name,'' rsvdacd" +
		//	    "  from pat_op_hist u01" +
		//	    " where u01.pat_id=?" +
		//	    "   and isnull(u01.cancel_flag,'')='0'" +
		//	    " order by op_date desc";
		//}else{
		    // 2023.10.19 WOOIL - 진주바른에서 당일 수술 두 건을 하는 경우 두 번째 수술명도 보여달아는 요청사항 처리.
			sql = "";
			sql += "select top 2 u01.opdt rsvopdt,u01.rsvop,u01.dacd rsvdacd" + "\n";
			sql += "     , (select a09.dptnm from ta09 a09 where a09.dptcd=u01.dptcd) as rsvopdptnm" + "\n";
			sql += "     , (select a07.drnm from ta07 a07 where a07.drid=u01.drid) as rsvopdrnm" + "\n";
			sql += "     , substring(opdt,1,4)+'년'+substring(opdt,5,2)+'월'+substring(opdt,7,2)+'일' as rsvopdt_ymd" + "\n";
			sql += "     , u01.rsvop as rsvop_2nd" + "\n";
			sql += "     , u01.dacd as rsvdacd_2nd" + "\n";
			sql += "  from tu01 u01" + "\n";
			sql += " where u01.pid=?" + "\n";
			sql += "   and isnull(u01.chgdt,'')=''" + "\n";
			sql += "   and u01.opdt=(select max(x.opdt) from tu01 x where x.pid=u01.pid and isnull(x.chgdt,'')='')" + "\n";
			sql += " order by u01.pid,u01.opdt,u01.dptcd,u01.opseq,u01.seq" + "\n";
		//}
		para.put(1, pid);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTU01Value", "sql=" + sql);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTU01Value", "rsString=" + rsString);
		returnString = rsString;
		return returnString;
	}
	
	private String getTU01Value(String hospitalId, String pid, String opdt, String dptcd, String opseq, String seq) throws Exception {
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		String sql = "";
		sql =
			"select top 1 u01.opdt rsvopdt,u01.rsvop,u01.dacd rsvdacd" +
			"     , (select a09.dptnm from ta09 a09 where a09.dptcd=u01.dptcd) as rsvopdptnm" +
		    "     , (select a07.drnm from ta07 a07 where a07.drid=u01.drid) as rsvopdrnm" +
			"     , substring(opdt,1,4)+'년'+substring(opdt,5,2)+'월'+substring(opdt,7,2)+'일' as rsvopdt_ymd" +
			"  from tu01 u01" +
			" where u01.pid=?" +
			"   and u01.opdt=?" +
			"   and u01.dptcd=?" +
			"   and u01.opseq=?" +
			"   and u01.seq=?" +
			"";
		para.put(1, pid);
		para.put(2, opdt);
		para.put(3, dptcd);
		para.put(4, opseq);
		para.put(5, seq);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		returnString = rsString;
		return returnString;		
	}
	
	private String getTK71IVValue(String hospitalId, String pid) throws Exception {
		// 나무병원 문진
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		
		String sql = "";
		sql =
		    "select isnull(disetc1,'')  as disetc1" + 
			"     , isnull(disetc2,'')  as disetc2 " + 
			"     , isnull(disetc3,'')  as disetc3 " + 
			"     , isnull(disetc4,'')  as disetc4 " + 
			"     , isnull(disetc5,'')  as disetc5 " + 
			"     , isnull(disetc6,'')  as disetc6 " + 
			"     , isnull(disetc7,'')  as disetc7 " + 
			"     , isnull(disetc8,'')  as disetc8 " + 
			"     , isnull(disetc9,'')  as disetc9 " + 
			"     , isnull(disetc10,'') as disetc10" + 
			"     , isnull(disetc11,'') as disetc11" + 
			"     , isnull(disetc12,'') as disetc12" + 
			"     , case when isnull(opration,'')='1' then 'V' else '' end as opration" + 
			"     , isnull(opretc,'')   as opretc  " + 
			"     , isnull(medetc1,'')  as medetc1 " + 
			"     , isnull(medetc2,'')  as medetc2 " + 
			"     , isnull(medetc3,'')  as medetc3 " + 
			"     , isnull(specetc1,'') as specetc1" + 
			"     , isnull(specetc5,'') as specetc5" + 
			"     , case when isnull(dischk1,'')='1' then 'V' else '' end  as dischk1 " + 
			"     , case when isnull(dischk2,'')='1' then 'V' else '' end  as dischk2 " + 
			"     , case when isnull(dischk3,'')='1' then 'V' else '' end  as dischk3 " + 
			"     , case when isnull(dischk4,'')='1' then 'V' else '' end  as dischk4 " + 
			"     , case when isnull(dischk5,'')='1' then 'V' else '' end  as dischk5 " + 
			"     , case when isnull(dischk6,'')='1' then 'V' else '' end  as dischk6 " + 
			"     , case when isnull(dischk7,'')='1' then 'V' else '' end  as dischk7 " + 
			"     , case when isnull(dischk8,'')='1' then 'V' else '' end  as dischk8 " + 
			"     , case when isnull(dischk9,'')='1' then 'V' else '' end  as dischk9 " + 
			"     , case when isnull(dischk10,'')='1' then 'V' else '' end as dischk10" + 
			"     , case when isnull(dischk11,'')='1' then 'V' else '' end as dischk11" + 
			"     , case when isnull(dischk12,'')='1' then 'V' else '' end as dischk12" + 
			"     , case when isnull(medchk1,'')='1' then 'V' else '' end  as medchk1 " + 
			"     , case when isnull(medchk2,'')='1' then 'V' else '' end  as medchk2 " + 
			"     , case when isnull(medchk3,'')='1' then 'V' else '' end  as medchk3 " + 
			"     , case when isnull(specchk1,'')='1' then 'V' else '' end as specchk1" + 
			"     , case when isnull(specchk2,'')='1' then 'V' else '' end as specchk2" + 
			"     , case when isnull(specchk3,'')='1' then 'V' else '' end as specchk3" + 
			"     , case when isnull(specchk5,'')='1' then 'V' else '' end as specchk5" +
			"     , case when isnull(nonmed,'')='1' then 'V' else '' end as nonmed" +
			"     , isnull(stopdt,'') as stopdt" +
			"  from tk71iv a" +
			" where a.pid=?" +
			"   and isnull(a.cancel,'')=''" +
			"   and a.seq=(select max(x.seq) from tk71iv x where x.pid=a.pid)" +
			"";
		para.put(1, pid);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTK71IVValue", "sql=" + sql);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTK71IVValue", "rsString=" + rsString);
		returnString = rsString;
		return returnString;
	}
	
	private String getTQ03NamuValue(String hospitalId, String pid) throws Exception {
		// 내시경 예약일자를 가져온다.
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = 
		    "select max(substring(rsvdt,1,4)+'년' + substring(rsvdt,5,2) + '월' + substring(rsvdt,7,2) + '일') namu_rsvdt" +
		    "  from tq03 q03" +
		    " where q03.pid=?" +
		    "   and q03.bdiv in ('1','2','3')" +
		    "   and q03.odt<=convert(varchar,getdate(),112)" +
		    "   and q03.rsvdt>=convert(varchar,getdate(),112)" +
		    "   and q03.exdptcd='SPE'" +
		    "   and q03.exrmid in ('Q10')";
		para.put(1, pid);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		returnString = rsString;
		return returnString;
	}
	
	private String getTK91Value1591(String hospitalId, String pid) throws Exception {
		// 백두병원 마취과 special permission
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		
		String sql = "";
		sql =
			"select b.keyvalue, b.text " +
			"  from tk92 a inner join tk91_value b on b.pid=a.pid and b.fbcode=a.fbcode and b.seq=a.seq" +
			" where a.pid=?" +
			"   and a.fbcode='1591'" +
			"   and a.seq=(select max(x.seq) from tk92 x where x.pid=a.pid and x.fbcode=a.fbcode)" +
			"";
		para.put(1, pid);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTK91Value1591", "sql=" + sql);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getTK91Value1591", "rsString=" + rsString);
		returnString="";
		ResultSetHelper rs = new ResultSetHelper(rsString, false);
		if(rs.getRecordCount()>0){
			columns = new JSONObject();
			columns.put("return_code",1);
			columns.put("return_desc","ok");
			status.add(columns);
			//
			columns = new JSONObject();
			for(int i=0;i<rs.getRecordCount();i++){
				String key = rs.getString(i, "keyvalue");
				String val = rs.getString(i, "text");
				if("5".equalsIgnoreCase(key) ||"8".equalsIgnoreCase(key) ||"22".equalsIgnoreCase(key)||"23".equalsIgnoreCase(key)||
				   "11".equalsIgnoreCase(key)||"14".equalsIgnoreCase(key)||"15".equalsIgnoreCase(key)||"24".equalsIgnoreCase(key))
				{
					// 환자성명 DKPNM,5
					// 의사명 DKDRNM,8
					// 성별 DKPSEX,22
					// 나이 DKPAGE,23
					// 수술일 DF_OPDT,11
					// 수술명 DF_OPNM,14
					// 내용 ___,15
					// 주치의 DKDRNM,24
					columns.put("bd_1591_" + key, val);
				}
			}
			rowData.add(columns);
		}else{
			columns = new JSONObject();
			columns.put("return_code",0);
			columns.put("return_desc","ok");
			status.add(columns);
		}
		// 반환자료
		result.add(status);
		result.add(rowData);
		
		return result.toJSONString();
	}
	
	private String getEmr082_2Value(String hospitalId, String pid) throws Exception {
		// 2023.01.02 WOOIL - 백두병원 마취전 환자 평가서
		SqlHelper sqlHelper;
		String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		//boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = 
			"select top 1 " +
		    "       a.bededt as bd2_bededt, a.opdt as bd2_opdt, a.seq as bd2_seq, a.opnm as bd2_opnm, a.dxnm as bd2_dxnm" +
			"     , dbo.mfn_piece(a.preop,char(21),1) as bd2_preop1" +
			"     , dbo.mfn_piece(a.preop,char(21),2) as bd2_preop2" +
			"     , dbo.mfn_piece(a.preop,char(21),3) as bd2_preop3" +
			"     , dbo.mfn_piece(a.preop,char(21),4) as bd2_preop4" +
			"     , dbo.mfn_piece(a.preop,char(21),5) as bd2_preop5" +
			"     , dbo.mfn_piece(a.preop,char(21),6) as bd2_preop6" +
			"     , dbo.mfn_piece(a.preop,char(21),7) as bd2_preop7" +
			"     , a.rh as bd2_rh, a.gumsa1 as bd2_gumsa1, a.gumsa2 as bd2_gumsa2, a.gumsa3 as bd2_gumsa3, a.gumsa4 as bd2_gumsa4" +
			"     , a.gumsa5 as bd2_gumsa5, a.gumsa6 as bd2_gumsa6, a.gumsa7 as bd2_gumsa7, a.gumsa8 as bd2_gumsa8, a.gumsa9 as bd2_gumsa9" +
			"     , a.gumsa10 as bd2_gumsa10, a.gumsa11 as bd2_gumsa11, a.gumsa12 as bd2_gumsa12, a.gumsa13 as bd2_gumsa13" +
			"     , case when dbo.mfn_piece(a.dise,char(21),1)='1' then 'V' else '' end as bd2_dise1" +
			"     , case when dbo.mfn_piece(a.dise,char(21),2)='1' then 'V' else '' end as bd2_dise2" +
			"     , case when dbo.mfn_piece(a.dise,char(21),3)='1' then 'V' else '' end as bd2_dise3" +
			"     , case when dbo.mfn_piece(a.dise,char(21),4)='1' then 'V' else '' end as bd2_dise4" +
			"     , case when dbo.mfn_piece(a.dise,char(21),5)='1' then 'V' else '' end as bd2_dise5" +
			"     , case when dbo.mfn_piece(a.dise,char(21),6)='1' then 'V' else '' end as bd2_dise6" +
			"     , case when dbo.mfn_piece(a.dise,char(21),7)='1' then 'V' else '' end as bd2_dise7" +
			"     , a.disetxt as bd2_disetxt" +
			"     , case when isnull(a.ophis,'')='1' then 'V' else '' end as bd2_ophis" +
			"     , a.ophistxt as bd2_ophistxt, a.bigo as bd2_bigo,a.rptdt as bd2_rptdt" +
			"     , case when substring(isnull(a.asa,''),1,1)='1' then 'V' else '' end as bd2_asa1" +
			"     , case when substring(isnull(a.asa,''),2,1)='1' then 'V' else '' end as bd2_asa2" +
			"     , case when substring(isnull(a.asa,''),3,1)='1' then 'V' else '' end as bd2_asa3" +
			"     , case when substring(isnull(a.asa,''),4,1)='1' then 'V' else '' end as bd2_asa4" +
			"     , case when substring(isnull(a.asa,''),5,1)='1' then 'V' else '' end as bd2_asa5" +
			"     , case when substring(isnull(a.asa,''),6,1)='1' then 'V' else '' end as bd2_asa6" +
			"     , case when substring(isnull(a.aneplan,''),1,1)='1' then 'V' else '' end as bd2_aneplan1" +
			"     , case when substring(isnull(a.aneplan,''),2,1)='1' then 'V' else '' end as bd2_aneplan2" +
			"     , case when substring(isnull(a.aneplan,''),3,1)='1' then 'V' else '' end as bd2_aneplan3" +
			"     , case when substring(isnull(a.aneplan,''),4,1)='1' then 'V' else '' end as bd2_aneplan4" +
			"     , case when substring(isnull(a.aneplan,''),5,1)='1' then 'V' else '' end as bd2_aneplan5" +
			"     , case when substring(isnull(a.aneplan,''),6,1)='1' then 'V' else '' end as bd2_aneplan6" +
			"     , a.anedr as bd2_anedr" +
			"     , a.sysdt as bd2_sysdt, a.systm as bd2_systm, a.empid as bd2_empid" +
			"     , b.drnm as bd2_anedrnm, a.rpttm as bd2_rpttm, a.bdiv as bd2_bdiv, a.dise6 as bd2_dise8, a.reply as bd2_reply" +
			"     , a.aneplan6 as bd2_aneplan7, chknote as bd2_chknote, teeth as bd2_teeth, teethtxt as bd2_teethtxt, teethtxt2 as bd2_teethtxt2" +
			"     , a.agree as bd2_agree, a.bmi as bd2_bmi" +
			"     , a.ba_wdate as bd2_ba_wdate, a.ba_wtime as bd2_ba_wtime" +
			"     , a.ba_food as bd2_ba_food, a.ba_food_date as bd2_ba_food_date, a.ba_food_time as bd2_ba_food_time, a.ba_chg as bd2_ba_chg" +
			"     , a.gumsa14 as bd2_gumsa14, a.drug as bd2_drug" +
			"     , case when dbo.mfn_piece(a.drug_allergy,char(21),1)='1' then 'V' else ''end as bd2_drug_allergy1" +
			"     , case when dbo.mfn_piece(a.drug_allergy,char(21),2)='1' then 'V' else ''end as bd2_drug_allergy2" +
			"     , case when dbo.mfn_piece(a.medication,char(21),1)='1' then 'V' else '' end as bd2_medication1" +
			"     , case when dbo.mfn_piece(a.medication,char(21),2)='1' then 'V' else '' end as bd2_medication2" +
			"     , a.drug_his as bd2_drug_his" +
			"     , case when dbo.mfn_piece(a.neck_ex,char(21),1)='1' then 'V' else '' end as bd2_neck_ex1" +
			"     , case when dbo.mfn_piece(a.neck_ex,char(21),2)='1' then 'V' else '' end as bd2_neck_ex2" +
			"     , case when dbo.mfn_piece(a.mouth,char(21),1)='1' then 'V' else '' end as bd2_mouth1" +
			"     , case when dbo.mfn_piece(a.mouth,char(21),2)='1' then 'V' else '' end as bd2_mouth2" +
			"     , case when dbo.mfn_piece(a.teeth_ex,char(21),1)='1' then 'V' else '' end as bd2_teeth_ex1" +
			"     , case when dbo.mfn_piece(a.teeth_ex,char(21),2)='1' then 'V' else '' end as bd2_teeth_ex2" +
			"     , a.neck as bd2_neck" +
			"     , case when dbo.mfn_piece(a.alcoh,char(21),1)='1' then 'V' else '' end as bd2_alcoh1" +
			"     , case when dbo.mfn_piece(a.alcoh,char(21),2)='1' then 'V' else '' end as bd2_alcoh2" +
			"     , dbo.mfn_piece(a.alcoh,char(21),3) as bd2_alcoh3" +
			"     , case when dbo.mfn_piece(a.smoke,char(21),1)='1' then 'V' else '' end as bd2_smoke1" +
			"     , case when dbo.mfn_piece(a.smoke,char(21),2)='1' then 'V' else '' end as bd2_smoke2" +
			"     , dbo.mfn_piece(a.smoke,char(21),3) as bd2_smoke3" +
			"     , case when dbo.mfn_piece(a.mallampati,char(21),1)='1' then 'V' else '' end as bd2_mallampati1" +
			"     , case when dbo.mfn_piece(a.mallampati,char(21),2)='1' then 'V' else '' end as bd2_mallampati2" +
			"     , case when dbo.mfn_piece(a.mallampati,char(21),3)='1' then 'V' else '' end as bd2_mallampati3" +
			"     , case when dbo.mfn_piece(a.mallampati,char(21),4)='1' then 'V' else '' end as bd2_mallampati4" +
			"  from emr082_2 a left outer join ta07 b on b.drid = a.anedr" +
			" where a.pid = ? " +
			" order by a.bededt desc,a.opdt desc,a.seq desc"; 
		
		para.put(1, pid);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		returnString = rsString;
		return returnString;
	}
	
	private String getBiDongValue(String hospitalId, String pid, String bededt, String bdiv, String dptcd, String dongExdt) throws Exception {
		new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "pid=" + pid + ",bededt=" + bededt + ",bdiv=" + bdiv + ",dptcd=" + dptcd + ",dongExdt=" + dongExdt);
		
		int bi_index = 0;
		int[] bi_no = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		String[] bi_onm = {"","","","","","","","","","","","","","",""};
		int[] bi_gumak = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		int bi_gumak_tot = 0;
		
		SqlHelper sqlHelper;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);

		String calc_fg = "";
		
		String sql = "";
		sql = "";
		sql += "select fld2qty,fld3qty from ta88 (nolock) where mst1cd = 'A' and mst2cd = 'HOSPITAL' and mst3cd = '220'";
		String rsString = sqlHelper.executeQuery(sql, null, null);
		ResultSetHelper rsTA88 = new ResultSetHelper(rsString, false);
		if(rsTA88.getRecordCount()>0){
			calc_fg = rsTA88.getString(0, "fld2qty");
		}
		
		String bthdt = "";
		
		sql = "";
		sql += "select bthdt from ta01 where pid=?";
		para.put(1, pid);
		rsString = sqlHelper.executeQuery(sql, para, null);
		ResultSetHelper rsTA01 = new ResultSetHelper(rsString, false);
		if(rsTA01.getRecordCount()>0){
			bthdt = rsTA01.getString(0, "bthdt");
		}
		
		if("2".equalsIgnoreCase(bdiv)){
			sql = "";
			sql += "select e01.odt, e01.odivcd" + "\n";
			sql += "     , e01a.ocd, e01a.alwfg, isnull(e01a.oqty,0) oqty, isnull(e01a.ordcnt,0) as ordcnt, isnull(e01a.odaycnt,0) as odaycnt, e01a.fldcd12" + "\n";
			sql += "     , a18.pricd, a18.onm" + "\n";
			sql += "  from tv01 e01 inner join tv01a e01a on e01a.hdid=e01.hdid" + "\n";
			sql += "                inner join ta18 a18 on a18.ocd=e01a.ocd" + "\n";
			sql += "                                   and a18.credt=(select max(x.credt)" + "\n";
			sql += "                                                    from ta18 x" + "\n";
			sql += "                                                   where x.ocd=e01a.ocd" + "\n";
			sql += "                                                     and x.credt<=e01.odt" + "\n";
			sql += "                                                 )" + "\n";
			sql += " where e01.pid=?" + "\n";
			sql += "   and e01.bededt=?" + "\n";
			if(!"".equalsIgnoreCase(dongExdt)){
				sql += "   and e01.odt=?" + "\n";
			}
			sql += "   and e01.ono<9000" + "\n";
			sql += "   and isnull(e01.usecd,'')<>'5'" + "\n";
			sql += "   and isnull(e01.odivcd,'')<>''" + "\n";
			sql += "   and isnull(e01.dcfg,'')<>'1'" + "\n";
			sql += "   and e01.odivcd not like 'H%'" + "\n";
			sql += "   and a18.pricd in (select a88.fld1qty from ta88 a88 where a88.mst1cd='A' and a88.mst2cd='NBFCHK')" + "\n"; // 대상이 되는 코드만
			sql += " order by e01.odt desc, e01.ono" + "\n";
			
			para.clear();
			para.put(1, pid);
			para.put(2, bededt);
			if(!"".equalsIgnoreCase(dongExdt)){
				para.put(3, dongExdt);
			}
		}else{
			sql = "";
			sql += "select e01.odt, e01.odivcd" + "\n";
			sql += "     , e01a.ocd, e01a.alwfg, isnull(e01a.oqty,0) oqty, isnull(e01a.ordcnt,0) as ordcnt, isnull(e01a.odaycnt,0) as odaycnt, e01a.fldcd12" + "\n";
			sql += "     , a18.pricd, a18.onm" + "\n";
			sql += "  from te01 e01 inner join te01a e01a on e01a.hdid=e01.hdid" + "\n";
			sql += "                inner join ta18 a18 on a18.ocd=e01a.ocd" + "\n";
			sql += "                                   and a18.credt=(select max(x.credt)" + "\n";
			sql += "                                                    from ta18 x" + "\n";
			sql += "                                                   where x.ocd=e01a.ocd" + "\n";
			sql += "                                                     and x.credt<=e01.odt" + "\n";
			sql += "                                                 )" + "\n";
			sql += " where e01.pid=?" + "\n";
			sql += "   and e01.odt=?" + "\n";
			sql += "   and e01.dptcd=? " + "\n";
			sql += "   and isnull(e01.dcfg,'')<>'1'" + "\n";
			sql += "   and e01.odivcd not like 'H%'" + "\n";
			sql += "   and a18.pricd in (select a88.fld1qty from ta88 a88 where a88.mst1cd='A' and a88.mst2cd='NBFCHK')" + "\n"; // 대상이 되는 코드만
			sql += " order by e01.odt, e01.ono" + "\n";
			
			para.clear();
			para.put(1, pid);
			para.put(2, bededt);
			para.put(3, dptcd);
		}
		
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "sql=" + sql);
		rsString = sqlHelper.executeQuery(sql, para, null);
		ResultSetHelper rsTE01 = new ResultSetHelper(rsString, false);
		if(rsTE01.getRecordCount()>0){
			for(int e01_idx=0;e01_idx<rsTE01.getRecordCount();e01_idx++){
				String e01_ocd = rsTE01.getString(e01_idx, "ocd");
				String e01_odt = rsTE01.getString(e01_idx, "odt");
				//new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "bthdt=" + rsTE01.getString(e01_idx, "bthdt") + ",odt=" + rsTE01.getString(e01_idx, "odt"));
				int age = 0;
				int age_mm = 0;
				int age_dd = 0;
				String age_ymd = Utility.getAgeYMD(bthdt, e01_odt);
				//new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "age_ymd=" + age_ymd);
				if("".equalsIgnoreCase(age_ymd)) age_ymd = "00200101";
				try{
					age = Integer.parseInt(age_ymd.substring(0, 4));
					age_mm = Integer.parseInt(age_ymd.substring(4, 6));
					age_dd = Integer.parseInt(age_ymd.substring(6, 8));
				}catch(Exception ex){
					age = 20;
					age_mm = 1;
					age_dd = 1;
				}

				boolean babyFg = (age==0 && age_mm==0 && age_dd<=28) ? true : false;
				
				//new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "age=" + age + ",age_mm=" + age_mm + ",age_dd=" + age_dd + ",babyFg=" + babyFg);
			
				String e01_odivcd = rsTE01.getString(e01_idx, "odivcd");
				String e01_fldcd12 = rsTE01.getString(e01_idx, "fldcd12");
				String e01_alwfg = rsTE01.getString(e01_idx, "alwfg");
				double e01_oqty = rsTE01.getDouble(e01_idx, "oqty");
				double e01_ordcnt = rsTE01.getDouble(e01_idx, "ordcnt");
				double e01_odaycnt = rsTE01.getDouble(e01_idx, "odaycnt");
				String a18_pricd = rsTE01.getString(e01_idx, "pricd");
				String a18_onm = rsTE01.getString(e01_idx, "onm");
				
				double tot_qty = 0;
				if("1".equalsIgnoreCase(bdiv)){
					// 외래
					if(e01_oqty==0) e01_oqty=1;
					if(e01_ordcnt==0) e01_ordcnt=1;
					if(e01_odaycnt==0) e01_odaycnt=1;
					
					if(e01_odivcd.startsWith("L")){
						// 진단검사
						tot_qty = e01_ordcnt * e01_odaycnt;
					}else if(e01_odivcd.startsWith("O")){
						// 물리치료
						tot_qty = e01_ordcnt * e01_odaycnt;
					}else if(e01_odivcd.startsWith("T")){
						// 처치 - 특정처치코드는 oqty를 곱해주는데 지금은 무시한다.
						tot_qty = e01_ordcnt * e01_odaycnt;
					}else if(e01_odivcd.startsWith("R")){
						// 방사선
						if("NEA".equalsIgnoreCase(e01_fldcd12)){
							tot_qty = 0.5 * e01_ordcnt * e01_odaycnt;
						}else{
							tot_qty = e01_ordcnt * e01_odaycnt;
						}
					}else if(e01_odivcd.startsWith("M")){
						String calUnit = getCalUnit(hospitalId, e01_ocd, e01_odt);
						double dMul = 0;
						if("".equalsIgnoreCase(calUnit)) dMul = 1.0; // 총량계산
						if("Y".equalsIgnoreCase(calUnit)) dMul = 1.0; // 총량계산
						if("N".equalsIgnoreCase(calUnit)) dMul = 10.0; // 각량계산
						if("X".equalsIgnoreCase(calUnit)) dMul = 100.0; // 각량계산
						if("T".equalsIgnoreCase(calUnit)) dMul = 1000.0; // 각량계산
						if("U".equalsIgnoreCase(calUnit)) dMul = 10000.0; // 각량계산
						if("Z".equalsIgnoreCase(calUnit)) dMul = 2.0; // 0.5 단위
						if(dMul!=0){
							int iSign = e01_oqty < 0 ? -1 : 1;
							e01_oqty = iSign * e01_oqty;
							e01_oqty = (double)((int)Math.ceil(e01_oqty * dMul)) / dMul;
							e01_oqty = iSign * e01_oqty;
							tot_qty = e01_oqty * e01_odaycnt;
						}
					}else{
						// 기본
						tot_qty = e01_oqty * e01_odaycnt;
					}
				}else{
					// 입원
					if(e01_odivcd.startsWith("M")&&e01_odaycnt==0) continue; // 데일리컨펌 제외
					
					if(e01_oqty==0) e01_oqty=1;
					if(e01_ordcnt==0) e01_ordcnt=1;
					if(e01_odaycnt==0) e01_odaycnt=1;
					
					if(e01_odivcd.startsWith("L")){
						// 진단검사
						tot_qty = e01_ordcnt * e01_odaycnt;
					}else if(e01_odivcd.startsWith("O")){
						// 물리치료
						tot_qty = e01_ordcnt * e01_odaycnt;
					}else if(e01_odivcd.startsWith("T")){
						// 처치
						if("SUB".equalsIgnoreCase(e01_fldcd12)){
							tot_qty = 0.5 * e01_ordcnt * e01_odaycnt;
						}else if("SAM".equalsIgnoreCase(e01_fldcd12)){
							tot_qty = 0.7 * e01_ordcnt * e01_odaycnt;
						}else{
							tot_qty = e01_ordcnt * e01_odaycnt;
						}
					}else if(e01_odivcd.startsWith("R")){
						// 방사선
						if("NEA".equalsIgnoreCase(e01_fldcd12)){
							tot_qty = 0.5 * e01_ordcnt * e01_odaycnt;
						}else{
							tot_qty = e01_ordcnt * e01_odaycnt;
						}
					}else if(e01_odivcd.startsWith("M")){
						//new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "e01_ocd=" + e01_ocd + ",e01_odt=" + e01_odt);
						String calUnit = getCalUnit(hospitalId, e01_ocd, e01_odt);
						//new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "calUnit=" + calUnit);
						double dMul = 0;
						if("".equalsIgnoreCase(calUnit)) dMul = 1.0; // 총량계산
						if("Y".equalsIgnoreCase(calUnit)) dMul = 1.0; // 총량계산
						if("N".equalsIgnoreCase(calUnit)) dMul = 10.0; // 각량계산
						if("X".equalsIgnoreCase(calUnit)) dMul = 100.0; // 각량계산
						if("T".equalsIgnoreCase(calUnit)) dMul = 1000.0; // 각량계산
						if("U".equalsIgnoreCase(calUnit)) dMul = 10000.0; // 각량계산
						if("Z".equalsIgnoreCase(calUnit)) dMul = 2.0; // 0.5 단위
						//new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "dMul=" + dMul);
						if(dMul!=0){
							int iSign = e01_oqty < 0 ? -1 : 1;
							e01_oqty = iSign * e01_oqty;
							e01_oqty = (double)((int)Math.ceil(e01_oqty * dMul)) / dMul;
							e01_oqty = iSign * e01_oqty;
							tot_qty = e01_oqty * e01_odaycnt;
						}
					}else{
						// 기본
						tot_qty = e01_oqty * e01_odaycnt;
					}
				}
				
				//new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "a18_pricd=" + a18_pricd);
				
				if(a18_pricd.startsWith("X")==false){
					// 단일수가
					sql = "";
					sql += "select a02.ialwf, isnull(a02.ipamt,0) ipamt, isnull(a02.gpamt,0) gpamt" + "\n";
					sql += "  from ta02 a02" + "\n";
					sql += " where a02.pricd=?" + "\n";
					sql += "   and a02.credt=(select max(x.credt) from ta02 x where x.pricd=a02.pricd and x.credt<=?)" + "\n";
					para.clear();
					para.put(1, a18_pricd);
					para.put(2, e01_odt);
					rsString = sqlHelper.executeQuery(sql, para, null);
					ResultSetHelper rsTA02 = new ResultSetHelper(rsString, false);
					if(rsTA02.getRecordCount()>0){
						String a02_alwfg = rsTA02.getString(0, "ialwf");
						String chrlt = getChrlt(e01_alwfg, a02_alwfg, calc_fg);
						int gumak = 0;
						if("4".equalsIgnoreCase(chrlt)) gumak = rsTA02.getInt(0, "gpamt");
						else if("1".equalsIgnoreCase(chrlt)) gumak = rsTA02.getInt(0, "ipamt");
						if(gumak>0){
							bi_index++;
							if(bi_index<=15){
								bi_no[bi_index-1] = bi_index; // int to string
								bi_onm[bi_index-1] = a18_onm;
								bi_gumak[bi_index-1] = (int)(gumak*tot_qty);
								bi_gumak_tot += (int)(gumak*tot_qty);
							}
						}
						new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "    e01_odt="+e01_odt+",gumak="+gumak+",e01_alwfg="+e01_alwfg+",a02_alwfg="+a02_alwfg+",chrlt="+chrlt+",tot_qty=" +tot_qty);
					}
				}else{
					// 그룹수가 TA02A->TA02
					int grp_gumak = 0;
					sql = "";
					sql += "select a02a.spcd, isnull(a02a.dqty,0) dqty, a02a.awlfg, a02a.agedv" + "\n";
					sql += "  from ta02a a02a" + "\n";
					sql += " where a02a.pricd=?" + "\n";
					sql += "   and a02a.credt=(select max(x.credt) from ta02a x where x.pricd=a02a.pricd and x.credt<=?)" + "\n";
					
					para.clear();
					para.put(1, a18_pricd);
					para.put(2, e01_odt);
					rsString = sqlHelper.executeQuery(sql, para, null);
					ResultSetHelper rsTA02A = new ResultSetHelper(rsString, false);
					if(rsTA02A.getRecordCount()>0){
						for(int a02a_idx=0;a02a_idx<rsTA02A.getRecordCount();a02a_idx++){
							String a02a_spcd = rsTA02A.getString(a02a_idx, "spcd");
							if("".equalsIgnoreCase(a02a_spcd)) continue;
							
							String a02a_alwfg = rsTA02A.getString(a02a_idx, "awlfg");
							String a02a_agedv = rsTA02A.getString(a02a_idx, "agedv");
							double a02a_dqty = rsTA02A.getDouble(a02a_idx, "dqty");
							
							if("".equalsIgnoreCase(a02a_agedv)) a02a_agedv="0";
							if(a02a_dqty==0) a02a_dqty=1;
							
							boolean bOK = false;
							if("0".equalsIgnoreCase(a02a_agedv)) bOK=true; // 공통
							if("1".equalsIgnoreCase(a02a_agedv) && age>=8) bOK = true;
							if("2".equalsIgnoreCase(a02a_agedv) && age<8) bOK = true;
							if("3".equalsIgnoreCase(a02a_agedv) && babyFg==true) bOK = true; //신생아(28일 이하)
							if("4".equalsIgnoreCase(a02a_agedv) && babyFg==false && age<1) bOK = true; // 28일 초과 만1세 미만
							if("5".equalsIgnoreCase(a02a_agedv) && age>=1 && age<6) bOK = true; //만1세 이상 만6세 미만
							if("6".equalsIgnoreCase(a02a_agedv) && age>=6) bOK = true; //만6세 이상
							
							if(bOK==false) continue;
							
							// 단일수가
							sql = "";
							sql += "select a02.ialwf, isnull(a02.ipamt,0) ipamt, isnull(a02.gpamt,0) gpamt" + "\n";
							sql += "  from ta02 a02" + "\n";
							sql += " where a02.pricd=?" + "\n";
							sql += "   and a02.credt=(select max(x.credt) from ta02 x where x.pricd=a02.pricd and x.credt<=?)" + "\n";
							para.clear();
							para.put(1, a02a_spcd);
							para.put(2, e01_odt);
							rsString = sqlHelper.executeQuery(sql, para, null);
							ResultSetHelper rsTA02 = new ResultSetHelper(rsString, false);
							if(rsTA02.getRecordCount()>0){
								String a02_alwfg = rsTA02.getString(0, "ialwf");
								String chrlt_grp = getAlwfg(a02a_alwfg, a02_alwfg, calc_fg);
								String chrlt = getChrlt(e01_alwfg, chrlt_grp, calc_fg);
								int gumak = 0;
								if("4".equalsIgnoreCase(chrlt)) gumak = rsTA02.getInt(0, "gpamt");
								else if("1".equalsIgnoreCase(chrlt)) gumak = rsTA02.getInt(0, "ipamt");
								if(gumak>0){
									grp_gumak += (int)(gumak*a02a_dqty*tot_qty);
								}
								new LogWrite().debugWrite(getClass().getSimpleName(), "getBiDongValue", "    a02a_spcd=" + a02a_spcd+",e01_odt="+e01_odt+",gumak="+gumak+",grp_gumak="+grp_gumak+",calc_fg="+calc_fg+",e01_alwfg="+e01_alwfg+",a02a_alwfg="+a02a_alwfg+",a02_alwfg="+a02_alwfg+",chrlt_grp="+chrlt_grp+",chrlt="+chrlt);
							}
						}
					}
					// 그룹 안에 있는 코드의 합계 금액을 작성한다.
					if(grp_gumak>0){
						bi_index++;
						if(bi_index<=15){
							bi_no[bi_index-1] = bi_index;
							bi_onm[bi_index-1] = a18_onm;
							bi_gumak[bi_index-1] = grp_gumak;
							bi_gumak_tot += grp_gumak;
						}
					}
				}
			}
		}
		
		DecimalFormat formatter = new DecimalFormat("###,###");
		
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		
		columns = new JSONObject();
		columns.put("return_code",1);
		columns.put("return_desc","ok");
		status.add(columns);
		//
		columns = new JSONObject();
		for(int i=0;i<15;i++){
			if(bi_no[i]>0){
				columns.put("bi_no_" + (i+1), String.format("%5s", bi_no[i]+""));
				columns.put("bi_onm_" + (i+1), bi_onm[i]);
				columns.put("bi_gumak_" + (i+1), String.format("%15s", formatter.format(bi_gumak[i])));
			}else{
				columns.put("bi_no_" + (i+1), ""); 
				columns.put("bi_onm_" + (i+1), "");
				columns.put("bi_gumak_" + (i+1), "");
			}
		}
		columns.put("bi_gumak_tot", String.format("%15s", formatter.format(bi_gumak_tot)));

		rowData.add(columns);
		
		result.add(status);
		result.add(rowData);
		
		return result.toJSONString();
	}
	
	private String getCalUnit(String hospitalId, String ocd, String exdt) throws Exception{
		SqlHelper sqlHelper;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		
		String sql = "";
		sql += "select a84.calunit" + "\n";
		sql += "  from ta84 a84" + "\n";
		sql += " where a84.drgcd=?" + "\n";
		sql += "   and a84.credt=(select max(x.credt) from ta84 x where x.drgcd=a84.drgcd and x.credt<=?)" + "\n";
		
		para.clear();
		para.put(1, ocd);
		para.put(2, exdt);
		
		String rsString = sqlHelper.executeQuery(sql, para, null);
		ResultSetHelper rsTA84 = new ResultSetHelper(rsString, false);
		String calUnit = "";
		if(rsTA84.getRecordCount()>0){
			calUnit = rsTA84.getString(0, "calunit");
		}
		return calUnit;
	}
	
	private String getChrlt(String e01_alwfg, String a02_alwfg, String calc_fg){
		if("2".equalsIgnoreCase(calc_fg)){
			return getChrlt_Compact(e01_alwfg, a02_alwfg);
		}else if("1".equalsIgnoreCase(calc_fg)){
			return getChrlt_New(e01_alwfg, a02_alwfg);
		}else{
			return getChrlt_Default(e01_alwfg, a02_alwfg);
		}
	}
	
	private String getChrlt_Compact(String e01_alwfg, String a02_alwfg){
		if("3".equalsIgnoreCase(e01_alwfg)) return "3";
		if("3".equalsIgnoreCase(a02_alwfg)) return "3";
		// 수가가 급여
		if("0".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "0"; // 급여    -> 급여
		if("1".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "D"; // 30/100  -> 30/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 30/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 급여    -> 30/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 30/100  -> 30/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 50/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 급여    -> 50/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 30/100  -> 50/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 80/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 급여    -> 80/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 50/100  -> 80/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 30/100  -> 80/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 90/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 급여    -> 90/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 50/100  -> 90/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 80/100  -> 90/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 30/100  -> 90/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 100/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 급여    -> 100/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 50/100  -> 100/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 80/100  -> 100/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 30/100  -> 100/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 90/100  -> 100/100
		// 수가가 비급여
		if("0".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 급여    -> 비보험
		if("1".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 비보험
		if("5".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 50/100  -> 비보험
		if("6".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 80/100  -> 비보험
		if("7".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 30/100  -> 비보험
		if("8".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 90/100  -> 비보험
		// 수가가 비보험
		if("0".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 급여    -> 비보험
		if("1".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 비보험
		if("5".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 50/100  -> 비보험
		if("6".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 80/100  -> 비보험
		if("7".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 30/100  -> 비보험
		if("8".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 90/100  -> 비보험
		return "4";
	}
	private String getChrlt_New(String e01_alwfg, String a02_alwfg){
		if("3".equalsIgnoreCase(e01_alwfg)) return "3";
		if("3".equalsIgnoreCase(a02_alwfg)) return "3";
		// 수가가 급여
		if("0".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "0"; // 급여    -> 급여
		if("1".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "D"; // 30/100  -> 30/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 30/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 급여    -> 30/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 30/100  -> 30/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 50/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 급여    -> 50/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 30/100  -> 50/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 80/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 급여    -> 80/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "5"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 50/100  -> 80/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 30/100  -> 80/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 90/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 급여    -> 90/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 50/100  -> 90/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 80/100  -> 90/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 30/100  -> 90/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 100/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 급여    -> 100/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 50/100  -> 100/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 80/100  -> 100/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 30/100  -> 100/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 90/100  -> 100/100
		// 수가가 비급여
		if("0".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 급여    -> 비급여
		if("1".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 100/100 -> 비급여
		if("5".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 50/100  -> 비급여
		if("6".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 80/100  -> 비급여
		if("7".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 30/100  -> 비급여
		if("8".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 90/100  -> 비급여
		// 수가가 비보험
		if("0".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 급여    -> 비보험
		if("1".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 비보험
		if("5".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 50/100  -> 비보험
		if("6".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 80/100  -> 비보험
		if("7".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 30/100  -> 비보험
		if("8".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 90/100  -> 비보험
		return "4";
	}
	private String getChrlt_Default(String e01_alwfg, String a02_alwfg){
		if("3".equalsIgnoreCase(e01_alwfg)) return "3";
		if("3".equalsIgnoreCase(a02_alwfg)) return "3";
		// 수가가 급여
		if("0".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "0"; // 급여    -> 급여
		if("1".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "D"; // 30/100  -> 30/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 비급여
		if("0".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 급여    -> 비급여
		if("1".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 비보험  -> 비급여
		if("4".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 100/100 -> 비급여
		if("5".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 50/100  -> 비급여
		if("6".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 80/100  -> 비급여
		if("7".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 30/100  -> 비급여
		if("8".equalsIgnoreCase(e01_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 90/100  -> 비급여
		// 수가가 비보험
		if("0".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 급여    -> 비보험
		if("1".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "D"; // 30/100  -> 30/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		// 수가가 100/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 급여    -> 100/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 비급여  -> 100/100
		if("2".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 비보험  -> 100/100
		if("4".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 50/100  -> 100/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 80/100  -> 100/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 30/100  -> 100/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 90/100  -> 100/100
		// 수가가 50/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 급여    -> 50/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 비급여  -> 50/100 기존(비급여) 2015.03.16
		if("2".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 비보험  -> 50/100 기존(비보험) 2016.09.08
		if("4".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 100/100 -> 50/100 기존(100/100)2016.09.08
		if("5".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 80/100  -> 50/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 30/100  -> 50/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 90/100  -> 50/100
		// 수가가 80/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 급여    -> 80/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 비급여  -> 80/100 기존(비급여) 2015.03.16
		if("2".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 비보험  -> 80/100 기존(비보험) 2016.09.08
		if("4".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 100/100 -> 80/100 기존(100/100)2016.09.08
		if("5".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 50/100  -> 80/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 30/100  -> 80/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 90/100  -> 80/100
		// 수가가 30/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 급여    -> 30/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 비급여  -> 30/100 기존(비급여) 2015.03.16
		if("2".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 비보험  -> 30/100 기존(비보험) 2016.09.08
		if("4".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 100/100 -> 30/100 기존(100/100)2016.09.08
		if("5".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 50/100  -> 30/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 80/100  -> 30/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 30/100  -> 30/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "D"; // 90/100  -> 30/100
		// 수가가 90/100
		if("0".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 급여    -> 90/100
		if("1".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 비급여  -> 90/100 기존(비급여) 2015.03.16
		if("2".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 비보험  -> 90/100 기존(비보험) 2016.09.08
		if("4".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 100/100 -> 90/100 기존(100/100)2016.09.08
		if("5".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 50/100  -> 90/100
		if("6".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 80/100  -> 90/100
		if("7".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 30/100  -> 90/100
		if("8".equalsIgnoreCase(e01_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "E"; // 90/100  -> 90/100
		return "4";
	}
	
	private String getAlwfg(String a02a_alwfg, String a02_alwfg, String calc_fg){
		if("2".equalsIgnoreCase(calc_fg)){
			return getAlwfg_Compact(a02a_alwfg, a02_alwfg);
		}else if("1".equalsIgnoreCase(calc_fg)){
			return getAlwfg_New(a02a_alwfg, a02_alwfg);
		}else{
			return getAlwfg_Default(a02a_alwfg, a02_alwfg);
		}
	}
	
	private String getAlwfg_Compact(String a02a_alwfg, String a02_alwfg){
		if("3".equalsIgnoreCase(a02a_alwfg)) return "3";
		if("3".equalsIgnoreCase(a02_alwfg)) return "3";
		// 그룹이 급여
		if("0".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "0";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "5";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "7";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8";
		// 그룹이 비급여
		if("1".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "1";
		// 그룹이 비보험
		if("2".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "2"; // 2017.02.13 WOOIL 4
		if("2".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "2";
		// 그룹이 100/100
		if("4".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "4";
		// 그룹이 50/100
		if("5".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 0
		if("5".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 2017.02.13 WOOIL 1
		if("5".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 2
		if("5".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 4
		if("5".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 5
		if("5".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 6
		if("5".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "5"; // 2018.03.13 By Moon 7
		if("5".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "5"; // 2018.03.13 By Moon 8
		// 그룹이 80/100
		if("6".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 0
		if("6".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 2017.02.13 WOOIL 1
		if("6".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 2
		if("6".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 4
		if("6".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 5
		if("6".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 6
		if("6".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "6"; // 2018.03.13 By Moon 7
		if("6".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "6"; // 2018.03.13 By Moon 8
		// 그룹이 30/100
		if("7".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 0
		if("7".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 2017.02.13 WOOIL 1
		if("7".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 2
		if("7".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 4
		if("7".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 5
		if("7".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 6
		if("7".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "7"; // 2018.03.13 By Moon 7
		if("7".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "7"; // 2018.03.13 By Moon 8
		// 그룹이 90/100
		if("8".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 0
		if("8".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 2017.02.13 WOOIL 1
		if("8".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 2
		if("8".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 4
		if("8".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 5
		if("8".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 6
		if("8".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "8"; // 2018.03.13 By Moon 7
		if("8".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8"; // 2018.03.13 By Moon 8
		return "2";
	}
	
	private String getAlwfg_New(String a02a_alwfg, String a02_alwfg){
		if("3".equalsIgnoreCase(a02a_alwfg)) return "3";
		if("3".equalsIgnoreCase(a02_alwfg)) return "3";
		// 싱글이 급여
		if("0".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "0"; // 급여    -> 급여    
		if("1".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여  
		if("2".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "2"; // 비보험  -> 비보험  
		if("4".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 100/100 
		if("5".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "5"; // 50/100  -> 50/100  
		if("6".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "6"; // 80/100  -> 80/100  
		if("7".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "7"; // 30/100  -> 30/100  
		if("8".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "8"; // 90/100  -> 90/100  
		// 싱글이 비급여
		if("0".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 급여    -> 비급여
		if("1".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "2"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 100/100 -> 비급여
		if("5".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 50/100  -> 비급여
		if("6".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 80/100  -> 비급여
		if("7".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 30/100  -> 비급여
		if("8".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 90/100  -> 비급여
		// 싱글이 비보험
		if("0".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2"; // 급여    -> 비보험
		if("1".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2"; // 비급여  -> 비보험
		if("2".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2"; // 100/100 -> 비보험
		if("5".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2"; // 50/100  -> 비보험
		if("6".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2"; // 80/100  -> 비보험
		if("7".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2"; // 30/100  -> 비보험
		if("8".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2"; // 90/100  -> 비보험
		// 싱글이 100/100
		if("0".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 급여    -> 100/100
		if("1".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "2"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 50/100  -> 100/100
		if("6".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 80/100  -> 100/100
		if("7".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 30/100  -> 100/100
		if("8".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4"; // 90/100  -> 100/100
		// 싱글이 50/100
		if("0".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "5"; // 급여    -> 50/100
		if("1".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "2"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "5"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "6"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "5"; // 30/100  -> 50/100
		if("8".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "8"; // 90/100  -> 90/100
		// 싱글이 80/100
		if("0".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6"; // 급여    -> 80/100
		if("1".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "2"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6"; // 50/100  -> 80/100
		if("6".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6"; // 30/100  -> 80/100
		if("8".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 90/100  -> 90/100
		// 싱글이 30/100
		if("0".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "7"; // 급여    -> 30/100
		if("1".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "2"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "5"; // 50/100  -> 50/100
		if("6".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "6"; // 80/100  -> 80/100
		if("7".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "7"; // 30/100  -> 30/100
		if("8".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "8"; // 90/100  -> 90/100
		// 싱글이 90/100
		if("0".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8"; // 급여    -> 90/100
		if("1".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "1"; // 비급여  -> 비급여
		if("2".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "2"; // 비보험  -> 비보험
		if("4".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "4"; // 100/100 -> 100/100
		if("5".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8"; // 50/100  -> 90/100
		if("6".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8"; // 80/100  -> 90/100
		if("7".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8"; // 30/100  -> 90/100
		if("8".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8"; // 90/100  -> 90/188
		return "2";
	}
	
	private String getAlwfg_Default(String a02a_alwfg, String a02_alwfg){
		if("3".equalsIgnoreCase(a02a_alwfg)) return "3";
		if("3".equalsIgnoreCase(a02_alwfg)) return "3";
		// 그룹이 급여
		if("0".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "0";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "5";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "7";
		if("0".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8";
		// 그룹이 비급여
		if("1".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "1";
		if("1".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "1";
		// 그룹이 비보험
		if("2".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "2"; // 2017.02.13 WOOIL 4
		if("2".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "2";
		if("2".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "2";
		// 그룹이 100/100
		if("4".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "4";
		if("4".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "4";
		// 그룹이 50/100
		if("5".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 0
		if("5".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 2017.02.13 WOOIL 1
		if("5".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 2
		if("5".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 4
		if("5".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 5
		if("5".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "5"; // 2017.02.13 WOOIL 6
		if("5".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "5"; // 2018.03.13 By Moon 7
		if("5".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "5"; // 2018.03.13 By Moon 8
		// 그룹이 80/100
		if("6".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 0
		if("6".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 2017.02.13 WOOIL 1
		if("6".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 2
		if("6".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 4
		if("6".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 5
		if("6".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "6"; // 2017.02.13 WOOIL 6
		if("6".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "6"; // 2018.03.13 By Moon 7
		if("6".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "6"; // 2018.03.13 By Moon 8
		// 그룹이 30/100
		if("7".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 0
		if("7".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 2017.02.13 WOOIL 1
		if("7".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 2
		if("7".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 4
		if("7".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 5
		if("7".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "7"; // 2017.02.13 WOOIL 6
		if("7".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "7"; // 2018.03.13 By Moon 7
		if("7".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "7"; // 2018.03.13 By Moon 8
		// 그룹이 90/100
		if("8".equalsIgnoreCase(a02a_alwfg)&&"0".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 0
		if("8".equalsIgnoreCase(a02a_alwfg)&&"1".equalsIgnoreCase(a02_alwfg)) return "1"; // 2017.02.13 WOOIL 1
		if("8".equalsIgnoreCase(a02a_alwfg)&&"2".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 2
		if("8".equalsIgnoreCase(a02a_alwfg)&&"4".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 4
		if("8".equalsIgnoreCase(a02a_alwfg)&&"5".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 5
		if("8".equalsIgnoreCase(a02a_alwfg)&&"6".equalsIgnoreCase(a02_alwfg)) return "8"; // 2017.02.13 WOOIL 6
		if("8".equalsIgnoreCase(a02a_alwfg)&&"7".equalsIgnoreCase(a02_alwfg)) return "8"; // 2018.03.13 By Moon 7
		if("8".equalsIgnoreCase(a02a_alwfg)&&"8".equalsIgnoreCase(a02_alwfg)) return "8"; // 2018.03.13 By Moon 8
		return "2";
	}
}
