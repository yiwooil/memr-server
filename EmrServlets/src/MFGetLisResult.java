import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetLisResult implements MFGet {
	private static MFGetLisResult mInstance=null;
	private MFGetLisResult(){
		
	}
	
	public static MFGetLisResult getInstance(){
		if(mInstance==null){
			mInstance = new MFGetLisResult();
		}
		return mInstance;
	}

	final static String STS_0_Order = "0"; 		// 처방
	final static String STS_1_Collect = "1"; 	// 채혈/채취
	final static String STS_2_Accept = "2"; 	// 접수
	final static String STS_3_Inprocess = "3"; 	// 검사
	final static String STS_4_Input = "4"; 		// 입력중
	final static String STS_5_Interim = "5"; 	// 중간결과(미생물인 경우만 사용)
	final static String STS_6_PartVerify = "6";	// 부분결과
	final static String STS_7_Verify = "7"; 	// 결과
	final static String STS_8_Modify = "8"; 	// 수정
	final static String STS_9_Print = "9";  	// 출력
	
	final static String Div_0_General = "0";	// 일반
	final static String Div_1_Micro = "1";		// 미생물
	final static String Div_2_Special = "2";	// 특수
	
	final static String CD2_Unit = "C207";		// 결과단위
	final static String CD2_Microbe = "C222";	// 미생물
	final static String CD2_AntiBiotic = "C223";// 항생제

	private SqlHelper sqlHelper=null;
	private String hospitalid="";
	private String pid = "";
	private String pid2 = ""; // 자인컴은 id2가 있음.
	private String bededt = "";
	private String frdt = "";
	private String todt = "";
	private String spcno = "";
	private String bdiv = "";

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		this.hospitalid = (String)param.get("hospitalid");
		this.pid = (String)param.get("pid");
		this.bededt = (String)param.get("bededt");
		this.frdt = (String)param.get("frdt");
		this.todt = (String)param.get("todt");
		this.spcno = (String)param.get("spcno");
		this.bdiv = "";

		if(this.spcno==null) this.spcno="";
		
		String returnString="";
		try{
			System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult - 시작 (" + hospitalid + ")");
			this.sqlHelper = new SqlHelper(this.hospitalid);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			boolean isJaincom = sqlHelper.isJaincom();
			if(interfaceTableYn){
				if(isJaincom){
					int pidLen = sqlHelper.getJainComPidLen(); // 2016.07.23 WOOIL
					String patientId = pid;
					if(patientId.length()<=pidLen){
						pid=patientId;
						pid2=" ";							
					}else{
						pid=patientId.substring(0, pidLen);
						pid2=patientId.substring(pidLen, pidLen+1);
					}
					
				}
				returnString=getDataInter();
				/*
				boolean isOracle = sqlHelper.isOracle();
				if(isJaincom){
					String patientId = pid + "        ";
					pid=patientId.substring(0, 7);
					pid2=patientId.substring(7, 8);
					returnString = getDataInterOracleJaincom();
				}else if(isOracle){
					returnString=getDataInterOracle();
				}else{
					returnString=getDataInter();
				}
				*/
			}else{
				returnString=getDataStandard();
			}
			System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult - 종료 (" + hospitalid + ")");
		}catch(Exception ex){
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getDataStandard(){
		String returnString="";
		try {
			
			JSONArray rowData = new JSONArray();
			JSONObject columns = null;
			int rowCount=0;
			
			System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC201_PtID_OrdDt - 시작 (" + hospitalid + ")");
			System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : spcno=" + spcno);
			String tc201 = "";
			if("".equals(spcno)){
				tc201 = getTC201_PtID_OrdDt(pid,frdt,todt,bdiv);
			}else{
				tc201 = getTC201_Spcno(spcno);
				System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : tc201="+tc201);
			}
			System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC201_PtID_OrdDt - 종료 (" + hospitalid + ")");
			
			ResultSetHelper rs201 = new ResultSetHelper(tc201);
			int count=rs201.getRecordCount();
			System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : count=" + count);
			for (int i=0;i<count;i++) {
				System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC301_001_TestCd - 시작 (" + hospitalid + ")");
				String tc301 = getTC301_001_TestCd(rs201.getString(i, "spcno"),0,"","",true);
				System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC301_001_TestCd - 종료 (" + hospitalid + ")");
				ResultSetHelper rs301 = new ResultSetHelper(tc301);
				int cnt301=rs301.getRecordCount();
				if (cnt301>0) {
					for (int j=0;j<cnt301;j++) {
						rowCount++;
						columns=getNewColumns();
						rowData.add(columns);
						if (j==0) {
							columns.put("orddt", rs201.getString(i, "orddt"));
							columns.put("spcnm", rs201.getString(i, "spcnm"));
							columns.put("majnm", rs201.getString(i, "majnm"));
						}
						String abbrnm=rs301.getString(j, "abbrnm");
						String stscd=rs301.getString(j, "stscd");
						String rstval=rs301.getString(j, "rstval");
						String modifyfg=rs301.getString(j, "modifyfg");
						String testdiv=rs301.getString(j, "testdiv");
						String dispAbbrnm="";
						String dispRstval="";
						if (testdiv.compareTo("1")!=0) {
							dispAbbrnm=abbrnm;
						}
						else {
							dispAbbrnm="   "+abbrnm;
						}
						if (!abbrnm.equals("")) {
							if (stscd.compareTo(STS_7_Verify)>=0) {
								dispRstval=rstval;
							}
							else if (stscd.compareTo(STS_5_Interim)==0) {
								dispAbbrnm+="  (중간결과)";
								dispRstval=rstval;
							}
							else if (stscd.compareTo(STS_2_Accept)>=0) {
								dispRstval="검사중";
							}
							else {
								dispRstval="채혈/채취";
							}
						}
						columns.put("abbrnm",dispAbbrnm);
						columns.put("rstval",dispRstval);
						// 이전결과 가져오기
						/* -- 2013.07.20 WOOIL - 조회속도 개선을 위해 일단 막았음.*/
						//columns.put("beforerstval", "");
						if (stscd.compareTo(STS_7_Verify)>=0) {
							System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC301_BeforeResult - 시작 (" + hospitalid + ")");
							String tc301bf=getTC301_BeforeResult(pid,rs301.getString(j, "testcd"),rs301.getString(j, "spccd"),rs201.getString(i, "spcno"));
							System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC301_BeforeResult - 종료 (" + hospitalid + ")");
							ResultSetHelper rs301bf=new ResultSetHelper(tc301bf);
							if (rs301bf.getRecordCount()>0) {
								columns.put("beforerstval", rs301bf.getString(0, "rstval"));
							}
						}
						//
						columns.put("referchk", rs301.getString(j, "referchk"));
						columns.put("panicchk", rs301.getString(j, "panicchk"));
						columns.put("deltachk", rs301.getString(j, "deltachk"));
						// 결과단위가져옴
						/* -- 2012.09.25 WOOIL - 조회속도 개선을 위해 일단 막았음.*/
						System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC002_Master - 시작 (" + hospitalid + ")");
						D_TC002 tc002=getTC002_Master(rs301.getString(j, "testcd"),rs301.getString(j, "appdt"),rs301.getString(j, "apptm"),rs301.getString(j, "equipcd"),rs301.getString(j, "spccd"));
						System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC002_Master - 종료 (" + hospitalid + ")");
						if(tc002!=null){
							columns.put("unit", tc002.unitnm);
						}
						/**/
						// 참고치가지고옴.
						/**/
						System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC004_Result - 시작 (" + hospitalid + ")");
						D_TC004 tc004=getTC004_Result(rs301.getString(j, "testcd"),rs301.getString(j, "equipcd"),rs301.getString(j, "spccd"),rs201.getString(i, "sex"),rs201.getString(i, "age"),rs301.getString(j, "appdt"),rs301.getString(j, "apptm"),rs201.getString(i, "spcno"));
						System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC004_Result - 종료 (" + hospitalid + ")");
						if(tc004!=null){
							String reference="";
							if(!"".equals(tc004.referfr)&&!"".equals(tc004.referto)){
								reference = tc004.referfr + " - " + tc004.referto;
							}else if(!"".equals(tc004.referfr)&&"".equals(tc004.referto)){
								reference = tc004.referfr + " - " + tc004.signfr;
							}else if("".equals(tc004.referfr)&&!"".equals(tc004.referto)){
								reference = tc004.signto + " - " + tc004.referto;
							}
							if(!"".equals(tc004.refer)){
								if (!reference.equals("")) reference+="\r\n";
								reference+=tc004.refer;
							}
							columns.put("reference", reference);
						}
						/**/
						// 검사결과가 미생물 stain 결과이고, 미생물 stain 결과가 있다면
						/**/
						String testrsttype=rs301.getString(j, "testrsttype");
						String stainseq=rs301.getString(j, "stainseq");
						if (testrsttype.equals(Div_1_Micro) && stainseq.compareTo("")>0 && stscd.compareTo(STS_7_Verify)>=0) {
							System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC305_ResultQuery - 시작 (" + hospitalid + ")");
							String tc305=getTC305_ResultQuery(rs201.getString(i,"spcno"),rs301.getString(j, "testcd"),rs301.getString(j, "seq"),rs301.getString(j, "stainseq"));
							System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC305_ResultQuery - 종료 (" + hospitalid + ")");
							ResultSetHelper rs305=new ResultSetHelper(tc305);
							if (rs305.getRecordCount()>0) {
								for (int k=0;k<rs305.getRecordCount();k++) {
									if (k==0) {
										dispRstval=rs305.getString(k, "stainval");
									}
									else {
										dispRstval+="\r\n"+rs305.getString(k, "stainval");
									}
								}
								columns.put("rstval",dispRstval);
							}
						}
						/**/
						// 검사결과가 미생물 culture 결과이고, 미생물 culture 결과가 있다면
						/* -- 2012.09.25 WOOIL - 조회속도 개선을 위해 일단 막았음.*/
						if (testrsttype.equals(Div_1_Micro) && stainseq.compareTo("")>0 && stscd.compareTo(STS_5_Interim)>=0) {
							if (testdiv.equals("0")) {
								dispAbbrnm="";
								columns.put("abbrnm",dispAbbrnm);
							}
							else {
								System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC306_ResultQuery - 시작 (" + hospitalid + ")");
								String tc306=getTC306_ResultQuery(rs201.getString(i, "spcno"),rs301.getString(j, "testcd"),rs301.getString(j, "seq"),rs301.getString(j, "cultureseq"));
								System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC306_ResultQuery - 종료 (" + hospitalid + ")");
								ResultSetHelper rs306=new ResultSetHelper(tc306);
								if (rs306.getRecordCount()>0) {
									dispRstval=rs306.getString(0, "cultureval");
									columns.put("rstval",dispRstval);
								}
								System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC307_ResultQuery - 시작 (" + hospitalid + ")");
								String tc307=getTC307_ResultQuery(rs201.getString(i, "spcno"),rs301.getString(j, "testcd"),rs301.getString(j, "seq"),rs301.getString(j, "cultureseq"));
								System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC307_ResultQuery - 종료 (" + hospitalid + ")");
								ResultSetHelper rs307=new ResultSetHelper(tc307);
								if (rs307.getRecordCount()>0) {
									for (int k=0;k<rs307.getRecordCount();k++) {
										rowCount++;
										columns=getNewColumns();
										rowData.add(columns);
										dispAbbrnm="     "+rs307.getString(k, "field2") + " : " + rs307.getString(k, "increaseval");
										columns.put("abbrnm",dispAbbrnm);
										
										System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC308_ResultQuery - 시작 (" + hospitalid + ")");
										String tcAnti=getTC308_ResultQuery(rs201.getString(i, "spcno"),rs301.getString(j, "testcd"),rs301.getString(j, "seq"),rs301.getString(j, "cultureseq"),rs307.getString(j, "microbeseq"));
										System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC308_ResultQuery - 종료 (" + hospitalid + ")");
										ResultSetHelper rsAnti=new ResultSetHelper(tcAnti);
										if (rsAnti.getRecordCount()>0) {
											for (int l=0;l<rsAnti.getRecordCount();l++) {
												rowCount++;
												columns=getNewColumns();
												rowData.add(columns);
												//
												dispAbbrnm="       "+rsAnti.getString(l, "field2");
												dispRstval=rsAnti.getString(l, "ris");
												if (!rsAnti.getString(l, "numval").equals("")) {
													dispRstval+=" ("+rsAnti.getString(l, "numval")+")";
												}
												columns.put("abbrnm",dispAbbrnm);
												columns.put("rstval", dispRstval);
												columns.put("beforerstval", rsAnti.getString(l, "antirmk"));
											}
										}
									}
								}
							}
						}
						/**/
						
						// 결과가 수정되었다면
						/* -- 2012.09.25 WOOIL - 조회속도 개선을 위해 일단 막았음.*/
						if (modifyfg.compareTo("1")==0) {
							System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC304 - 시작 (" + hospitalid + ")");
							String tc304=getTC304(rs201.getString(i, "spcno"),rs301.getString(j, "testcd"),rs301.getString(j, "seq"));
							System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC304 - 종료 (" + hospitalid + ")");
							ResultSetHelper rs304=new ResultSetHelper(tc304);
							if (rs304.getRecordCount()>0) {
								for (int k=0;k<rs304.getRecordCount();k++) {
									rowCount++;
									columns=getNewColumns();
									rowData.add(columns);
									//
									columns.put("abbrnm", "      수정전결과");
									columns.put("rstval", rs304.getString(k, "rstval"));
									columns.put("referchk", rs304.getString(k, "referchk"));
									columns.put("panicchk", rs304.getString(k, "panicchk"));
									columns.put("deltachk", rs304.getString(k, "deltachk"));
								}
							}
						}
						/**/
						//
						
					}
				}
				// 검사항목 foot note가 있다면
				
				// 검체 foot note가 있는지
				/* -- 2012.09.25 WOOIL - 조회속도 개선을 위해 일단 막았음.*/
				if (rs201.getString(i, "spcfootseq").compareTo("0")>0 && rs201.getString(i, "stscd").compareTo(STS_5_Interim)>=0) {
					System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC303 - 시작 (" + hospitalid + ")");
					String tc303=getTC303(rs201.getString(i, "spcno"),rs201.getString(i, "spcfootseq"));
					System.out.println(Utility.getCurrentDateTime() + " MFGetLisResult : getTC303 - 종료 (" + hospitalid + ")");
					ResultSetHelper rs303=new ResultSetHelper(tc303);
					if (rs303.getRecordCount()>0) {
						rowCount++;
						columns=getNewColumns();
						rowData.add(columns);
						columns.put("abbrnm",rs303.getString(0, "footnote"));
					}
				}
				/**/
				//
				/* -- 2012.09.25 WOOIL - 조회속도 개선을 위해 일단 막았음.*/
				if (rs201.getString(i, "stscd").compareTo(STS_7_Verify)>=0) {
					if (rs201.getString(i, "pthrpt").compareTo("")!=0) {
						rowCount++;
						columns=getNewColumns();
						rowData.add(columns);
						rowCount++;
						columns=getNewColumns();
						rowData.add(columns);
						columns.put("abbrnm", "■ Cli. Pathol. Report");
						rowCount++;
						columns=getNewColumns();
						rowData.add(columns);
						columns.put("abbrnm", rs201.getString(i, "pthrptdis"));
					}
					if (rs201.getString(i, "diagnosis").compareTo("")!=0) {
						rowCount++;
						columns=getNewColumns();
						rowData.add(columns);
						rowCount++;
						columns=getNewColumns();
						rowData.add(columns);
						columns.put("abbrnm", "■ 진단/IMPRESSION");
						rowCount++;
						columns=getNewColumns();
						rowData.add(columns);
						columns.put("abbrnm", rs201.getString(i, "diagnosisdis"));
					}
				}
				/**/
				
			} // end for i
			
			// 리턴값과 메시지
			JSONArray status = new JSONArray();
			JSONArray result = new JSONArray();
			//
			columns = new JSONObject();
			columns.put("return_code",rowCount);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			result.add(rowData);
			//
			returnString=result.toJSONString();
		} catch (JSONException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getDataStandard", "JSONException", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getDataStandard", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	private String getTC201_PtID_OrdDt(String pid, String frdt, String todt, String bdiv) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		ResultSetHelper rs;
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select distinct spcno, sex, age, rcvdt, rcvtm, stscd, spcfootseq, pthrpt, diagnosis, orddt, spcnm, majnm" +
				    "  from tc201 " +
				    " where ptid = ?" +
				    "   and orddt between ? and ?" +
				    "   and (cancelfg != '1' or cancelfg is null)" +
				    "   and stscd >= 1";
			if (bdiv.equals("1")){
				sql +=
					"   and accdiv = '1' ";
			}
			else if (bdiv.equals("2")) {
				sql +=   
					"   and accdiv = '0'  and left(deptcd, 2) != 'ER' ";
			}
			else if (bdiv.equals("3")) {
				sql +=   
					"   and accdiv = '0'  and left(deptcd, 2) = 'ER' ";
			}
			sql +=  " order by orddt desc, spcno desc";
			para.put(++idx, pid);
			para.put(++idx, frdt);
			para.put(++idx, todt);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC201_PtID_OrdDt", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getTC301_001_TestCd(String spcno,int flag,String wscd,String testcd,boolean queryFg) {
		// flag 0: 모든 검사항목
		//      1: 결과확인된 검사항목만
		// wscd : 이런 형식으로 data를 받아야 한다.
		//        예) t0001 + tab + t0002 + tab + t0003
		// testcd :  이런 형식으로 data를 받아야 한다.
		//           예) t0001 + tab + t0002 + tab + t0003
		// queryFg false : 모든 검사항목
		//         true  : 조회/출력이 체크된 검사항목
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select a.spcno, a.testcd, a.seq, a.appdt, a.apptm, a.testdiv, a.testrsttype, a.headtestcd," +
					"       a.sitecd, a.ptid, a.rstfg, a.wscd, a.spccd, a.rstval, a.referchk, a.panicchk," +
					"       a.deltachk, a.pickcd, a.microrsttype, a.stainseq, a.cultureseq, a.footseq, a.modifyfg," +
					"       a.dlyfg, a.statfg, a.manstatfg, a.equipcd, a.vfyid, a.vfydt, a.vfytm, a.prtfg, a.prtdt," +
					"       a.prttm, a.stscd, a.cancelfg, a.cancelcd, a.regdr, a.specdr, b.abbrnm," +
					"       b.datatype, b.datalen, b.keypad, b.rstfg, b.norstfg, b.queryfg, b.norstqueryfg, b.onofffg, b.weekdaydiv" +
				    "  from tc301 a, tc001 b " +
				    " where a.spcno = ?";
			if (!testcd.equals("")) {
				sql +=
					"   and (a.testcd in ('" + testcd.replace("\t", ",") + "') or a.headtestcd in ('" + testcd.replace("\t", ",") + "'))";
			}
			else if (!wscd.equals("")) {
				sql +=
					"   and (wscd in ('" + wscd.replace("\t", ",") + "')";
			}
			if (flag==0) {
				sql +=
					"   and a.stscd>='" + STS_1_Collect + "'";
			}
			else {
				sql +=
					"   and a.stscd>='" + STS_7_Verify + "'";
			}
			if (queryFg==true) {
				sql +=
					"   and ((b.rstfg = '1') or ((rtrim(a.rstval) != '' or a.footseq > 0 or a.stainseq > 0 or a.cultureseq > 0) and b.queryfg = '1') or (b.norstqueryfg = '1'))";
			}
			sql +=
				    "   and (a.cancelfg != '1' or a.cancelfg is null)" +
				    "   and b.testcd = a.testcd" +
				    "   and b.appdt = a.appdt" +
				    "   and b.apptm = a.apptm" +
				    " order by a.seq";
			para.put(++idx, spcno);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC301_001_TestCd", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private JSONObject getNewColumns() {
		JSONObject columns=new JSONObject();
		columns.put("orddt", "");
		columns.put("abbrnm","");
		columns.put("rstval", "");
		columns.put("beforerstval", "");
		columns.put("referchk", "");
		columns.put("panicchk", "");
		columns.put("deltachk", "");
		columns.put("unit", "");
		columns.put("reference", "");
		columns.put("spcnm", "");
		columns.put("majnm", "");
		return columns;
	}
	
	private String getTC301_BeforeResult(String ptid,String testcd,String spccd,String spcno) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select a.spcno, a.rstval, a.vfydt, a.vfytm, a.vfyid, a.appdt, a.apptm" +
				    "  from tc301 a " +
				    " where a.ptid=?" +
				    "   and a.testcd=?" +
				    "   and a.spccd=?" +
				    "   and a.spcno=(select max(b.spcno)" +
				    "                  from tc301 b " +
				    "                 where b.ptid=a.ptid" +
				    "                   and b.testcd=a.testcd" + 
				    "                   and b.spccd=a.spccd" +
				    "                   and b.spcno<?" +
				    "                   and b.stscd>=?" +
				    "                   and (b.cancelfg!=1 or b.cancelfg is null)" +
				    "                  group by b.ptid,b.testcd" +
				    "                )";
			para.put(++idx, ptid);
			para.put(++idx, testcd);
			para.put(++idx, spccd);
			para.put(++idx, spcno);
			para.put(++idx, STS_7_Verify);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC301_BeforeResult", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private D_TC002 getTC002_Master(String testcd, String appdt, String apptm, String equipcd, String spccd) {
		D_TC002 tc002=null;
		D_TC002_dao tc002_dao = D_TC002_dao.getInstance();
		tc002 = tc002_dao.get(hospitalid, testcd, appdt, apptm, equipcd, spccd);
		return tc002;
	}
	
	private D_TC004 getTC004_Result(String testcd,String equipcd,String spccd,String sex,String age,String appdt,String apptm,String spcno) {
		String returnString;
		D_TC004 tc004=null;
		try {
			String agediv="";
			String blooddt="";
			String tc201=getTC201(spcno);
			ResultSetHelper rs201=new ResultSetHelper(tc201);
			if (rs201.getRecordCount()>0) {
				agediv=rs201.getString(0, "agediv");
				blooddt=rs201.getString(0, "blooddt");
			}
			D_TC004_dao tc004_dao = D_TC004_dao.getInstance();
			tc004 = tc004_dao.get(hospitalid, agediv, blooddt, testcd, equipcd, spccd, sex, age, appdt, apptm);
			
		}catch(JSONException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC004_Result", "JSONException", ex.getLocalizedMessage());
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC004_Result", "Exception", ex.getLocalizedMessage());
		}finally{
			return tc004;
		}
	}
	
	private String getTC305_ResultQuery(String spcno,String testcd,String seq,String stainseq) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select spcno, testcd, seq, stainseq, stainvalseq, stainval" +
				    "  from tc305 " +
				    " where spcno=?" +
				    "   and testcd=?" +
				    "   and seq=?" +
				    "   and stainseq=?";
			para.put(++idx, spcno);
			para.put(++idx, testcd);
			para.put(++idx, seq);
			para.put(++idx, stainseq);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC305_ResultQuery", "JSONException", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getTC306_ResultQuery(String spcno,String testcd,String seq,String cultureseq) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select spcno, testcd, seq, cultureseq, weekdayfg, weekday, cultureval, stsdiv" +
				    "  from tc306 " +
				    " where spcno=?" +
				    "   and testcd=?" +
				    "   and seq=?" +
				    "   and cultureseq=?" +
				    "   and stsdiv!='0'" +
				    " order by stsdiv desc,weekday desc";
			para.put(++idx, spcno);
			para.put(++idx, testcd);
			para.put(++idx, seq);
			para.put(++idx, cultureseq);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC306_ResultQuery", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getTC307_ResultQuery(String spcno,String testcd,String seq,String cultureseq) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select a.spcno, a.testcd, a.seq, a.cultureseq, a.weekdayfg, a.weekday, a.microbeseq, a.microbecd, a.increaseval, b.field1, b.field2" +
				    "  from tc307 a, tc032 b " +
				    " where a.spcno=?" +
				    "   and a.testcd=?" +
				    "   and a.seq=?" +
				    "   and a.cultureseq=?" +
				    "   and b.cddiv=?" +
				    "   and b.cdval1=a.microbecd" +
				    " order by a.microbeseq";
			para.put(++idx, spcno);
			para.put(++idx, testcd);
			para.put(++idx, seq);
			para.put(++idx, cultureseq);
			para.put(++idx, CD2_Microbe);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC307_ResultQuery", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getTC308_ResultQuery(String spcno,String testcd,String seq,String cultureseq,String microbeseq) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select a.spcno, a.testcd, a.seq, a.cultureseq, a.microbeseq, a.antiseq, a.anticd, a.numval," +
					"       a.ris, a.adultdos, a.achieve, a.antirmk, a.antiqueryfg, b.field2" +
				    "  from tc308 a, tc032 b " +
				    " where a.spcno=?" +
				    "   and a.testcd=?" +
				    "   and a.seq=?" +
				    "   and a.cultureseq=?" +
				    "   and a.microbeseq=?" +
				    "   and a.antiqueryfg='1'" +
				    "   and b.cddiv=?" +
				    "   and b.cdval1=a.anticd" +
				    " order by a.antiseq";
			para.put(++idx, spcno);
			para.put(++idx, testcd);
			para.put(++idx, seq);
			para.put(++idx, cultureseq);
			para.put(++idx, microbeseq);
			para.put(++idx, CD2_AntiBiotic);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC308_ResultQuery", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getTC304(String spcno,String testcd,String seq) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select spcno, testcd, seq, modifyseq, truemodifyfg, rstval, referchk, panicchk, deltachk, cultureseq, footseq, vfyid, vfydt, vfytm" +
				    "  from tc304 " +
				    " where spcno=?" +
				    "   and testcd=?" +
				    "   and seq=?" +
				    " order by modifyseq";
			para.put(++idx, spcno);
			para.put(++idx, testcd);
			para.put(++idx, seq);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC304", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getTC303(String spcno,String seq) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select spcno, seq, footnote" +
				    "  from tc303 " +
				    " where spcno=?";
			if (!seq.equals("")) {
				sql +=
				    "   and seq=?";
			}
			sql +=  " order by seq";
			para.put(++idx, spcno);
			if (!seq.equals("")) para.put(++idx, seq);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC303", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getTC201(String spcno) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select agediv, blooddt" +
				    "  from tc201 " +
				    " where spcno=?";
			para.put(++idx, spcno);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC201", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}

	private String getTC201_Spcno(String spcno) {
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		String returnString;
		try {
			int idx=0;
			String sql = 
					"select spcno, sex, age, rcvdt, rcvtm, stscd, spcfootseq, pthrpt, diagnosis, orddt, spcnm, majnm" +
				    "  from tc201 " +
				    " where spcno=?";
			para.put(++idx, spcno);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			returnString=rsString;
		}catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getTC201_Spcno", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	/***
	 * Interface 테이블을 사용하는 경우 호출됨.
	 * @return
	 */
	private String getDataInter(){
		String returnString="";
		try {
			HashMap<Integer, Object>para=new HashMap<Integer,Object>();
			HashMap<Integer, String>paraType=new HashMap<Integer,String>();
			
			JSONArray rowData = new JSONArray();
			JSONObject columns = null;
			int rowCount=0;
			
			String bkOdt="";
			String bkSpcnm="";
			String bkMajnm="";

			String sql="";
			
			sql = getSqlInXml(this.hospitalid);
			if("".equalsIgnoreCase(sql)){
				if(sqlHelper.isJaincom()){
					sql = 
						"select a.order_date,a.result_value,a.reference_check,a.panic_check,a.delta_check,a.unit,a.reference_value,a.specimen_code,a.test_status" +
				        "     , b.test_name as test_name" +
						"     , c.specimen_name as specimen_name" +
				        "     , d.test_status_name as test_status_name,d.verify_yn as verify_yn" +
						"     , a.maj_doctor_id" +
				        "     , e.doctor_name as maj_doctor_name" +
				        "  from lab_result_hist a inner join test_code_mast b on b.test_code=a.test_code and b.start_date=(select max(x.start_date) from test_code_mast x where x.test_code=a.test_code and x.start_date<=a.order_date)" +
						"                         left  join specimen_code_mast c on c.specimen_code=a.specimen_code" +
				        "                         left  join test_status_mast d on d.test_status=a.test_status" +
						"                         left  join doctor_mast e on e.doctor_id=a.maj_doctor_id" +
				        " where a.pat_id=?" +
						"   and a.pat_id2=?" +
				        "   and a.order_date between ? and ? " +
				        " order by a.order_date desc,a.specimen_code,a.pat_id,a.bed_in_date,a.order_no,a.order_seq,a.test_seq";					
				}else if(sqlHelper.isOracle()){
					sql = 
						"select a.order_date,a.result_value,a.reference_check,a.panic_check,a.delta_check,a.unit,a.reference_value,a.specimen_code,a.test_status" +
				        "     , b.test_name as test_name" +
						"     , c.specimen_name as specimen_name" +
				        "     , d.test_status_name as test_status_name,d.verify_yn as verify_yn" +
						"     , a.maj_doctor_id" +
				        "     , e.doctor_name as maj_doctor_name" +
				        "  from lab_result_hist a inner join test_code_mast b on b.test_code=a.test_code and b.start_date=(select max(x.start_date) from test_code_mast x where x.test_code=a.test_code and x.start_date<=a.order_date)" +
						"                         left  join specimen_code_mast c on c.specimen_code=a.specimen_code" +
				        "                         left  join test_status_mast d on d.test_status=a.test_status" +
						"                         left  join doctor_mast e on e.doctor_id=a.maj_doctor_id" +
				        " where a.pat_id=?" +
				        "   and a.order_date between ? and ? " +
				        " order by a.order_date desc,a.specimen_code,a.pat_id,a.bed_in_date,a.order_no,a.order_seq,a.test_seq";
				}else{
					sql = 
						"select a.order_date,a.result_value,a.reference_check,a.panic_check,a.delta_check,a.unit,a.reference_value,a.specimen_code,a.test_status" +
				        "     , isnull(b.test_name,'') as test_name" +
						"     , isnull(c.specimen_name,'') as specimen_name" +
				        "     , isnull(d.test_status_name,'') as test_status_name,isnull(d.verify_yn,'') as verify_yn" +
						"     , a.maj_doctor_id" +
				        "     , isnull(e.doctor_name,'') as maj_doctor_name" +
				        "  from lab_result_hist a inner join test_code_mast b on b.test_code=a.test_code and b.start_date=(select max(x.start_date) from test_code_mast x where x.test_code=a.test_code and x.start_date<=a.order_date)" +
						"                         left  join specimen_code_mast c on c.specimen_code=a.specimen_code" +
				        "                         left  join test_status_mast d on d.test_status=a.test_status" +
						"                         left  join doctor_mast e on e.doctor_id=a.maj_doctor_id" +
				        " where a.pat_id=?" +
				        "   and a.order_date between ? and ? " +
				        " order by a.order_date desc,a.specimen_code,a.pat_id,a.bed_in_date,a.order_no,a.order_seq,a.test_seq";
				}
			}
			new LogWrite().debugWrite(getClass().getSimpleName(), "getDataInter", "sql=" + sql);
			int idx=0;
			para.put(++idx, this.pid); paraType.put(idx, "C");
			if(sqlHelper.isJaincom()){
				para.put(++idx, this.pid2); paraType.put(idx, "C");
			}
			para.put(++idx, this.frdt); paraType.put(idx, "D");
			para.put(++idx, this.todt); paraType.put(idx, "D");
			String rsString=sqlHelper.executeQuery(sql,para,paraType);
			ResultSetHelper rs = new ResultSetHelper(rsString);
			int cnt=rs.getRecordCount();
			if (cnt>0) {
				for (int i=0;i<cnt;i++) {
					String spcnm=rs.getString(i, "specimen_name");
					if("".equals(spcnm)) spcnm=rs.getString(i, "specimen_code");
					String rstval=rs.getString(i, "result_value");
					String stscd=rs.getString(i, "test_status");
					String vfyYn=rs.getString(i, "verify_yn");
					if(!"Y".equals(vfyYn)){
						rstval=rs.getString(i, "test_status_name");
					}
					String beforerstval="";
					String majnm=rs.getString(i, "maj_doctor_name");
					String odt=rs.getString(i, "order_date");
					
					rowCount++;
					columns=getNewColumns();
					rowData.add(columns);
					if (!bkOdt.equals(odt)){
						columns.put("orddt", odt);
						columns.put("spcnm", spcnm);
						columns.put("majnm", majnm);
						bkOdt=odt;
						bkSpcnm=spcnm;
						bkMajnm=majnm;
					}
					if (!bkSpcnm.equals(spcnm)){
						columns.put("spcnm", spcnm);
						bkSpcnm=spcnm;
					}
					if (!bkMajnm.equals(majnm)){
						columns.put("majnm", majnm);
						bkMajnm=majnm;
					}
					columns.put("abbrnm",rs.getString(i, "test_name"));
					columns.put("rstval", rstval);
					columns.put("beforerstval", beforerstval);
					columns.put("referchk", rs.getString(i, "reference_check"));
					columns.put("panicchk", rs.getString(i, "panic_check"));
					columns.put("deltachk", rs.getString(i, "delta_check"));
					columns.put("unit", rs.getString(i, "unit"));
					columns.put("reference", rs.getString(i, "reference_value"));
				}
			}
			// 리턴값과 메시지
			JSONArray status = new JSONArray();
			JSONArray result = new JSONArray();
			//
			columns = new JSONObject();
			columns.put("return_code",rowCount);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			result.add(rowData);
			//
			returnString=result.toJSONString();

		} catch (JSONException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getDataInter", "JSONException", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getDataInter", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}

//	private String getDataInterOracle(){
//		String returnString="";
//		try {
//			HashMap<Integer, Object>para=new HashMap<Integer,Object>();
//			HashMap<Integer, String>paraType=new HashMap<Integer,String>();
//			
//			JSONArray rowData = new JSONArray();
//			JSONObject columns = null;
//			int rowCount=0;
//			
//			String bkOdt="";
//			String bkSpcnm="";
//			String bkMajnm="";
//
//			String sql = 
//					"select a.order_date,a.result_value,a.reference_check,a.panic_check,a.delta_check,a.unit,a.reference_value,a.specimen_code,a.test_status" +
//			        "     , b.test_name as test_name" +
//					"     , c.specimen_name as specimen_name" +
//			        "     , d.test_status_name as test_status_name,d.verify_yn as verify_yn" +
//					"     , a.maj_doctor_id" +
//			        "     , e.doctor_name as maj_doctor_name" +
//			        "  from lab_result_hist a inner join test_code_mast b on b.test_code=a.test_code and b.start_date=(select max(x.start_date) from test_code_mast x where x.test_code=a.test_code and x.start_date<=a.order_date)" +
//					"                         left  join specimen_code_mast c on c.specimen_code=a.specimen_code" +
//			        "                         left  join test_status_mast d on d.test_status=a.test_status" +
//					"                         left  join doctor_mast e on e.doctor_id=a.maj_doctor_id" +
//			        " where a.pat_id=?" +
//			        "   and a.order_date between ? and ? " +
//			        " order by a.order_date desc,a.specimen_code,a.pat_id,a.bed_in_date,a.order_no,a.order_seq,a.test_seq";
//			para.put(1, this.pid); paraType.put(1, "C");
//			para.put(2, this.frdt); paraType.put(2, "D");
//			para.put(3, this.todt); paraType.put(3, "D");
//			String rsString=sqlHelper.executeQuery(sql,para,paraType);
//			ResultSetHelper rs = new ResultSetHelper(rsString);
//			int cnt=rs.getRecordCount();
//			if (cnt>0) {
//				for (int i=0;i<cnt;i++) {
//					String spcnm=rs.getString(i, "specimen_name");
//					if("".equals(spcnm)) spcnm=rs.getString(i, "specimen_code");
//					String rstval=rs.getString(i, "result_value");
//					String stscd=rs.getString(i, "test_status");
//					String vfyYn=rs.getString(i, "verify_yn");
//					if(!"Y".equals(vfyYn)){
//						rstval=rs.getString(i, "test_status_name");
//					}
//					String beforerstval="";
//					String majnm=rs.getString(i, "maj_doctor_name");
//					String odt=rs.getString(i, "order_date");
//					
//					rowCount++;
//					columns=getNewColumns();
//					rowData.add(columns);
//					if (!bkOdt.equals(odt)){
//						columns.put("orddt", odt);
//						columns.put("spcnm", spcnm);
//						columns.put("majnm", majnm);
//						bkOdt=odt;
//						bkSpcnm=spcnm;
//						bkMajnm=majnm;
//					}
//					if (!bkSpcnm.equals(spcnm)){
//						columns.put("spcnm", spcnm);
//						bkSpcnm=spcnm;
//					}
//					if (!bkMajnm.equals(majnm)){
//						columns.put("majnm", majnm);
//						bkMajnm=majnm;
//					}
//					columns.put("abbrnm",rs.getString(i, "test_name"));
//					columns.put("rstval", rstval);
//					columns.put("beforerstval", beforerstval);
//					columns.put("referchk", rs.getString(i, "reference_check"));
//					columns.put("panicchk", rs.getString(i, "panic_check"));
//					columns.put("deltachk", rs.getString(i, "delta_check"));
//					columns.put("unit", rs.getString(i, "unit"));
//					columns.put("reference", rs.getString(i, "reference_value"));
//				}
//			}
//			// 리턴값과 메시지
//			JSONArray status = new JSONArray();
//			JSONArray result = new JSONArray();
//			//
//			columns = new JSONObject();
//			columns.put("return_code",rowCount);
//			columns.put("return_desc","ok");
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			result.add(rowData);
//			//
//			returnString=result.toJSONString();
//
//		} catch (JSONException ex) {
//			new LogWrite().errorWrite(getClass().getSimpleName(), "getDataInterOracle", "JSONException", ex.getLocalizedMessage());
//			returnString=ExceptionHelper.toJSONString(ex);
//		} catch (Exception ex) {
//			new LogWrite().errorWrite(getClass().getSimpleName(), "getDataInterOracle", "Exception", ex.getLocalizedMessage());
//			returnString=ExceptionHelper.toJSONString(ex);
//		}
//		return returnString;
//	}
	
//	private String getDataInterOracleJaincom(){
//		String returnString="";
//		try {
//			HashMap<Integer, Object>para=new HashMap<Integer,Object>();
//			JSONArray rowData = new JSONArray();
//			JSONObject columns = null;
//			int rowCount=0;
//			
//			String bkOdt="";
//			String bkSpcnm="";
//			String bkMajnm="";
//
//			String sql = 
//					"select a.order_date,a.result_value,a.reference_check,a.panic_check,a.delta_check,a.unit,a.reference_value,a.specimen_code,a.test_status" +
//			        "     , b.test_name as test_name" +
//					"     , c.specimen_name as specimen_name" +
//			        "     , d.test_status_name as test_status_name,d.verify_yn as verify_yn" +
//					"     , a.maj_doctor_id" +
//			        "     , e.doctor_name as maj_doctor_name" +
//			        "  from lab_result_hist a inner join test_code_mast b on b.test_code=a.test_code and b.start_date=(select max(x.start_date) from test_code_mast x where x.test_code=a.test_code and x.start_date<=a.order_date)" +
//					"                         left  join specimen_code_mast c on c.specimen_code=a.specimen_code" +
//			        "                         left  join test_status_mast d on d.test_status=a.test_status" +
//					"                         left  join doctor_mast e on e.doctor_id=a.maj_doctor_id" +
//			        " where a.pat_id=?" +
//					"   and a.pat_id2=?" +
//			        "   and a.order_date between ? and ? " +
//			        " order by a.order_date desc,a.specimen_code,a.pat_id,a.bed_in_date,a.order_no,a.order_seq,a.test_seq";
//			para.put(1, this.pid);
//			para.put(2, this.pid2);
//			para.put(3, this.frdt);
//			para.put(4, this.todt);
//			String rsString=sqlHelper.executeQuery(sql,para,null);
//			ResultSetHelper rs = new ResultSetHelper(rsString);
//			int cnt=rs.getRecordCount();
//			if (cnt>0) {
//				for (int i=0;i<cnt;i++) {
//					String spcnm=rs.getString(i, "specimen_name");
//					if("".equals(spcnm)) spcnm=rs.getString(i, "specimen_code");
//					String rstval=rs.getString(i, "result_value");
//					String stscd=rs.getString(i, "test_status");
//					String vfyYn=rs.getString(i, "verify_yn");
//					if(!"Y".equals(vfyYn)){
//						rstval=rs.getString(i, "test_status_name");
//					}
//					String beforerstval="";
//					String majnm=rs.getString(i, "maj_doctor_name");
//					String odt=rs.getString(i, "order_date");
//					
//					rowCount++;
//					columns=getNewColumns();
//					rowData.add(columns);
//					if (!bkOdt.equals(odt)){
//						columns.put("orddt", odt);
//						columns.put("spcnm", spcnm);
//						columns.put("majnm", majnm);
//						bkOdt=odt;
//						bkSpcnm=spcnm;
//						bkMajnm=majnm;
//					}
//					if (!bkSpcnm.equals(spcnm)){
//						columns.put("spcnm", spcnm);
//						bkSpcnm=spcnm;
//					}
//					if (!bkMajnm.equals(majnm)){
//						columns.put("majnm", majnm);
//						bkMajnm=majnm;
//					}
//					columns.put("abbrnm",rs.getString(i, "test_name"));
//					columns.put("rstval", rstval);
//					columns.put("beforerstval", beforerstval);
//					columns.put("referchk", rs.getString(i, "reference_check"));
//					columns.put("panicchk", rs.getString(i, "panic_check"));
//					columns.put("deltachk", rs.getString(i, "delta_check"));
//					columns.put("unit", rs.getString(i, "unit"));
//					columns.put("reference", rs.getString(i, "reference_value"));
//				}
//			}
//			// 리턴값과 메시지
//			JSONArray status = new JSONArray();
//			JSONArray result = new JSONArray();
//			//
//			columns = new JSONObject();
//			columns.put("return_code",rowCount);
//			columns.put("return_desc","ok");
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			result.add(rowData);
//			//
//			returnString=result.toJSONString();
//
//		} catch (JSONException ex) {
//			new LogWrite().errorWrite(getClass().getSimpleName(), "getDataInterOracleJaincom", "JSONException", ex.getLocalizedMessage());
//			returnString=ExceptionHelper.toJSONString(ex);
//		} catch (Exception ex) {
//			new LogWrite().errorWrite(getClass().getSimpleName(), "getDataInterOracleJaincom", "Exception", ex.getLocalizedMessage());
//			returnString=ExceptionHelper.toJSONString(ex);
//		}
//		return returnString;
//	}	
	
	private String getSqlInXml(String hospitalId) throws Exception{
		String sqlId = "lis_result";
		HashMap<String,Object>param = new HashMap<String,Object>();
		param.put("hospitalid", hospitalId);
		param.put("sqlid", sqlId);
		MFGet instance = MFGetHospitalSql.getInstance();
		String sql = instance.getData(param);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getSqlInXml", "sql=" + sql);
		return sql;
	}
	
}
