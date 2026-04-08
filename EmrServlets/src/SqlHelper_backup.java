//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.sql.DataSource;
//
//
//import org.apache.commons.dbcp.BasicDataSource;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//
//import com.metrosoft.chocolate.Cacao;


public class SqlHelper_backup {
//	class Hospital {
//		public String hospitalId;
//		public String hospitalName;
//		public boolean useChocolate;
//		public String chocolateUrl;
//		public String databaseUrl;
//		public String homeUrl;
//		public String maskYn;
//		public String scanUrl;
//		public String interfaceTableYn;
//	}
//	static Map<String, Hospital> hospitals = null; // 병원정보를 데이터베이스에서 한번만 읽기 위한 처리.
//	Hospital hos = null;
//	private boolean reuse=false;
//	
//	/***
//	 * 병원에 대한 정보를 읽어놓는다.
//	 * 최초 한번만 읽기 위하여 Map을 사용하여 메모리에 정보를 담아놓는다.
//	 * 
//	 * @param hospitalId
//	 * @throws SQLException
//	 */
//	public SqlHelper_backup(String hospitalId) throws SQLException {
//		if (hospitals!=null) {
//			if (hospitals.containsKey(hospitalId)) {
//				hos = hospitals.get(hospitalId);
//			}
//		}
//		if (hos!=null) {
//			reuse=true;
//			return;
//		}
//		if (hospitals==null) {
//			hospitals = new HashMap<String, Hospital>();
//		}
//
//		hos = new Hospital();
//		this.hos.hospitalId=hospitalId;
//		this.hos.hospitalName="";
//		this.hos.useChocolate=false;
//		this.hos.chocolateUrl="";
//		this.hos.databaseUrl="";
//		this.hos.homeUrl="";
//		this.hos.maskYn="";
//		this.hos.scanUrl="";
//		this.hos.interfaceTableYn="";
//		
//		Connection con = null;
//		PreparedStatement stmt = null;
//		ResultSet rs = null;
//		
//		try {
//
//			con = getBasecampDataSource().getConnection();
//			String sql = 
//					"select hospital_name,chocolate_use_yn,chocolate_url,database_url,home_url,isnull(mask_yn,'') as mask_yn,isnull(scan_url,'') as scan_url,isnull(interface_table_yn,'') as interface_table_yn " +
//					"  from Hospitals " +
//					" where hospital_id=?";
//			stmt = con.prepareStatement(sql);
//			stmt.setString(1, hospitalId);
//			rs = stmt.executeQuery();
//			if (rs.next()) {
//				this.hos.hospitalName = rs.getString("hospital_name");
//				this.hos.useChocolate = rs.getString("chocolate_use_yn").equalsIgnoreCase("y");
//				this.hos.chocolateUrl = rs.getString("chocolate_url");
//				this.hos.databaseUrl = rs.getString("database_url");
//				this.hos.homeUrl = rs.getString("home_url");
//				this.hos.maskYn = rs.getString("mask_yn");
//				this.hos.scanUrl = rs.getString("scan_url");
//				this.hos.interfaceTableYn = rs.getString("interface_table_yn");
//			}
//			
//			hospitals.put(hospitalId, hos);
//			
//		} finally {
//			if(rs!=null) {
//				try {
//					rs.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//			if(stmt!=null) {
//				try {
//					stmt.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//			if(con!=null) {
//				try {
//					con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//		}
//	}
//	
//
//	/***
//	 * 병원정보를 메모리에 올려놓고 사용하므로 병원정보가 변경되면 메모리에 다시 올려한다.
//	 * 이를 위한 메소드임
//	 * 
//	 * @param hospitalId
//	 */
//	public void resetHospitalInformation(String hospitalId) {
//		if (this.hospitals!=null) {
//			if (this.hospitals.containsKey(hospitalId)) {
//				this.hospitals.remove(hospitalId);
//			}
//		}
//	}
//	
//	public boolean getReuse() {
//		return this.reuse;
//	}
//	
//	public String getHospitalName(){
//		return this.hos.hospitalName;
//	}
//	
//	public boolean getMasking() {
//		return this.hos.maskYn.equals("y");
//	}
//	
//	public String getScanUrl(){
//		return this.hos.scanUrl;
//	}
//	
//	public String getHomeUrl(){
//		return this.hos.homeUrl;
//	}
//	
//	public boolean getInterfaceTableYn(){
//		return this.hos.interfaceTableYn.equalsIgnoreCase("y");
//	}
//	
//	/***
//	 * SELECT문을 수행하는 메소드
//	 * 병원의 정보를 읽어보아서 직접 SELECT할 수도 있고 Chocolate 서버에 접속하여 사용할 수 있다.
//	 * 이곳에서 분기하여 호출한다.
//	 * 
//	 * @param sql
//	 * @return
//	 */
//	public String executeQuery(String sql) {
//		if (this.hos.useChocolate) {
//			return executeQueryRemote(sql);
//		}
//		else {
//			return executeQueryLocal(sql);
//		}		
//	}
//
//	/***
//	 * SELECT문을 수행하는 메소드
//	 * 병원의 정보를 읽어보아서 직접 SELECT할 수도 있고 Chocolate 서버에 접속하여 사용할 수 있다.
//	 * 이곳에서 분기하여 호출한다.
//	 * 
//	 * PrepareStatement를 사용하기 위하여 파라메터를 넘긴다.
//	 * 
//	 * @param sql
//	 * @param para
//	 * @return
//	 */
//	public String executeQuery(String sql, HashMap<Integer, Object>para) {
//		if (this.hos.useChocolate) {
//			return executeQueryRemote(sql,para);
//		}
//		else {
//			return executeQueryLocal(sql,para);
//		}	
//	}
//
//	/***
//	 * UPDATE문을 수행하는 메소트
//	 * 병원의 정보를 읽어보아서 직접 UPDATE 할 수도 있고 Chocolate 서버에 접속하여 사용할 수 있다.
//	 * 이곳에서 분기하여 호출한다.
//	 * 
//	 * @param sql
//	 * @return
//	 */
//	public String executeUpdate(String sql) {
//		if (this.hos.useChocolate) {
//			return executeUpdateRemote(sql);
//		}
//		else {
//			return executeUpdateLocal(sql);
//		}
//	}
//
//	/***
//	 * UPDATE문을 수행하는 메소트
//	 * 병원의 정보를 읽어보아서 직접 UPDATE 할 수도 있고 Chocolate 서버에 접속하여 사용할 수 있다.
//	 * 이곳에서 분기하여 호출한다.
//	 * 
//	 * PrepareStatement를 사용하기 위하여 파라메터를 넘긴다.
//
//	 * @param sql
//	 * @param para
//	 * @return
//	 */
//	public String executeUpdate(String sql, HashMap<Integer, Object>para) {
//		if (this.hos.useChocolate) {
//			return executeUpdateRemote(sql,para);
//		}
//		else {
//			return executeUpdateLocal(sql,para);
//		}
//	}
//	
//	/***
//	 * 서버에 있는 파일을 일거오는 메소드
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	public String getFile(String fileName) {
//		if (this.hos.useChocolate) {
//			return getFileRemote(fileName);
//		}
//		else {
//			return getFileLocal(fileName);
//		}
//		
//	}
//	
//	/***
//	 * 서버에 있는 이미지 파일을 읽어오는 메소드
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	public byte[] getEmrScanFile(String fileName) {
//		if (this.hos.useChocolate) {
//			return getEmrScanFileRemote(fileName);
//		}
//		else {
//			return getEmrScanFileLocal(fileName);
//		}
//		
//	}
//	
//	/***
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	public byte[] getEmrScanFile_test(String fileName) {
//		if (this.hos.useChocolate) {
//			// 베이스켐프의 해당폴더에 자료가 있는지 먼저 찾아본다.
//			byte[] ret=getEmrScanFileBasecamp(fileName);
//			// 없으면 병원서버에서 읽어와서 베이스켐프에 저장한다.
//			if(ret==null){
//				// 병원서버에 접속하여 읽는다.
//				ret=getEmrScanFileRemote(fileName);
//				// 베이스캠프에 저장한다.
//				if(ret!=null){
//					this.saveScanFileBasecamp(fileName, ret);
//				}
//			}
//			// 자료를 반환한다.
//			return ret;
//		}
//		else {
//			return getEmrScanFileLocal(fileName);
//		}
//		
//	}
//
//	/***
//	 * 서버에 파일을 저장한다.
//	 * 
//	 * @param fileName
//	 * @param data
//	 * @return
//	 */
//	public String saveFile(String fileName, byte[] data){
//		if (this.hos.useChocolate) {
//			return saveFileRemote(fileName, data);
//		}
//		else {
//			return saveFileLocal(fileName, data);
//		}
//	}
//	
//	/***
//	 * 서버에 이미지파일을 저장한다.
//	 * 
//	 * @param fileName
//	 * @param data
//	 * @return
//	 */
//	public String saveImageFile(String fileName, byte[] data){
//		if (this.hos.useChocolate) {
//			return saveImageFileRemote(fileName, data);
//		}
//		else {
//			return saveImageFileLocal(fileName, data);
//		}
//	}
//	
//	/***
//	 * Chocolate서버가 정상동작하는지 알아보기 위한 메소드
//	 * 파라메터로 넘긴 스트링을 그대로 반환한다.
//	 * 
//	 * @param echo
//	 * @return
//	 */
//	public String getEchoString(String echo) {
//		Cacao cacao = null;
//		String resultString = "";
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//		}
//		catch(Exception ex) {
//			resultString="(1)SqlHelper.getEchoString : " + ex.getMessage();
//			return resultString;
//		}
//		try {
//			resultString = cacao.echo(echo);
//		} catch (RemoteException e) {
//			resultString="(2)SqlHelper.getEchoString : " + e.getMessage();
//		}
//		return resultString;
//	}
//	
//	/***
//	 * Chocolate 서버를 거쳐서 select하는 메소드
//	 * 
//	 * @param sql
//	 * @return
//	 */
//	private String executeQueryRemote(String sql) {
//		Cacao cacao = null;
//		String resultString = "";
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//			resultString = cacao.executeQuery(sql,this.hos.databaseUrl);
//		}
//		catch(Exception ex) {
//			resultString=ExceptionHelper.toJSONString(ex);
//		}
//		return resultString;
//	}
//	
//	/***
//	 * Chocolate 서버를 거쳐서 select하는 메소드
//	 *
//	 * PreparedStatement 를 사용하기 위한 메소드
//	 * 
//	 * @param sql
//	 * @param para
//	 * @return
//	 */
//	private String executeQueryRemote(String sql,HashMap<Integer, Object>para) {
//		Cacao cacao = null;
//		String resultString = "";
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//			resultString = cacao.executeQuery(sql,para,this.hos.databaseUrl);
//		}
//		catch(Exception ex) {
//			resultString=ExceptionHelper.toJSONString(ex);
//		}
//		return resultString;
//	}
//	
//	/***
//	 * Chocolate 서버를 거쳐서 update하는 메소드
//	 * 
//	 * @param sql
//	 * @return
//	 */
//	private String executeUpdateRemote(String sql) {
//		Cacao cacao = null;
//		String resultString = "";
//		
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//			resultString = cacao.executeQuery(sql,this.hos.databaseUrl);
//		}
//		catch(Exception ex) {
//			resultString=ExceptionHelper.toJSONString(ex);
//		}
//		
//		return resultString;
//	}
//	
//	/***
//	 * Chocolate 서버를 거쳐서 update하는 메소드
//	 * 
//	 * PreparedStatement 를 사용하기 위한 메소드
//	 * 
//	 * @param sql
//	 * @return
//	 */
//	private String executeUpdateRemote(String sql, HashMap<Integer, Object>para) {
//		Cacao cacao = null;
//		String resultString = "";
//		
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//			resultString = cacao.executeQuery(sql,para,this.hos.databaseUrl);
//		}
//		catch(Exception ex) {
//			resultString=ExceptionHelper.toJSONString(ex);
//		}
//		
//		return resultString;
//	}
//
//	/***
//	 * Select 문장을 수행한다.
//	 * 
//	 * @param sql
//	 * @return
//	 */
//	private String executeQueryLocal(String sql) {
//		JSONArray result = new JSONArray();
//		JSONArray status = new JSONArray();
//		JSONArray rowData = new JSONArray();
//		JSONObject columns = null;
//		
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			int rowCount=0;
//			con = getDataSource(this.hos.databaseUrl).getConnection();
//			pstmt = con.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//			while (rs.next()) {
//				rowCount++;
//				columns = new JSONObject();
//				for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
//					columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
//				}
//				rowData.add(columns);
//			}
//			rs.close();
//			pstmt.close();
//			con.close();
//			// 리턴값과 메시지
//			columns = new JSONObject();
//			columns.put("return_code",rowCount);
//			columns.put("return_desc","ok");
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			result.add(rowData);
//			
//			return result.toJSONString();
//		} catch(Exception ex) {
//			// 리턴값과 메시지
//			columns = new JSONObject();
//			columns.put("return_code",-1);
//			columns.put("return_desc",ex.getMessage());
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			
//			return result.toJSONString();
//		} finally {
//			if(rs!=null) {
//				try {
//					rs.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//			if(pstmt!=null) {
//				try {
//					pstmt.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//			if(con!=null) {
//				try {
//					con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//		}
//		
//	}
//	
//	/***
//	 * Select 문장을 수행한다.
//	 * 
//	 * PreparedStatement 문을 사용한다.
//	 * 
//	 * @param sql
//	 * @param para
//	 * @return
//	 */
//	private String executeQueryLocal(String sql, HashMap<Integer, Object>para) {
//		JSONArray result = new JSONArray();
//		JSONArray status = new JSONArray();
//		JSONArray rowData = new JSONArray();
//		JSONObject columns = null;
//		
//		Connection con = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			int rowCount=0;
//			con = getDataSource(this.hos.databaseUrl).getConnection();
//			pstmt = con.prepareStatement(sql);
//			int paraCnt=para.size();
//			for(int i=1;i<=paraCnt;i++){
//				pstmt.setObject(i, para.get(i));
//			}
//			rs = pstmt.executeQuery();
//			while (rs.next()) {
//				rowCount++;
//				columns = new JSONObject();
//				for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
//					columns.put(rs.getMetaData().getColumnName(i+1).toLowerCase(), rs.getString(i+1));
//				}
//				rowData.add(columns);
//			}
//			rs.close();
//			pstmt.close();
//			con.close();
//			// 리턴값과 메시지
//			columns = new JSONObject();
//			columns.put("return_code",rowCount);
//			columns.put("return_desc","ok");
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			result.add(rowData);
//			
//			return result.toJSONString();
//		}catch(Exception ex) {
//			// 리턴값과 메시지
//			columns = new JSONObject();
//			columns.put("return_code",-1);
//			columns.put("return_desc",ex.getMessage());
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			
//			return result.toJSONString();
//		} finally {
//			if(rs!=null) {
//				try {
//					rs.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//			if(pstmt!=null) {
//				try {
//					pstmt.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//			if(con!=null) {
//				try {
//					con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//		}
//	}
//
//	/***
//	 * Updagte문을 수행한다.
//	 * 
//	 * @param sql
//	 * @return
//	 */
//	private String executeUpdateLocal(String sql) {
//		JSONArray result = new JSONArray();
//		JSONArray status = new JSONArray();
//		JSONObject columns = null;
//		
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = getDataSource(this.hos.databaseUrl).getConnection();
//			pstmt = con.prepareStatement(sql);
//			int count = pstmt.executeUpdate();
//			pstmt.close();
//			con.close();
//			// 리턴값과 메시지
//			columns = new JSONObject();
//			columns.put("return_code",count);
//			columns.put("return_desc","ok");
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			
//			return result.toJSONString();
//		}
//		catch(Exception ex) {
//			// 리턴값과 메시지
//			columns = new JSONObject();
//			columns.put("return_code",-1);
//			columns.put("return_desc",ex.getMessage());
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			
//			return result.toJSONString();
//		} finally {
//			if(pstmt!=null) {
//				try {
//					pstmt.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//			if(con!=null) {
//				try {
//					con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//		}
//	}
//
//	/***
//	 * Update 문장을 수행한다.
//	 * 
//	 * PreparedStatement 를 사용한다.
//	 * 
//	 * @param sql
//	 * @param para
//	 * @return
//	 */
//	private String executeUpdateLocal(String sql, HashMap<Integer, Object>para) {
//		JSONArray result = new JSONArray();
//		JSONArray status = new JSONArray();
//		JSONObject columns = null;
//		
//		Connection con = null;
//		PreparedStatement pstmt = null;
//
//		try {
//			con = getDataSource(this.hos.databaseUrl).getConnection();
//			pstmt = con.prepareStatement(sql);
//			int paraCnt=para.size();
//			for(int i=1;i<=paraCnt;i++){
//				pstmt.setObject(i, para.get(i));
//			}
//			int count = pstmt.executeUpdate();
//			pstmt.close();
//			con.close();
//			// 리턴값과 메시지
//			columns = new JSONObject();
//			columns.put("return_code",count);
//			columns.put("return_desc","ok");
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			
//			return result.toJSONString();
//		}
//		catch(Exception ex) {
//			// 리턴값과 메시지
//			columns = new JSONObject();
//			columns.put("return_code",-1);
//			columns.put("return_desc",ex.getMessage());
//			status.add(columns);
//			// 반환자료
//			result.add(status);
//			
//			return result.toJSONString();
//		} finally {
//			if(pstmt!=null) {
//				try {
//					pstmt.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//			if(con!=null) {
//				try {
//					con.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					//e.printStackTrace();
//				}
//			}
//		}
//	}
//	
//	/***
//	 * Chocolate 서버를 통하여 서버에서 파일을 읽어온다.
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	private String getFileRemote(String fileName) {
//		Cacao cacao = null;
//		String resultString = "";
//		
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//			String buffer = cacao.getFile(fileName,this.hos.homeUrl);
//			resultString=buffer;
//			//resultString = new String(buffer,"utf-8");
//		}
//		catch(Exception ex) {
//			resultString = null;
//		}
//		
//		return resultString;
//	}
//	
//	/***
//	 * 서버에서 파일을 읽어온다.
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	private String getFileLocal(String fileName) {
//		try {
//			StringBuffer buffer = new StringBuffer();
//			String filePath = this.hos.homeUrl + fileName;
//	        BufferedReader in = new BufferedReader(new FileReader(filePath));
//	        while(true) {
//	        	int c = in.read();
//	        	if (c==-1) break;
//	        	buffer.append((char)c);
//	        }
//	        in.close();
//	        return buffer.toString();
//	    } 
//		catch(Exception e){
////	        System.out.println("FileImpl: "+e.getMessage());
////	        e.printStackTrace();
//	        return(null);
//	    }
//		//return null;
//	}
//	
//	/***
//	 * Basecamp 서버에서 파일을 읽어온다.
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	public String getFileBasecamp(String fileName) {
//		try {
//			StringBuffer buffer = new StringBuffer();
//			String filePath = fileName;
//	        BufferedReader in = new BufferedReader(new FileReader(filePath));
//	        while(true) {
//	        	int c = in.read();
//	        	if (c==-1) break;
//	        	buffer.append((char)c);
//	        }
//	        in.close();
//	        return buffer.toString();
//	    } 
//		catch(Exception e){
//	        return(null);
//	    }
//	}
//	
//	/***
//	 * Chocolate 서버를 이용하여 이미지 파일을 읽어온다.
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	private byte[] getEmrScanFileRemote(String fileName) {
//		Cacao cacao = null;
//		
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//			byte[] buffer = cacao.getFileByteFormat(fileName,this.hos.scanUrl);
//			return buffer;
//		}
//		catch(Exception ex) {
//			return null;
//		}
//	}
//	
//	/***
//	 * 서버에서 이미지 파일을 읽어온다.
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	private byte[] getEmrScanFileLocal(String fileName) {
//		try {
//			String filePath = this.hos.scanUrl + fileName;
//			File file = new File(filePath);
//			byte buffer[] = new byte[(int)file.length()];
//			BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath));
//			input.read(buffer,0,buffer.length);
//			input.close();
//			return buffer;
//		}
//		catch(Exception e) {
//			return null;
//		}
//	}
//	
//	/***
//	 * Basecamp에 있는 이미지 파일을 읽어온다.
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	private byte[] getEmrScanFileBasecamp(String fileName) {
//		try {
//			String filePath = "D:/EmrDroid/DownloadFile/emr_scan/" + this.hos.hospitalId + "/" + fileName + ".png";
//			File file = new File(filePath);
//			byte buffer[] = new byte[(int)file.length()];
//			BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath));
//			input.read(buffer,0,buffer.length);
//			input.close();
//			return buffer;
//		}
//		catch(Exception e) {
//			return null;
//		}
//	}
//	
//	/***
//	 * 파일을 저장한다.
//	 * 
//	 * @param fileName
//	 * @param data
//	 * @return
//	 */
//	private String saveFileLocal(String fileName, byte[] data){
//		try{
//			BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(fileName));
//			out.write(data);
//			out.close();
//			return "";
//		}catch(Exception e){
//			return e.getMessage().toString();
//		}
//	}
//	
//	/***
//	 * Chocolate 서버를 이용하여 파일을 저장한다.
//	 * 
//	 * @param fileName
//	 * @param data
//	 * @return
//	 */
//	private String saveFileRemote(String fileName, byte[] data){
//		Cacao cacao = null;
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//			String retString=cacao.saveFile(fileName,data);
//			return retString;
//		}
//		catch(Exception ex) {
//			return ex.getMessage().toString();
//		}
//	}
//	
//	/***
//	 * 이미지 파일을 저장한다.
//	 * 
//	 * @param fileName
//	 * @param data
//	 * @return
//	 */
//	private String saveImageFileLocal(String fileName, byte[] data){
//		try{
//			BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(fileName));
//			out.write(data);
//			out.close();
//			return "";
//		}catch(Exception e){
//			return e.getMessage().toString();
//		}
//	}
//
//	/***
//	 * Basecamp서버에 이미지 파일을 저장한다.
//	 * 
//	 * @param fileName
//	 * @param data
//	 * @return
//	 */
//	private String saveScanFileBasecamp(String fileName, byte[] data){
//		try{
//			String filePath = "D:/EmrDroid/DownloadFile/emr_scan/" + this.hos.hospitalId + "/" + fileName + ".png";
//			File f = new File(filePath);
//			File dir = f.getParentFile();
//			if(!dir.exists()){
//				boolean status = dir.mkdirs();
//			}
//			BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(filePath));
//			out.write(data);
//			out.close();
//			return "";
//		}catch(Exception e){
//			return e.getMessage().toString();
//		}
//	}
//	
//	/***
//	 * Chocolate 서버를 이용하여 이미지 파일을 저장한다.
//	 * 
//	 * @param fileName
//	 * @param data
//	 * @return
//	 */
//	private String saveImageFileRemote(String fileName, byte[] data){
//		Cacao cacao = null;
//		try {
//			cacao = (Cacao)Naming.lookup(this.hos.chocolateUrl);
//			String retString=cacao.saveImageFile(fileName,data);
//			return retString;
//		}
//		catch(Exception ex) {
//			return ex.getMessage().toString();
//		}
//	}
//	
//	/***
//	 * Basecamp에 대한 connection을 DataSource를 사용해서 가져온다.
//	 * 
//	 * @return
//	 */
//	public static DataSource getBasecampDataSource() {
//		String connectionUrl = Utility.getBasecampDbConnectionUrl();
//		return MyBasecampDataSource.getDataSource(connectionUrl);
//	}
//	
//	static class MyBasecampDataSource {
//		private static BasicDataSource dataSource = null;
//		private static String conUrl = null;
//		private static DataSource getDataSource(String connectionUrl) {
//			if (dataSource==null){
//				dataSource = new BasicDataSource();
//				dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//				dataSource.setUrl(connectionUrl);
//				conUrl = connectionUrl;
//			}
//			// 혹시 connectionUrl이 변경되었으면...
//			if(conUrl.equals(connectionUrl)==false){
//				dataSource.setUrl(connectionUrl);
//				conUrl = connectionUrl;
//			}
//			return dataSource;
//		}
//	}	
//	
//	/***
//	 * DataSource를 가져온다.
//	 * 
//	 * @param connectionUrl
//	 * @return
//	 */
//	public static DataSource getDataSource(String connectionUrl) {
//		return MyDataSource.getDataSource(connectionUrl);
//	}
//	
//	static class MyDataSource {
//		private static BasicDataSource dataSource = null;
//		private static String conUrl = null;
//		private static DataSource getDataSource(String connectionUrl) {
//			if (dataSource==null){
//				System.out.println(Utility.getCurrentDateTime() + " DataSource생성시작");
//				dataSource = new BasicDataSource();
//				dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//				dataSource.setUrl(connectionUrl);
//				conUrl = connectionUrl;
//				System.out.println(Utility.getCurrentDateTime() + " DataSource생성완료");
//			}
//			// 혹시 connectionUrl이 변경되었으면...
//			if(conUrl.equals(connectionUrl)==false){
//				dataSource.setUrl(connectionUrl);
//				conUrl = connectionUrl;
//			}
//			return dataSource;
//		}
//	}
	
}
