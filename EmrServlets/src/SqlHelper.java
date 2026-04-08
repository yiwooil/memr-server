import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;


import org.apache.commons.dbcp.BasicDataSource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SqlHelper {

	Hospital hos = null;
	
	/*
	 * 병원에 대한 정보를 읽어놓는다.
	 * 최초 한번만 읽기 위하여 Map을 사용하여 메모리에 정보를 담아놓는다.
	 * 
	 */
	public SqlHelper(String hospitalId) throws Exception {
		//new LogWrite().debugWrite(getClass().getSimpleName(), "SqlHelper", "시작");
		hos = HospitalInformation.getHospital(hospitalId);
		//new LogWrite().debugWrite(getClass().getSimpleName(), "SqlHelper", "종료");
	}
	

	/*
	 * 병원정보를 반환하는 메소드
	 */
	public String getHospitalName(){
		return this.hos.hospitalName;
	}
	
	public String getDatabaseUrl(){
		return this.hos.databaseUrl;
	}
	
	public boolean getMasking() {
		return this.hos.maskYn.equals("y");
	}
	
	public String getScanUrl(){
		return this.hos.scanUrl;
	}
	
	public String getPreSaveUrl(){
		return this.hos.preSaveUrl;
	}
	
	public String getMP4Url(){
		return this.hos.mp4Url;
	}

	public String getPicUrl(){
		return this.hos.picUrl;
	}

	public String getHomeUrl(){
		return this.hos.homeUrl;
	}
	
	public boolean getInterfaceTableYn(){
		return this.hos.interfaceTableYn.equalsIgnoreCase("y");
	}
	
//	public boolean isTprTableSepYn(){
//		return this.hos.tprTableSepYn.equalsIgnoreCase("y");
//	}
//	
//	public boolean isTptTableBedInDateNoYn(){
//		return this.hos.tprTableBedInDateNoYn.equalsIgnoreCase("y");
//	}
	
//	public boolean isChartHistTableBedInDateNoYn(){
//		return this.hos.chartHistTableBedInDateNoYn.equalsIgnoreCase("y");
//	}

	public boolean isOracle(){
		return this.hos.databaseUrl.toLowerCase().startsWith("jdbc:oracle");
	}
	
	public boolean isJaincom(){
		return this.hos.emrCompany.toLowerCase().equals("jaincom");
	}
	
	public String getEmrScanUrl(){
		return this.hos.emrScanUrl;
	}
	
	public String getEmrScanUrlFormat(){
		return this.hos.emrScanUrlFormat;
	}
	
	public String getEmrDateFormat(){
		return this.hos.emrDateFormat;
	}
	
	public String getEmrResidFormat(){
		return this.hos.emrResidFormat;
	}
	
	public boolean getPwdSkpYn(){
		return this.hos.pwdSkpYn.equalsIgnoreCase("y");
	}
	
	public int getJainComPidLen(){
		int len = Integer.valueOf(this.hos.jainComPidLen);
		return len;
	}
	
	public String getFileNamePrefix(){
		return this.hos.fileNamePrefix;
	}
	
	public String getFileNamePrefixPresave(){
		return this.hos.fileNamePrefixPresave;
	}

	public String getFileNamePrefixPic(){
		return this.hos.fileNamePrefixPic;
	}
	
	public String getFileNamePrefixMP4(){
		return this.hos.fileNamePrefixMP4;
	}

	//	public String getHosLogoImageUrl(){
//		return this.hos.hosLogoImageUrl;
//	}
	


	/*
	 * Sql 문 처리 메소드 
	 * 
	 */
	public String executeQuery(String sql) {
		new LogWrite().debugWrite(getClass().getSimpleName(), "executeQuery", "시작(" + this.hos.databaseUrl + ")");
		String returnString = executeQuery(sql, null, null);
		new LogWrite().debugWrite(getClass().getSimpleName(), "executeQuery", "종료");
		return returnString;
	}

	public String executeQuery(String sql, HashMap<String, Object>paraValue, String paraString, String paraTypeString){
		HashMap<Integer, Object>para = new HashMap<Integer, Object>();
		HashMap<Integer, String>paraType = new HashMap<Integer, String>();
		
		// paraString 순서대로 파라메터를 만든다.
		if(paraString==null) paraString="";
		if(paraTypeString==null) paraTypeString="";
		String[] paraStringArray = paraString.split(",");
		String[] paraTypeStringArray = paraTypeString.split(",");
		for(int i=0;i<paraStringArray.length;i++){
			para.put(i+1, paraValue.get(paraStringArray[i]));
			if(paraTypeStringArray.length<i+1){
				paraType.put(i+1, "");
			}else{
				paraType.put(i+1, paraTypeStringArray[i]);
			}
		}
		
		return executeQuery(sql, para, paraType);
	}
	
	public String executeQuery(String sql, HashMap<Integer, Object>para, HashMap<Integer, String>paraType) {
		return executeQuery(sql, para, paraType, "1");
	}
	
	public String executeQuery(String sql, HashMap<Integer, Object>para, HashMap<Integer, String>paraType, String retver) {
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			String paraColType="";
			String paraColValue="";
			
			int rowCount=0;
			con = getDataSource(this.hos.databaseUrl).getConnection();
			pstmt = con.prepareStatement(sql);
			int paraCnt=0;
			if(para!=null) paraCnt=para.size();
			int paraTypeCnt=0;
			if(paraType!=null) paraTypeCnt=paraType.size();
			for(int i=1;i<=paraCnt;i++){
				if(i<=paraTypeCnt){
					paraColType=paraType.get(i);
					if("D".equalsIgnoreCase(paraColType)){
						paraColValue = (String)para.get(i);
						if("yyyy-mm-dd".equalsIgnoreCase(this.hos.emrDateFormat)){
							paraColValue = Utility.getFormattedDate(paraColValue, this.hos.emrDateFormat);
							pstmt.setObject(i, paraColValue);
						}else{
							pstmt.setObject(i, para.get(i));
						}
					}else{
						pstmt.setObject(i, para.get(i));
					}
				}else{
					pstmt.setObject(i, para.get(i));
				}
			}
			rs = pstmt.executeQuery();
			//
			String columnName="";
			String columnValue="";
			String orgColumnValue="";
			while (rs.next()) {
				rowCount++;
				columns = new JSONObject();
				for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
					columnName = rs.getMetaData().getColumnName(i+1).toLowerCase();
					columnValue = rs.getString(i+1);
					if (columnValue == null) columnValue = ""; // 2026.02.11 WOOIL - NULL인 경우 공백문자로...
					orgColumnValue = columnValue;
					// bit는 일자가 yyyy-mm-dd 임.
					// 이를 처리하기 위함임.
					if("yyyy-mm-dd".equalsIgnoreCase(this.hos.emrDateFormat)){
						if("bededt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("bedodt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("bthdt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("chkdt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("dodt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("exdt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("maxdodt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("mindodt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("odt".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}else if("order_date".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}
					}
					// bit는 시간이 hh:mm:ss 임.
					// 이를 처리하기 위함임.
					if("hh:mm:ss".equalsIgnoreCase(this.hos.emrTimeFormat)){
						if("chktm".equalsIgnoreCase(columnName)){
							columnValue = columnValue.replace(":", "");
							columnValue = columnValue.trim();
						}
					}
					// bit는 주민번호를 123456-1234567로 처리함
					// 이를 처리하기 위함임.
					if("123456-1234567".equalsIgnoreCase(this.hos.emrResidFormat)){
						if("resid".equalsIgnoreCase(columnName)){
							// 일단 비트사이트
							columnValue = columnValue.replace("-", "");
							columnValue = columnValue.trim();
						}
					}
					// 오공백을 제거해야 mEMR에서 정상으로 동작한다.
					if("pid".equalsIgnoreCase(columnName)){
						columnValue = columnValue.trim();
					}else if("wardid".equalsIgnoreCase(columnName)){
						columnValue = columnValue.trim();
					}else if("deptcd".equalsIgnoreCase(columnName)){
						columnValue = columnValue.trim();
					}
					columns.put(columnName, columnValue);
					
					//new LogWrite().debugWrite(getClass().getSimpleName(), "executeQueryLocal", "columnName=" + columnName + ", orgColumnValue=" + orgColumnValue + ", columnValue=" + columnValue);

				}
				rowData.add(columns);
			}
			rs.close();
			pstmt.close();
			con.close();
			// 리턴값과 메시지
			if("2".equalsIgnoreCase(retver)){
				columns = new JSONObject();
				columns.put("return_code",rowCount);
				columns.put("return_desc","ok");
				columns.put("return_rslt", rowData);
				
				return columns.toJSONString();
			}else{
				columns = new JSONObject();
				columns.put("return_code",rowCount);
				columns.put("return_desc","ok");
				status.add(columns);
				// 반환자료
				result.add(status);
				result.add(rowData);
				
				return result.toJSONString();
			}
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "executeQueryLocal", "Exception", ex.getLocalizedMessage());
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",-1);
			columns.put("return_desc",ex.getMessage());
			status.add(columns);
			// 반환자료
			result.add(status);
			
			return result.toJSONString();
		} finally {
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

	public String executeUpdate(String sql, HashMap<Integer, Object>para) {
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONObject columns = null;
		
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = getDataSource(this.hos.databaseUrl).getConnection();
			pstmt = con.prepareStatement(sql);
			int paraCnt=0;
			if(para!=null) paraCnt = para.size();
			for(int i=1;i<=paraCnt;i++){
				pstmt.setObject(i, para.get(i));
			}
			int count = pstmt.executeUpdate();
			pstmt.close();
			con.close();
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",count);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			
			return result.toJSONString();
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "executeUpdateLocal", "Exception", ex.getLocalizedMessage());
			new LogWrite().errorWrite(getClass().getSimpleName(), "executeUpdateLocal", "sql",sql);
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",-1);
			columns.put("return_desc",ex.getMessage());
			status.add(columns);
			// 반환자료
			result.add(status);
			
			return result.toJSONString();
		} finally {
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
	
	public String executeUpdate(String sql, HashMap<Integer, Object>para, String sql2, HashMap<Integer, Object>para2) {
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONObject columns = null;
		
		Connection con = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;

		try {
			con = getDataSource(this.hos.databaseUrl).getConnection();
			con.setAutoCommit(false);
			pstmt = con.prepareStatement(sql);
			int paraCnt=para.size();
			for(int i=1;i<=paraCnt;i++){
				pstmt.setObject(i, para.get(i));
			}
			int count = pstmt.executeUpdate();
			pstmt.close();
			//
			pstmt2 = con.prepareStatement(sql2);
			int paraCnt2=para2.size();
			for(int i=1;i<=paraCnt2;i++){
				pstmt2.setObject(i, para2.get(i));
			}
			count += pstmt2.executeUpdate();
			//
			pstmt2.close();
			con.commit();
			con.close();
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",count);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			
			return result.toJSONString();
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "executeUpdateLocal", "Exception", ex.getLocalizedMessage());
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",-1);
			columns.put("return_desc",ex.getMessage());
			status.add(columns);
			// 반환자료
			result.add(status);
			try {
				con.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			return result.toJSONString();
		} finally {
			if(pstmt2!=null) {
				try {
					pstmt2.close();
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
	
	public String executeUpdate(String sql, HashMap<Integer, Object>para, String sql2, List<HashMap<Integer, Object>>paraList2) {
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONObject columns = null;
		
		Connection con = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;

		try {
			con = getDataSource(this.hos.databaseUrl).getConnection();
			con.setAutoCommit(false);
			pstmt = con.prepareStatement(sql);
			int paraCnt=para.size();
			for(int i=1;i<=paraCnt;i++){
				pstmt.setObject(i, para.get(i));
			}
			int count = pstmt.executeUpdate();
			pstmt.close();
			//
			pstmt2 = con.prepareStatement(sql2);
			int paraListCnt= paraList2.size();
			for(int idx=0;idx<paraListCnt;idx++){
				HashMap<Integer, Object> para2 = paraList2.get(idx);
				int paraCnt2=para2.size();
				for(int i=1;i<=paraCnt2;i++){
					pstmt2.setObject(i, para2.get(i));
				}
				count += pstmt2.executeUpdate();
			}
			//
			pstmt2.close();
			con.commit();
			con.close();
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",count);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			
			return result.toJSONString();
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "executeUpdateLocal", "Exception", ex.getLocalizedMessage());
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",-1);
			columns.put("return_desc",ex.getMessage());
			status.add(columns);
			// 반환자료
			result.add(status);
			try {
				con.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			return result.toJSONString();
		} finally {
			if(pstmt2!=null) {
				try {
					pstmt2.close();
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
	
	public String executeUpdate(List<String> sqlList, List<HashMap<Integer, Object>>paraList) {
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONObject columns = null;
		
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = getDataSource(this.hos.databaseUrl).getConnection();
			con.setAutoCommit(false);
			int count = 0;
					
			int sqlCnt = sqlList.size();
			for(int idx=0;idx<sqlCnt;idx++){
				String sql = sqlList.get(idx);
				//new LogWrite().debugWrite(getClass().getSimpleName(), "executeUpdate", "sql["+idx+"]="+sql);
				HashMap<Integer, Object> para = paraList.get(idx);
				pstmt = con.prepareStatement(sql);
				int paraCnt=para.size();
				for(int i=1;i<=paraCnt;i++){
					pstmt.setObject(i, para.get(i));
				}
				count += pstmt.executeUpdate();
				pstmt.close();
			}
			
			con.commit();
			con.close();
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",count);
			columns.put("return_desc","ok");
			status.add(columns);
			// 반환자료
			result.add(status);
			
			return result.toJSONString();
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "executeUpdate", "Exception", ex.getLocalizedMessage());
			// 리턴값과 메시지
			columns = new JSONObject();
			columns.put("return_code",-1);
			columns.put("return_desc",ex.getMessage());
			status.add(columns);
			// 반환자료
			result.add(status);
			try {
				con.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			return result.toJSONString();
		} finally {
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
	
	/*
	 *  파일을 다루는 메소드
	 *  
	 */
	public String getFile(String fileName) {
		try {
			StringBuffer buffer = new StringBuffer();
			String filePath = this.hos.homeUrl + fileName;
			new LogWrite().debugWrite(getClass().getSimpleName(), "getFileLocal", "filePath="+filePath);
	        BufferedReader in = new BufferedReader(new FileReader(filePath));
	        while(true) {
	        	int c = in.read();
	        	if (c==-1) break;
	        	buffer.append((char)c);
	        }
	        in.close();
	        return buffer.toString();
	    } 
		catch(Exception e){
			new LogWrite().errorWrite(getClass().getSimpleName(), "getFileLocal", "Exception", e.getLocalizedMessage());
	        return(null);
	    }
		//return null;
	}
	
	public byte[] getEmrScanFile(String fileName) {
		try {
			String filePath = this.hos.scanUrl + fileName;
			if("empty".equalsIgnoreCase(this.hos.emrScanUrl)){
				// emrScanUrl의 값이 empty이면 fileName을 그대로 사용한다.
				filePath = fileName;
			}
			return getFileForByte(filePath);
		}
		catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getEmrScanFileLocal", "Exception", e.getLocalizedMessage());
			return null;
		}
	}
	
	public byte[] getHosEmrScanFile(String fileName) {
		try {
			String filePath = this.hos.emrScanUrl + fileName;
			if("".equalsIgnoreCase(this.hos.emrScanUrl)){
				// emrScanUrl에 값이 없다는 것은 scanUrl을 같이 사용한다는 것임.
				filePath = this.hos.scanUrl + fileName;
			}
			if("empty".equalsIgnoreCase(this.hos.emrScanUrl)){
				// emrScanUrl의 값이 empty이면 fileName을 그대로 사용한다.
				filePath = fileName;
			}
			return getFileForByte(filePath);
		}
		catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getHosEmrScanFileLocal", "Exception", e.getLocalizedMessage());
			return null;
		}
	}

	public byte[] getPreSavedCCFFile(String fileName) {
		try {
			String filePath = this.hos.preSaveUrl + fileName;
			if("empty".equalsIgnoreCase(this.hos.emrScanUrl)){
				// emrScanUrl의 값이 empty이면 fileName을 그대로 사용한다.
				filePath = fileName;
			}
			//new LogWrite().debugWrite(getClass().getSimpleName(), "getPreSavedCCFFileLocal", "filePath="+filePath);
			return getFileForByte(filePath);
		}
		catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPreSavedCCFFileLocal", "Exception", e.getLocalizedMessage());
			return null;
		}
	}
	
	public byte[] getPreSavedCCFMP4File(String fileName) {
		try {
			String filePath = this.hos.mp4Url + fileName;
			if("empty".equalsIgnoreCase(this.hos.emrScanUrl)){
				// emrScanUrl의 값이 empty이면 fileName을 그대로 사용한다.
				filePath = fileName;
			}
			return getFileForByte(filePath);
		}
		catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPreSavedCCFMP4FileLocal", "Exception", e.getLocalizedMessage());
			return null;
		}
	}
	
	public byte[] getPicFile(String fileName) {
		try {
			String filePath = this.hos.picUrl + fileName;
			if("empty".equalsIgnoreCase(this.hos.emrScanUrl)){
				// emrScanUrl의 값이 empty이면 fileName을 그대로 사용한다.
				filePath = fileName;
			}
			new LogWrite().debugWrite(getClass().getSimpleName(), "getPicFileLocal", "filePath="+filePath);
			return getFileForByte(filePath);
		}
		catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPicFileLocal", "Exception", e.getLocalizedMessage());
			return null;
		}
	}
	
//	public byte[] getHosLogoImageFile(){
//		try {
//			String filePath = this.hos.hosLogoImageFile;
//			return getFileForByte(filePath);
//			/*
//			File file = new File(filePath);
//			byte buffer[] = new byte[(int)file.length()];
//			BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath));
//			input.read(buffer,0,buffer.length);
//			input.close();
//			return buffer;
//			*/
//		}
//		catch(Exception e) {
//			new LogWrite().errorWrite(getClass().getSimpleName(), "getHosImageFileLocal", "Exception", e.getLocalizedMessage());
//			return null;
//		}
//	}
	
	public byte[] getFileForByte(String filePath) throws Exception {
		File file = new File(filePath);
		if(file.length()==0){
			return null;
		}else{
			byte buffer[] = new byte[(int)file.length()];
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath));
			input.read(buffer,0,buffer.length);
			input.close();
			return buffer;
		}
	}

	/*
	private String saveFileLocal(String fileName, byte[] data){
		try{
			File file = new File(fileName);
			File dir=file.getParentFile();
			if(dir!=null){
				// 폴더가 없으면 생성
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(fileName));
			out.write(data);
			out.close();
			return "";
		}catch(Exception e){
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveFileLocal", "Exception", e.getLocalizedMessage());
			return e.getMessage().toString();
		}
	}
	*/
	
	public String saveImageFile(String fileName, byte[] data){
		try{
			File file = new File(fileName);
			File dir=file.getParentFile();
			if(dir!=null){
				// 폴더가 없으면 생성
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(fileName));
			out.write(data);
			out.close();
			return "";
		}catch(Exception e){
			new LogWrite().errorWrite(getClass().getSimpleName(), "saveImageFileLocal", "Exception", e.getLocalizedMessage());
			return e.getMessage().toString();
		}
	}
	
	public boolean isFile(String fileType, String fileName){
		String filePath = "";
		if("mp4".equalsIgnoreCase(fileType)){
			filePath = this.hos.mp4Url + fileName;
		}else if("pic".equalsIgnoreCase(fileType)){
			filePath = this.hos.picUrl + fileName;
		}else if("ccf".equalsIgnoreCase(fileType)){
			filePath = this.hos.homeUrl + fileName;
		}else {
			filePath = this.hos.scanUrl + fileName;
		}
		File file = new File(filePath);
		return file.isFile();
	}
	

	/*
	 * Basecamp에 대한 connection을 DataSource를 사용해서 가져온다.
	 * 
	 */
	public static DataSource getBasecampDataSource() {
		String connectionUrl = Utility.getBasecampDbConnectionUrl();
		return MyBasecampDataSource.getDataSource(connectionUrl);
	}
	
	static class MyBasecampDataSource {
		private static BasicDataSource dataSource = null;
		private static String conUrl = null;
		private static DataSource getDataSource(String connectionUrl) {
			if (dataSource==null){
				dataSource = new BasicDataSource();
				dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				dataSource.setUrl(connectionUrl);
				conUrl = connectionUrl;
			}
			// 혹시 connectionUrl이 변경되었으면...
			if(conUrl.equals(connectionUrl)==false){
				dataSource.setUrl(connectionUrl);
				conUrl = connectionUrl;
			}
			return dataSource;
		}
	}	
	
	/***
	 * DataSource를 가져온다.
	 * 
	 * @param connectionUrl
	 * @return
	 */
	public static DataSource getDataSource(String connectionUrl) {
		return MyDataSource.getDataSource(connectionUrl);
	}
	
	static class MyDataSource {
		private static HashMap<String,BasicDataSource> dataSourceMap = null;
		private static DataSource getDataSource(String connectionUrl) {
			if(dataSourceMap==null) dataSourceMap = new HashMap<String,BasicDataSource>();
			if(!dataSourceMap.containsKey(connectionUrl)){
				BasicDataSource dataSource = new BasicDataSource();
				if(connectionUrl.toLowerCase().startsWith("jdbc:oracle")){
					// ORACLE
					dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
				}else{
					// MS-SQL SERVER
					dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				}
				dataSource.setUrl(connectionUrl);
				dataSourceMap.put(connectionUrl, dataSource);
			}
			return dataSourceMap.get(connectionUrl);
		}
		/*
		private static BasicDataSource dataSource = null;
		private static String conUrl = null;
		private static DataSource getDataSource(String connectionUrl) {
			if (dataSource==null){
				System.out.println(Utility.getCurrentDateTime() + " DataSource생성시작");
				dataSource = new BasicDataSource();
				if(connectionUrl.toLowerCase().startsWith("jdbc:oracle")){
					// ORACLE
					dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
				}else{
					// MS-SQL SERVER
					dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				}
				dataSource.setUrl(connectionUrl);
				conUrl = connectionUrl;
				System.out.println(Utility.getCurrentDateTime() + " DataSource생성완료");
			}
			// 혹시 connectionUrl이 변경되었으면...
			if(conUrl.equals(connectionUrl)==false){
				dataSource.setUrl(connectionUrl);
				conUrl = connectionUrl;
			}
			return dataSource;
		}
		*/
	}
	
}
