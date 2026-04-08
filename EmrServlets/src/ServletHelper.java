import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class ServletHelper {
	private String ipAddress = "http://180.70.20.24:8080"; // 본사 개발 서버(공인ip)

	public void setServletIp(String servletIp) {
		/*
		if(servletIp.equals("")){
			ipAddress = "http://180.70.20.24:8080";
		}else{
			ipAddress = servletIp;
		}
		*/
	}
	
	public String getFullUrl(String servlet){
		String url = ipAddress + "/emrdroid/servlet/" + servlet;
		return url;
	}
	
	public String getXml(String servlet) throws Exception {
		return getXml(servlet,null);
	}

	public String getXml(String servlet,HashMap<String, String> param) throws KeyManagementException, MalformedURLException, NoSuchAlgorithmException, IOException {
		if(param!=null){
			String paramString=getParamString(param);
			if(paramString.equals("")==false){
				servlet += "?" + paramString;
			}
		}
		String url = ipAddress + "/emrdroid/servlet/" + servlet;
		String xml = downloadXml(url);
		return xml;
	}
	
	public String uploadPngFile(String fileName, String uploadFileName){
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary="*****";
		int maxBufferSize = 4*1024*1024;
		String resultString = "";
		try{
			String urlString = ipAddress + "/emrdroid/servlet/FileUploadServlet";
			FileInputStream fis = new FileInputStream(fileName);
			// open a URL connection to th Servlet
			URL url = new URL(urlString);
			
			// open a HTTP connection to the URL
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			// Allow Inputs
			conn.setDoInput(true);
			// Allow Outputs
			conn.setDoOutput(true);
			// Don't use a cached copy.
			conn.setUseCaches(false);
			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
			
			// write data
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			
			//Log.d("EmrDroid","Content-Disposition:form-data;name=\"uploadedfile\";filename=" + uploadFileName + "" + lineEnd);
			// type=image/pnm 가 꼭 있어야함.
			dos.writeBytes("Content-Disposition:form-data;name=\"uploadedfile\";filename=" + uploadFileName + ";type=image/png" + lineEnd);
			dos.writeBytes(lineEnd);
			// create a buffer of maximum size
			int bytesAvailable = fis.available();
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];
			// read file and write it to form ...
			int bytesRead = fis.read(buffer, 0, bufferSize);
			while(bytesRead>0){
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fis.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fis.read(buffer, 0, bufferSize);
			}
			// send multipart form data necessary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			// close stream
			fis.close();
			dos.flush();
			dos.close();
			
			// 웹서버에서 결과를 받는다.
			StringBuffer sb = new StringBuffer();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				for (;;) {
					int read = br.read();
					if (read == -1)
						break;
					sb.append((char) read);
				}
				br.close();
			} else {
				sb.append("HttpURLConnection is not OK.");
			}
			
			conn.disconnect();
			
			resultString = sb.toString();
		}catch (FileNotFoundException ex){
			new LogWrite().errorWrite(getClass().getSimpleName(), "uploadPngFile", "FileNotFoundException", ex.getLocalizedMessage());
			resultString = "FileNotFound : " + ex.getMessage().toString();
		}catch (MalformedURLException ex){
			new LogWrite().errorWrite(getClass().getSimpleName(), "uploadPngFile", "MalformedURLException", ex.getLocalizedMessage());
			resultString = "MalformedURL : " + ex.getMessage().toString();
		}catch (IOException ex){
			new LogWrite().errorWrite(getClass().getSimpleName(), "uploadPngFile", "IOException", ex.getLocalizedMessage());
			resultString = "IO : " + ex.getMessage().toString();
		}catch (Exception ex){
			new LogWrite().errorWrite(getClass().getSimpleName(), "uploadPngFile", "Exception", ex.getLocalizedMessage());
			resultString = "Ex : " + ex.getMessage().toString();
		}
		
		return resultString;
	}
	
	public String getXmlPost(String servlet,HashMap<String, String> param) throws Exception {
		StringBuilder xml = new StringBuilder();
		String addr = ipAddress + "/emrdroid/servlet/" + servlet;
		String paramString=getParamString(param);
		URL url = new URL(addr);
		HttpURLConnection conn = null;
		if (url.getProtocol().toLowerCase().equals("https")) {
			trustAllHosts();
			HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			https.setHostnameVerifier(DO_NOT_VERIFY);
			conn = https;
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		DataOutputStream out;
		if (conn != null) {
			conn.setConnectTimeout(10000);
			conn.setDoInput(true); //기본 true
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestMethod("POST");
			conn.setDoOutput(true); // true이면 RequestMethos가 자동으로 POST임.
			conn.setUseCaches(false);
			conn.setRequestProperty("Connection", "close");
			out=null;
			out=new DataOutputStream(conn.getOutputStream());
			//out.writeBytes(paramString);
			out.write(paramString.getBytes("utf-8"));
			out.flush();
			out.close();
			//
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				for (;;) {
					int read = br.read();
					if (read == -1)
						break;
					xml.append((char) read);
				}
				br.close();
			}
			conn.disconnect();
		}
		return xml.toString();
	}

	private String getParamString(HashMap<String, String> param){
		String retString="";
		int count = param.size();
		int i = 0;
		if (count > 0) {
			Iterator<String> iterator = param.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (i == 0) {
					retString = key + "=" + param.get(key);
				} else {
					retString += "&" + key + "=" + param.get(key);
				}
				i++;
			}
		}		
		return retString;
	}
	
	@SuppressWarnings("finally")
	// NetworkOnMainThreadException이 발생하면,
	// 결론부터 말하자면 honeycomb에서는 main thread(UI)에서 네트워크 호출을 하면 무조건 error로 간주한다
	private String downloadXml(String addr) throws MalformedURLException, KeyManagementException, NoSuchAlgorithmException, IOException {
		StringBuilder xml = new StringBuilder();
		URL url = new URL(addr);
		HttpURLConnection conn = null;
		if (url.getProtocol().toLowerCase().equals("https")) {
			trustAllHosts();
			HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
			https.setHostnameVerifier(DO_NOT_VERIFY);
			conn = https;
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		if (conn != null) {
			conn.setConnectTimeout(10000);
			conn.setUseCaches(false);
			conn.setRequestProperty("Connection", "close");
			int responseCode=conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				for (;;) {
					int read = br.read();
					if (read == -1)
						break;
					xml.append((char) read);
				}
				br.close();
			}
			conn.disconnect();
		}
		return xml.toString();
	}

	private static void trustAllHosts() throws NoSuchAlgorithmException, KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
}
