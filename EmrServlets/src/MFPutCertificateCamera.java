import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MFPutCertificateCamera implements MFPut {
	private static MFPutCertificateCamera mInstance=null;
	private MFPutCertificateCamera(){
		
	}
	
	public static MFPutCertificateCamera getInstance(){
		if(mInstance==null){
			mInstance = new MFPutCertificateCamera();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String rptcd = (String)param.get("rptcd");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String bdiv = (String)param.get("bdiv");
		String fileNameList = (String)param.get("filename"); // 2024.07.15 WOOIL - fileName -> fileNameList
		String applyExdt = (String)param.get("apply_exdt"); // 2026.01.30 WOOIL - sysdt와 분리한다. 사용자가 exdt를 수정할 수 있게 함.
		String seqList = (String)param.get("seq"); // 2024.07.15 WOOIL - seq -> seqList
		String sysdt = (String)param.get("sysdt");
		String systm = (String)param.get("systm");
		String dptcd = (String)param.get("dptcd"); // 2024.06.21 WOOIL -
		String drid = (String)param.get("drid"); // 2024.06.21 WOOIL -
		String qfycd = (String)param.get("qfycd"); // 2024.06.24 WOOIL - 

		if (pid == null) pid = "";
		if (bededt == null) bededt = "";
		if (bdiv == null) bdiv = "";
		if (dptcd == null) dptcd = ""; // 2024.06.21 WOOIL -
		if (drid == null) drid = ""; // 2024.06.21 WOOIL -
		if (qfycd == null) qfycd = ""; // 2024.06.24 WOOIL -
		if (applyExdt == null) applyExdt = sysdt; // 2026.01.30 WOOIL - 앱이 나중에 업데이트되는 경우를 대비함.
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "pid="+pid+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  sysdt="+sysdt+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  systm="+systm+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  applyExdt="+applyExdt+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  seqList="+seqList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  fileNameList="+fileNameList+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  dptcd="+dptcd+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  drid="+drid+"");
		new LogWrite().debugWrite(getClass().getSimpleName(), "putData", "  qfycd="+qfycd+"");
		
		List<String> sqlList = new ArrayList<String>();
		List<HashMap<Integer, Object>> paraList = new ArrayList<HashMap<Integer, Object>>(); 
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			String inoutdiv = "I"; // 
			if(bdiv.equals("1")) inoutdiv = "O"; // 외래
			
			
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String fileNamePrefix = sqlHelper.getFileNamePrefix();
			

			String insString="";
			
			// 2024.07.02 WOOIL - 여러 페이지를 한 트랜젝션으로 저장하도록 수정
			String[] arySeq = seqList.split(",", -1); // 2024.07.15 WOOIL
			String[] aryFileName = fileNameList.split(",", -1); // 2024.07.15 WOOIL
			
			int ccfCount = aryFileName.length;
			for(int ccfidx=0 ; ccfidx < ccfCount ; ccfidx++){
				
				String seq = arySeq[ccfidx];
				String fileName = aryFileName[ccfidx];
			
				// 테이블 TG02에 저장한다.
				StringBuilder sb;
				sb = new StringBuilder();
				
				sb.append("insert into tg02(pid,bdiv,exdt,seq,rptcd,path,sysdt,systm,empid,bfexdt,bfseq,path2,rptcd2,rptnm2,ccf_id,sub_page_list,sub_page_no,dptcd,drid,qfycd,tsa_status,orgexdt,orgseq)");
				sb.append("values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

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
				para.put(10, "");
				para.put(11, "");
				para.put(12, "");
				para.put(13, "");
				para.put(14, ""); // rptnm
				para.put(15, ""); 
				para.put(16, "");
				para.put(17, "");
				para.put(18, dptcd); // 2024.06.21 WOOIL - 진료과 정보를 저장한다.
				para.put(19, drid); // 2024.06.21 WOOIL - 의사 정보를 저장한다.
				para.put(20, qfycd); // 2024.06.24 WOOIL - 자격을 저장한다.
				para.put(21, ""); // tsa여부를 저장한다.
				para.put(22, ""); // orgexdt
				para.put(23, ""); // orgseq
	
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

}
