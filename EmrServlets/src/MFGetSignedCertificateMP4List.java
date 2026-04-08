import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetSignedCertificateMP4List implements MFGet {
	private static MFGetSignedCertificateMP4List mInstance=null;
	private MFGetSignedCertificateMP4List(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetSignedCertificateMP4List getInstance(){
		if(mInstance==null){
			mInstance = new MFGetSignedCertificateMP4List();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		
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
			
			if(interfaceTableYn){
				sql =
					"select '녹음파일' title, file_path path,scan_date exdt,scan_seq seq,bf_scan_date bfexdt,bf_scan_seq bfseq " +
					"  from signed_consent_form_hist g02 " +
					" where g02.pat_id=?" +
					"   and g02.scan_date=?" +
					"   and g02.scan_seq=?" +
					"   and g02.scan_class='ZZ01'";
			}else{
				sql =
					"select '녹음파일' title, path,exdt,seq,bfexdt,bfseq " +
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
			
			String mp4Path="";
			//String path="";
			String bfExdt="";
			String bfSeq="";
					
			if (rs.next()) {
				//mp4Path=rs.getString("mp4_path");
				//path=rs.getString("path");
				bfExdt=rs.getString("bfexdt");
				bfSeq=rs.getString("bfseq");			
				int idx=0;
				if(interfaceTableYn){
					sql2 = "select file_path mp4_path from signed_consent_form_mp4_hist where pat_id=? and scan_date=? and scan_seq=? and scan_class='ZZ01' order by mp4_seq";
				}else{
					sql2 = "select path mp4_path from tg02mp4 where pid=? and exdt=? and seq=? and rptcd='ZZ01' order by seqno";
				}
				pstmt2 = con.prepareStatement(sql2);
				pstmt2.setString(1, pid);
				pstmt2.setString(2, exdt);
				pstmt2.setString(3, seq);
				rs2 = pstmt2.executeQuery();
				while(rs2.next()){
					idx++;
					mp4Path=rs2.getString("mp4_path");

					rowCount++;
					columns = new JSONObject();
					for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
						columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
					}
					columns.put("mp4_path", mp4Path);
					columns.put("mp4_idx", idx);
					rowData.add(columns);
				}
				rs2.close();
				pstmt2.close();
				/*
				while(true){
					idx++;
					mp4Path=path + "." + idx + ".mp4";
					boolean isFile = sqlHelper.isFile("mp4", mp4Path);
					//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "mp4Path = " + mp4Path + ", " + isFile);
					if(!isFile) break;
					// 녹음 파일이 있는 경우만...
					rowCount++;
					columns = new JSONObject();
					for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
						columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
					}
					columns.put("mp4_path", mp4Path);
					columns.put("mp4_idx", idx);
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
				if("".equalsIgnoreCase(bfExdt)) break;
				if("".equalsIgnoreCase(bfSeq)) break;
				if(interfaceTableYn){
					sql =
						"select '임시녹음' title, file_path path,scan_date exdt,scan_seq seq,bf_scan_date bfexdt,bf_scan_seq bfseq " +
						"  from presaved_consent_form_hist g02 " +
						" where g02.pat_id=?" +
						"   and g02.scan_date=?" +
						"   and g02.scan_seq=?" +
						"   and g02.scan_class='ZZ01'";
				}else{
					sql =
						"select '임시녹음' title, path,exdt,seq,bfexdt,bfseq " +
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
				
				if(!rs.next()){
					bfExdt="";
					bfSeq="";					
				}else{
					//mp4Path=rs.getString("mp4_path");
					// 녹음내용을 찾는다.
					int idx=0;
					if(interfaceTableYn){
						sql2 = "select file_path mp4_path from presaved_consent_form_mp4_hist where pat_id=? and scan_date=? and scan_seq=? and scan_class='ZZ01' order by mp4_seq";
					}else{
						sql2 = "select path mp4_path from tg02tmp4 where pid=? and exdt=? and seq=? and rptcd='ZZ01' order by seqno";
					}
					pstmt2 = con.prepareStatement(sql2);
					pstmt2.setString(1, pid);
					pstmt2.setString(2, bfExdt);
					pstmt2.setString(3, bfSeq);
					rs2 = pstmt2.executeQuery();
					while(rs2.next()){
						idx++;
						mp4Path=rs2.getString("mp4_path");

						rowCount++;
						columns = new JSONObject();
						for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
							columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
						}
						columns.put("mp4_path", mp4Path);
						columns.put("mp4_idx", idx);
						rowData.add(columns);
					}
					rs2.close();
					pstmt2.close();
					//path=rs.getString("path");
					bfExdt=rs.getString("bfexdt");
					bfSeq=rs.getString("bfseq");
					orgExdt=rs.getString("exdt");
					orgSeq=rs.getString("seq");
					// 자료가 잘못발생하는 경우가 있음.
					if(bfExdt.equalsIgnoreCase(orgExdt) && bfSeq.equalsIgnoreCase(orgSeq)){
						bfExdt="";
						bfSeq="";
					}
					/*
					while(true){
						idx++;
						mp4Path=path + "." + idx + ".mp4";
						boolean isFile = sqlHelper.isFile("mp4", mp4Path);
						//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "mp4Path = " + mp4Path + ", " + isFile);
						if(!isFile) break;
						// 녹음 파일이 있는 경우만...
						rowCount++;
						columns = new JSONObject();
						for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
							columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
						}
						columns.put("mp4_path", mp4Path);
						columns.put("mp4_idx", idx);
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
