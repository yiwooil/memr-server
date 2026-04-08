import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MFPutCertificatePaper implements MFPut {
	private static MFPutCertificatePaper mInstance=null;
	private MFPutCertificatePaper(){
		
	}
	
	public static MFPutCertificatePaper getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificatePaper();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String ccfIdList = (String)param.get("ccfId"); // 2024.07.12 WOOIL - ccfId -> ccfIdList
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String bdiv = (String)param.get("bdiv");
		String fileNameList = (String)param.get("filename"); // 2024.07.15 WOOIL - fileName -> fileNameList
		String mp4FileList = (String)param.get("mp4filelist");
		String picFileList = (String)param.get("picfilelist");
		String applyExdt = (String)param.get("apply_exdt"); // 2026.01.30 WOOIL - sysdt와 분리한다. 사용자가 exdt를 수정할 수 있게 함.
		String seqList = (String)param.get("seq"); // 2024.07.15 WOOIL - seq -> seqList
		String sysdt = (String)param.get("sysdt");
		String systm = (String)param.get("systm");
		String rptnm = (String)param.get("rptnm");
		String presave = (String)param.get("presave"); // 임시저장 중인지(TG02T에 저장해야함.)
		String bfPresaved = (String)param.get("bf_presaved"); // 임시저장이미지를 불러와서 저장하는 중인지(TG02T에 삭제표시를 해야해서)
		String bfExdt = (String)param.get("bf_exdt");
		String bfSeqList = (String)param.get("bf_seq"); // 2024.07.15 WOOIL - bfSeq -> bfSeqList
		String emrScanClass = (String)param.get("emr_scan_class");
		String subPageListList = (String)param.get("sub_page_list"); // 2024.07.15 WOOIL - subPageList -> subPageListList
		String subPageNoList = (String)param.get("sub_page_no"); // 2024.07.15 WOOIL - subPageNo -> subPageNoList
		String dptcd = (String)param.get("dptcd"); // 2024.06.21 WOOIL -
		String drid = (String)param.get("drid"); // 2024.06.21 WOOIL -
		String qfycd = (String)param.get("qfycd"); // 2024.06.24 WOOIL - 
		String reSaveYn = (String)param.get("re_save_yn"); // 2026.02.03 WOOIL - 한 번 저장한 동의서를 다시 저장함.

		if (pid == null) pid = "";
		if (bededt == null) bededt = "";
		if (bdiv == null) bdiv = "";
		if (mp4FileList == null) mp4FileList = "";
		if (picFileList == null) picFileList = "";
		if (emrScanClass == null) emrScanClass = "";
		if (subPageListList == null) subPageListList = "";
		if (subPageNoList == null) subPageNoList = "";
		if (dptcd == null) dptcd = ""; // 2024.06.21 WOOIL -
		if (drid == null) drid = ""; // 2024.06.21 WOOIL -
		if (qfycd == null) qfycd = ""; // 2024.06.24 WOOIL -
		if (applyExdt == null) applyExdt = sysdt; // 2026.01.30 WOOIL - 앱이 나중에 업데이트되는 경우를 대비함.
		if (reSaveYn == null) reSaveYn = ""; // 2026.02.03 WOOIL - 
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "pid="+pid+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  sysdt="+sysdt+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  systm="+systm+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  applyExdt="+applyExdt+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  seqList="+seqList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  ccfIdList="+ccfIdList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  fileNameList="+fileNameList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  mp4FileList="+mp4FileList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  picFileList="+picFileList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  bfExdt="+bfExdt+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  bfSeqList="+bfSeqList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  subPageListList="+subPageListList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  subPageNoList="+subPageNoList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  dptcd="+dptcd+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  drid="+drid+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  qfycd="+qfycd+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  reSaveYn="+reSaveYn+"");
		
		List<String> sqlList = new ArrayList<String>();
		List<HashMap<Integer, Object>> paraList = new ArrayList<HashMap<Integer, Object>>(); 
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			String inoutdiv = "I"; // 
			if(bdiv.equals("1")) inoutdiv = "O"; // 외래
			String rptcd = "ZZ01"; // android에서 올린 사인이미지
			
			//int idx=0;
			String tg02="tg02";
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String scanUrl = sqlHelper.getScanUrl();
			String emrScanUrl = sqlHelper.getEmrScanUrl();
			String emrScanUrlFormat = sqlHelper.getEmrScanUrlFormat();
			String emrScanFile = "";
			String emrScanFile4save = "";
			String picUrl = sqlHelper.getPicUrl();
			String fileNamePrefix = sqlHelper.getFileNamePrefix();
			String fileNamePrefixPresave = sqlHelper.getFileNamePrefixPresave();
			String fileNamePrefixPic = sqlHelper.getFileNamePrefixPic();
			String fileNamePrefixMP4 = sqlHelper.getFileNamePrefixMP4();
			
			//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "emrScanUrl="+emrScanUrl+"");
			

			String insString="";
			
			// 2024.07.02 WOOIL - 여러 페이지를 한 트랜젝션으로 저장하도록 수정
			String[] aryCcfid = ccfIdList.split(",");
			String[] arySeq = seqList.split(",", -1); // 2024.07.15 WOOIL
			String[] aryFileName = fileNameList.split(",", -1); // 2024.07.15 WOOIL
			String[] aryBfSeq = bfSeqList.split(",", -1); // 2024.07.15 WOOIL
			String[] arySubPageList = subPageListList.split(",", -1); // 2024.07.15 WOOIL
			String[] arySubPageNo = subPageNoList.split(",", -1); // 2024.07.15 WOOIL
			
			int ccfCount = aryCcfid.length;
			for(int ccfidx=0 ; ccfidx < ccfCount ; ccfidx++){
				
				String ccfId = aryCcfid[ccfidx];
				String seq = arySeq[ccfidx];
				String fileName = aryFileName[ccfidx];
				String bfSeq = aryBfSeq[ccfidx];
				String subPageList = arySubPageList[ccfidx];
				String subPageNo = arySubPageNo[ccfidx];
			
				if(ccfidx==0){
					// 녹음파일과 사진파일은 동의서 1번 페이지 저장할 때 저장한다.
					if(!"".equalsIgnoreCase(mp4FileList)){
						// 녹음파일이 있음.저장하자.
						StringBuilder sbmp4;
						sbmp4 = new StringBuilder();
						String tg02mp4="";
						tg02mp4="tg02mp4";
						if("Y".equalsIgnoreCase(presave)) tg02mp4="tg02tmp4";
						sbmp4.append("insert into "+tg02mp4+"(pid,bdiv,exdt,seq,rptcd,seqno,path,sysdt,systm,empid,path2)");
						sbmp4.append("values(?,?,?,?,?,?,?,?,?,?,?)");
						String sqlmp4 = sbmp4.toString();
						String[] aryMp4File = mp4FileList.split(",");
						int mp4Count = aryMp4File.length;
						for(int mp4idx=0 ; mp4idx < mp4Count ; mp4idx++){
							HashMap<Integer, Object>paramp4=new HashMap<Integer,Object>();
							paramp4.put(1, pid);
							paramp4.put(2, inoutdiv);
							paramp4.put(3, applyExdt); // 2026.01.30 WOOIL - sysdt->exdt
							paramp4.put(4, seq);
							paramp4.put(5, rptcd);
							paramp4.put(6, (mp4idx+1));
							paramp4.put(7, fileNamePrefixMP4 + aryMp4File[mp4idx]);
							paramp4.put(8, sysdt);
							paramp4.put(9, systm);
							paramp4.put(10,userId);
							paramp4.put(11,"");
							
							sqlList.add(sqlmp4);
							paraList.add(paramp4);
						}
					}
					if(!"".equalsIgnoreCase(picFileList)){
						// 사진파일이 있음.저장하자.
						StringBuilder sbpic;
						sbpic = new StringBuilder();
						String tg02pic="";
						tg02pic="tg02pic";
						if("Y".equalsIgnoreCase(presave)) tg02pic="tg02tpic";
						sbpic.append("insert into "+tg02pic+"(pid,bdiv,exdt,seq,rptcd,seqno,path,sysdt,systm,empid,path2)");
						sbpic.append("values(?,?,?,?,?,?,?,?,?,?,?)");
						// emr 쪽에 저장할 폴더명을 구한다.
						String emrScanPicFolder4save = File.separator + pid + File.separator + sysdt;
						// 옵션1 환자ID + 일자(YYYY-MM-DD)
						if("1".equalsIgnoreCase(emrScanUrlFormat)){
							emrScanPicFolder4save = File.separator + pid + File.separator + Utility.getFormattedDate(sysdt, "yyyy-mm-dd");
						}
						//
						String sqlpic = sbpic.toString();
						String[] aryPicFile = picFileList.split(",");
						int picCount = aryPicFile.length;
						//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "picFileList=" + picFileList + ", picCount=" + picCount + ", aryPicFile.length=" + aryPicFile.length);
						for(int picidx=0 ; picidx < picCount ; picidx++){
							String scanFilePic = picUrl + File.separator + aryPicFile[picidx];
							File file = new File(scanFilePic); // 동의서 사진파일(full path)
							String emrScanPicFile4save = emrScanPicFolder4save + File.separator + file.getName(); // emr쪽으로 넘길 파일명
							
							HashMap<Integer, Object>parapic=new HashMap<Integer,Object>();
							parapic.put(1, pid);
							parapic.put(2, inoutdiv);
							parapic.put(3, applyExdt);  // 2026.01.30 WOOIL - sysdt->exdt
							parapic.put(4, seq);
							parapic.put(5, rptcd);
							parapic.put(6, (picidx+1));
							parapic.put(7, fileNamePrefixPic + aryPicFile[picidx]);
							parapic.put(8, sysdt);
							parapic.put(9, systm);
							parapic.put(10,userId);
							parapic.put(11,emrScanPicFile4save);
							
							sqlList.add(sqlpic);
							paraList.add(parapic);
						}
					}
				}
									
				if("Y".equalsIgnoreCase(reSaveYn)){
					// 2026.02.04 WOOIL - 이전 저장한 내역의 AFEXDT,AFSEQ를 UPDATE 한다.
					HashMap<Integer, Object>para=new HashMap<Integer,Object>();
					String sql = "";
					sql = "update tg02 set afexdt=?, afseq=? where pid=? and bdiv=? and exdt=? and seq=? and rptcd=?";
					//
					para.put(1, applyExdt); // 새로 저장되는 exdt
					para.put(2, seq);       // 새로 저장되는 seq
					para.put(3, pid);            // 이전 동의서 pid
					para.put(4, inoutdiv);       // 이전 동의서 bdiv
					para.put(5, bfExdt);         // 이전 동의서 exdt
					para.put(6, bfSeq);          // 이전 동의서 seq
					para.put(7, rptcd);
					
					sqlList.add(sql);
					paraList.add(para);
					
					// 2026.02.04 WOOIL - CCF_ID를 구해야함.
					String qSql =
							"select ccf_id from tg02 where pid=? and bdiv=? and exdt=? and seq=? and rptcd=?";
					HashMap<Integer, Object>qPara=new HashMap<Integer,Object>();
					qPara.put(1, pid);
					qPara.put(2, inoutdiv);
					qPara.put(3, bfExdt);
					qPara.put(4, bfSeq);
					qPara.put(5, rptcd);
					String rsString = sqlHelper.executeQuery(qSql, qPara, null);
					ResultSetHelper rsHelper = new ResultSetHelper(rsString);
					new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccf_id 읽는 중(읽은 건수="+rsHelper.getRecordCount()+")");
					if(rsHelper.getRecordCount()>0){
						ccfId = rsHelper.getString(0, "ccf_id");
					}			
					
				}else if("Y".equalsIgnoreCase(bfPresaved)){
					// 2021.09.30 WOOIL - 임시저장한 자료를 불러와서 저장하는 경우 ccf_id가 잘못 저장되는 경우가 있어서 로그를 남김
					new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "임시저장내역 정상저장중");
					new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "pid="+pid+", inoutdiv="+inoutdiv+", bfExdt="+bfExdt+", bfSeq="+bfSeq);
	
					// 2021.10.07 WOOIL - 이부분을 앞으로 옮겼다.
					//                    inoutdiv가 잘 못 설정되는 경우 바로잡기 위해서임.
					// 2021.08.30 WOOIL - 임시저장되었던 자료를 저장하면 ccf_id에 "presaved" 라고 넘어온다.
					//                    클라이언트에서 임시저장되었던 테비을에서 자료에서 ccf_id를 가져온다.
					//                    클라이언트에서 자료를 넘겨도 되지만 빨리 처리해야하기 위해 이곳에서 처리한다.
					boolean bReadOk = false;
					new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccf_id 읽기 전(ccfId="+ccfId+")");
					String qSql =
							"select ccf_id from tg02t where pid=? and bdiv=? and exdt=? and seq=? and rptcd=?";
					HashMap<Integer, Object>qPara=new HashMap<Integer,Object>();
					qPara.put(1, pid);
					qPara.put(2, inoutdiv);
					qPara.put(3, bfExdt);
					qPara.put(4, bfSeq);
					qPara.put(5, rptcd);
					String rsString = sqlHelper.executeQuery(qSql, qPara, null);
					ResultSetHelper rsHelper = new ResultSetHelper(rsString);
					new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccf_id 읽는 중(읽은 건수="+rsHelper.getRecordCount()+")");
					if(rsHelper.getRecordCount()>0){
						ccfId = rsHelper.getString(0, "ccf_id");
						bReadOk = true;
					}			
					new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccf_id 읽은 후(ccfId="+ccfId+")");
					if(bReadOk==false){
						// 2021.10.07 WOOIL - inoutdiv를 변경하여 시도한다.
						if("I".equalsIgnoreCase(inoutdiv)) inoutdiv="O";
						else if("O".equalsIgnoreCase(inoutdiv)) inoutdiv="I";
						new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccf_id 읽기 전(두번째시도)(ccfId="+ccfId+")");
						String qSql_2 =
								"select ccf_id from tg02t where pid=? and bdiv=? and exdt=? and seq=? and rptcd=?";
						HashMap<Integer, Object>qPara_2=new HashMap<Integer,Object>();
						qPara_2.put(1, pid);
						qPara_2.put(2, inoutdiv);
						qPara_2.put(3, bfExdt);
						qPara_2.put(4, bfSeq);
						qPara_2.put(5, rptcd);
						String rsString_2 = sqlHelper.executeQuery(qSql_2, qPara_2, null);
						ResultSetHelper rsHelper_2 = new ResultSetHelper(rsString_2);
						new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccf_id 읽는 중(두번째시도)(읽은 건수="+rsHelper_2.getRecordCount()+")");
						if(rsHelper_2.getRecordCount()>0){
							ccfId = rsHelper_2.getString(0, "ccf_id");
						}			
						new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "ccf_id 읽은 후(두번째시도)(ccfId="+ccfId+")");
					}
					
					// 2021.09.30 WOOIL - 임시저장한 자료를 불러왓 저장하는 경우 ccf_id가 잘못 저장되는 경우가 있어서 로그를 남김
					new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "pid="+pid+", inoutdiv="+inoutdiv+", bfExdt="+bfExdt+", bfSeq="+bfSeq);
					// 임시저장된 동의서를 불러서 저장하는 경우 임시저장 테이블에 삭제표시(del_flag='1')를 해주어야한다.
					HashMap<Integer, Object>para2=new HashMap<Integer,Object>();
					String sql2 = "";
					sql2 = "update tg02t set delfg='1' where pid=? and bdiv=? and exdt=? and seq=? and rptcd=?";
					//
					para2.put(1, pid);
					para2.put(2, inoutdiv);
					para2.put(3, bfExdt);
					para2.put(4, bfSeq);
					para2.put(5, rptcd);
					
					sqlList.add(sql2);
					paraList.add(para2);
				}
				
				// 
				// 테이블 TG02에 저장한다.
				StringBuilder sb;
				sb = new StringBuilder();
				
				if("Y".equalsIgnoreCase(presave)){
					sb.append("insert into tg02t(pid,bdiv,exdt,seq,rptcd,path,sysdt,systm,empid,bfexdt,bfseq,path2,rptcd2,rptnm2,ccf_id,sub_page_list,sub_page_no,dptcd,drid,qfycd,tsa_status,orgexdt,orgseq)");
					sb.append("values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				}else{
					sb.append("insert into tg02(pid,bdiv,exdt,seq,rptcd,path,sysdt,systm,empid,bfexdt,bfseq,path2,rptcd2,rptnm2,ccf_id,sub_page_list,sub_page_no,dptcd,drid,qfycd,tsa_status,orgexdt,orgseq)");
					sb.append("values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				}		

				if("Y".equalsIgnoreCase(presave)) fileNamePrefix=fileNamePrefixPresave;
				String sql = sb.toString();
	
				HashMap<Integer, Object>para=new HashMap<Integer,Object>();			
				para.put(1, pid);
				para.put(2, inoutdiv);
				para.put(3, applyExdt);  // 2026.01.30 WOOIL - sysdt->exdt
				para.put(4, seq);
				para.put(5, rptcd);
				para.put(6, fileNamePrefix + fileName);
				para.put(7, sysdt);
				para.put(8, systm);
				para.put(9, userId);
				if("Y".equalsIgnoreCase(reSaveYn)){
					para.put(10, ""); // 다시 저장하는 경우는 orgexdt에 저장하고 bfexdt에는 저장하지 않는다
					para.put(11, ""); // 다시 저장하는 경우는 orgseq에 저장하고 bfseq에는 저장하지 않는다
				}else{
					para.put(10, bfExdt);
					para.put(11, bfSeq);
				}
				para.put(12, emrScanFile4save);
				para.put(13, emrScanClass);
				
				// tsa여부를 저장한다.
				String tsaStatus = "";
				String qSql =
						"select cdnm,fld4cd from ta88 (nolock) where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
				HashMap<Integer, Object>qPara=new HashMap<Integer,Object>();
				qPara.put(1, ccfId);
				String rsString = sqlHelper.executeQuery(qSql, qPara, null);
				ResultSetHelper rsHelper = new ResultSetHelper(rsString);
				if(rsHelper.getRecordCount()>0){
					String cdnm = rsHelper.getString(0, "cdnm");
					String fld4cd = rsHelper.getString(0, "fld4cd");
					rptnm = cdnm;
					if("1".equals(fld4cd)) tsaStatus="Y";
					if("Y".equalsIgnoreCase(presave)) tsaStatus=""; // 2024.07.09 WOOIL - 임시저장이면 플래그를 지운다.
				}
					
				//new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "(3) tsaStatus="+tsaStatus);
				// 동의서 이름. 2장 이상인 동의서인 경우 단말기에서 명칭을 넘기지 못하므로 이곳에서 읽어서 처리한다.
				para.put(14, rptnm);
				// 동의서ID를 저장한다
				para.put(15, ccfId); 
				// 2022.03.22 WOOIL - 실저장, 임시저장 모두 페이지 정보를 저장한다.
				para.put(16, subPageList);
				para.put(17, subPageNo);
				para.put(18, dptcd); // 2024.06.21 WOOIL - 진료과 정보를 저장한다.
				para.put(19, drid); // 2024.06.21 WOOIL - 의사 정보를 저장한다.
				para.put(20, qfycd); // 2024.06.24 WOOIL - 자격을 저장한다.
				para.put(21, tsaStatus); // tsa여부를 저장한다.
				if("Y".equalsIgnoreCase(reSaveYn)){
					para.put(22, bfExdt); // orgexdt (어느 동의서에서 왔는지)
					para.put(23, bfSeq);  // orgseq (어느 동의서에서 왔는지)
				}else{
					para.put(22, ""); // orgexdt
					para.put(23, ""); // orgseq
				}
	
				sqlList.add(sql);
				paraList.add(para);
			
			}
			
			insString = sqlHelper.executeUpdate(sqlList, paraList);
			
			if(ResultSetHelper.getReturnCount(insString)<0){
				returnString = ResultSetHelper.getReturnDesc(insString);
			}else{
				returnString = "success";
			}

		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "putData", "Exception", ex.getLocalizedMessage());
			returnString = ex.getMessage().toString();
		}
		return returnString;
	}
	
	private String copyFile(SqlHelper sqlHelper, String srcFile, String dstFile) throws FileNotFoundException, IOException, Exception{
		FileInputStream fis = new FileInputStream(srcFile);
		int bytesAvailable = fis.available();
		byte[] buff = new byte[bytesAvailable];
		// read file and write it to form ...
		int bytesRead = fis.read(buff, 0, bytesAvailable);
		String returnString = sqlHelper.saveImageFile(dstFile, buff);
		return returnString;
	}
	
}
