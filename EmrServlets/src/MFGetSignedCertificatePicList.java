import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetSignedCertificatePicList implements MFGet {
	private static MFGetSignedCertificatePicList mInstance=null;
	private MFGetSignedCertificatePicList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetSignedCertificatePicList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetSignedCertificatePicList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String exdt = (String)param.get("exdt");
		String seq = (String)param.get("seq");

		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs2 = null;

		try {
			String sql="";
			String sql2="";
			
			SqlHelper sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			
			new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "pid="+pid+", exdt="+exdt+", seq="+seq);
			if(interfaceTableYn){
				sql =
					"select '사진파일' title, file_path path,scan_date exdt,scan_seq seq,bf_scan_date bfexdt,bf_scan_seq bfseq " +
					"  from signed_consent_form_hist g02 " +
					" where g02.pat_id=?" +
					"   and g02.scan_date=?" +
					"   and g02.scan_seq=?" +
					"   and g02.scan_class='ZZ01'";
			}else{
				sql =
					"select '사진파일' title, path,exdt,seq,bfexdt,bfseq " +
					"  from tg02 g02 " +
					" where g02.pid=?" +
					"   and g02.exdt=?" +
					"   and g02.seq=?" +
					"   and g02.rptcd='ZZ01'";
			}
			
			int rowCount=0;
			con = sqlHelper.getDataSource(sqlHelper.getDatabaseUrl()).getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, pid);
			pstmt.setString(2, exdt);
			pstmt.setString(3, seq);
			rs = pstmt.executeQuery();
			
			//String path="";
			String picPath="";
			String bfExdt="";
			String bfSeq="";
			
			// 동의서가 있으면 진행.
			if (rs.next()) {
				new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "exdt="+rs.getString("exdt")+", seq="+rs.getString("seq")+", bfexdt="+rs.getString("bfexdt")+", bfseq"+rs.getString("bfseq"));
				//path=rs.getString("path");
				// 임시저장내역에 대한 연결키
				bfExdt=rs.getString("bfexdt");
				bfSeq=rs.getString("bfseq");
				// 동의서에 연결된 사진
				int idx=0;
				if(interfaceTableYn){
					sql2 = "select file_path pic_path from signed_consent_form_pic_hist where pat_id=? and scan_date=? and scan_seq=? and scan_class='ZZ01' order by pic_seq";
				}else{
					sql2 = "select path pic_path from tg02pic where pid=? and exdt=? and seq=? and rptcd='ZZ01' order by seqno";
				}
				pstmt2 = con.prepareStatement(sql2);
				pstmt2.setString(1, pid);
				pstmt2.setString(2, exdt);
				pstmt2.setString(3, seq);
				rs2 = pstmt2.executeQuery();
				while(rs2.next()){
					idx++;
					picPath=rs2.getString("pic_path");

					rowCount++;
					columns = new JSONObject();
					for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
						columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
					}
					columns.put("pic_path", picPath);
					columns.put("pic_idx", idx);
					rowData.add(columns);
				}
				rs2.close();
				pstmt2.close();
				/*
				while(true){
					idx++;
					picPath=path + "." + idx + ".png";
					boolean isFile = sqlHelper.isFile("pic", picPath);
					//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "picPath = " + picPath + ", " + isFile);
					if(!isFile)break;

					// 파일이 있는 경우만...
					rowCount++;
					columns = new JSONObject();
					for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
						columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
					}
					columns.put("pic_path", picPath);
					columns.put("pic_idx", idx);
					rowData.add(columns);
				}
				*/
			}
			rs.close();
			pstmt.close();
			
			// 임시저장된 내역이 있는지 찾아본다.
			String orgExdt="";
			String orgSeq="";
			while(true){
				// 임시저장내역이 없으면 종료
				if("".equalsIgnoreCase(bfExdt)) break;
				if("".equalsIgnoreCase(bfSeq)) break;
				// 찾아본다.
				if(interfaceTableYn){
					sql =
						"select '사진파일' title, file_path path,scan_date exdt,scan_seq seq,bf_scan_date bfexdt,bf_scan_seq bfseq " +
						"  from presaved_consent_form_hist g02 " +
						" where g02.pat_id=?" +
						"   and g02.scan_date=?" +
						"   and g02.scan_seq=?" +
						"   and g02.scan_class='ZZ01'";
				}else{
					sql =
						"select '사진파일' title, path,exdt,seq,bfexdt,bfseq " +
						"  from tg02t g02 " +
						" where g02.pid=?" +
						"   and g02.exdt=?" +
						"   and g02.seq=?" +
						"   and g02.rptcd='ZZ01'";
				}
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, pid);
				pstmt.setString(2, bfExdt);
				pstmt.setString(3, bfSeq);
				rs = pstmt.executeQuery();
				
				//path="";
				picPath="";
				if(!rs.next()){
					bfExdt="";
					bfSeq="";
				}else{
					new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "exdt="+rs.getString("exdt")+", seq="+rs.getString("seq")+", bfexdt="+rs.getString("bfexdt")+", bfseq"+rs.getString("bfseq"));
					// 사진을 찾는다.
					int idx=0;
					if(interfaceTableYn){
						sql2 = "select file_path pic_path from presaved_consent_form_pic_hist where pat_id=? and scan_date=? and scan_seq=? and scan_class='ZZ01' order by pic_seq";
					}else{
						sql2 = "select path pic_path from tg02tpic where pid=? and exdt=? and seq=? and rptcd='ZZ01' order by seqno";
					}
					pstmt2 = con.prepareStatement(sql2);
					pstmt2.setString(1, pid);
					pstmt2.setString(2, bfExdt);
					pstmt2.setString(3, bfSeq);
					rs2 = pstmt2.executeQuery();
					while(rs2.next()){
						idx++;
						picPath=rs2.getString("pic_path");

						rowCount++;
						columns = new JSONObject();
						for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
							columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
						}
						columns.put("pic_path", picPath);
						columns.put("pic_idx", idx);
						rowData.add(columns);
					}
					rs2.close();
					pstmt2.close();
					//path=rs.getString("path");
					bfExdt=rs.getString("bfexdt");
					bfSeq=rs.getString("bfseq");
					orgExdt=rs.getString("exdt");
					orgSeq=rs.getString("seq");
					// 자료가 잘못발생한 경우가 있음.
					if(bfExdt.equalsIgnoreCase(orgExdt) && bfSeq.equalsIgnoreCase(orgSeq)){
						bfExdt="";
						bfSeq="";
					}
					/*
					while(true){
						idx++;
						picPath=path + "." + idx + ".png";
						boolean isFile = sqlHelper.isFile("pic", picPath);
						//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "picPath = " + picPath + ", " + isFile);
						if(!isFile)break;

						// 파일이 있는 경우만...
						rowCount++;
						columns = new JSONObject();
						for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
							columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
						}
						columns.put("pic_path", picPath);
						columns.put("pic_idx", idx);
						rowData.add(columns);
					}
					*/
				}
				rs.close();
				pstmt.close();
			}
			
			con.close();
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",rowCount);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			result.add(rowData);
			
			return result.toJSONString();
		} catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",-1);
			columns.put("return_desc",ex.getMessage());
			status.add(columns);
			// 반환자료
			result.add(status);
			
			return result.toJSONString();
		} finally {
			if(rs2!=null) {
				try {
					rs2.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(pstmt2!=null) {
				try {
					pstmt2.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			if(con!=null) {
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
	}

}
