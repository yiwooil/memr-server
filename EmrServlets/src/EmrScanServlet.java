import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
//import java.awt.image.RenderedImage; // 2026.04.02 WOOIL - 표준화.제거
import java.io.ByteArrayInputStream; // 2026.04.02 WOOIL - 표준화.추가
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import javax.media.jai.*; // 2026.04.02 WOOIL - 표준화.제거
//import com.sun.media.jai.codec.*; // 2026.04.02 WOOIL - 표준화.제거

public class EmrScanServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String hospitalId = request.getParameter("hospitalid");
		String path = request.getParameter("path");
		String mode = request.getParameter("mode");
		String ccfId = request.getParameter("ccfid");
		String drid = request.getParameter("drid");
		
		if(mode==null) mode="";
		if(ccfId==null) ccfId="";
		if(drid==null) drid="";
		
		/* 2013.02.12 WOOIL - 
		 *      파일을 매번 서버에서 읽어오지 않고 베이스캠프서버에 보관했다가 다시 읽으면 빨라질것 같아서
		 *      그렇게 했으나 눈에 띠는 속도개선의 효과가 없었음.
		 *      
		 *      이미지의 크기가 더 큰 영향을 주는 것 같다.
		 *      
		 *      getEmrScanFile_test가 베이스캠프서버에 보관하는 버전임.
		 *      추후를 위하여 남겨놓음.
		 *      
		String test = request.getParameter("test"); // 테스트 기간에만 사용

		byte[] returnByte = null;
		if(test==null) test="";
		if("".equals(test)){
			returnByte = getEmrScanFile(hospitalId, path);
		}else{
			returnByte = getEmrScanFile_test(hospitalId, path);
		}
		*/
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "doGet", "mode=" + mode);
		
		
		byte[] returnByte = null;//getEmrScanFile(hospitalId, path);
		if("9".equalsIgnoreCase(mode)){
			// 의사 사인 이미지를 불러온다.
			returnByte = getSignImageFile(hospitalId, drid);
		}else if("8".equalsIgnoreCase(mode)){
			// 동의서 이미지를 불러온다.
			returnByte = getCcfImageFile(hospitalId, ccfId);
		}else if("7".equalsIgnoreCase(mode)){
			// 병원에서 생상된 이미지
			returnByte = getHosEmrScanFile(hospitalId, path);
		}else if("6".equalsIgnoreCase(mode)){
			// 동의서에 사용될 이미지
			//returnByte = getHosLogoImageFile(hospitalId);
			returnByte = null;
		}else if("5".equalsIgnoreCase(mode)){
			// 사진 파일을 불러온다.
			returnByte = getPicFile(hospitalId, path);
		}else if("4".equalsIgnoreCase(mode)){
			// 녹음파일을 불러온다.
			returnByte = getPreSavedCCFMP4File(hospitalId, path);
			response.setContentType("video/mp4");
			ServletOutputStream out = response.getOutputStream();
			out.write(returnByte, 0, returnByte.length);
			out.close();
			return;
		}else if("3".equalsIgnoreCase(mode)){
			// 동의서 임시저장 이미지를 불러온다.
			returnByte = getPreSavedCCFFile(hospitalId, path);
		}else{
			returnByte = getEmrScanFile(hospitalId, path);
		}

		// 2013.09.10 WOOIL - 파일이 있는지 검사한다.
		if("2".equals(mode)){
			// 자인컴용도임.
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = response.getWriter();		
			if(returnByte==null){
				out.print("no");
			}else{
				out.print("yes");
			}
			out.close();
			return;
		}
		
		ServletOutputStream out = response.getOutputStream();

		if (returnByte == null) {

		    response.setContentType("image/png");

		    int w = 300;
		    int h = 100;
		    int type = BufferedImage.TYPE_3BYTE_BGR;
		    BufferedImage image = new BufferedImage(w, h, type);
		    Graphics g = image.getGraphics();
		    g.drawString("자료가 없습니다.", 10, 50);
		    ImageIO.write(image, "png", out);

		} else {

		    String contentType = guessContentType(returnByte);

		    try {
		        ByteArrayInputStream bais = new ByteArrayInputStream(returnByte);
		        BufferedImage image = ImageIO.read(bais);

		        if (image != null) {
		            // 읽을 수 있는 정상 이미지면 PNG로 변환해서 응답
		            response.setContentType("image/png");
		            ImageIO.write(image, "png", out);
		        } else {
		            // ImageIO가 읽지 못하면 원본 그대로 응답
		            response.setContentType(contentType);
		            out.write(returnByte, 0, returnByte.length);
		        }

		    } catch (Exception ex) {
		        // CMYK JPEG 등 ImageIO 처리 실패 시 원본 그대로 응답
		        new LogWrite().errorWrite(getClass().getSimpleName(), "doGet", "ImageIO ExceptionClass", ex.getClass().getName());
		        new LogWrite().errorWrite(getClass().getSimpleName(), "doGet", "ImageIO ExceptionMessage", ex.getMessage());

		        response.setContentType(contentType);
		        out.write(returnByte, 0, returnByte.length);
		    }
		}

		out.close();
		
		/*
		response.setContentType("image/png");
		ServletOutputStream out = response.getOutputStream();

		if (returnByte == null) {

			int w = 300;
			int h = 100;
			int type = BufferedImage.TYPE_3BYTE_BGR;
			BufferedImage image = new BufferedImage(w, h, type);
			Graphics g = image.getGraphics();
			g.drawString("자료가 없습니다.", 10, 50);
			ImageIO.write(image, "png", out);
				
		} else {

			ByteArrayInputStream bais = new ByteArrayInputStream(returnByte);
		    BufferedImage image = ImageIO.read(bais);

		    if (image == null) {
		        int w = 300;
		        int h = 100;
		        int type = BufferedImage.TYPE_3BYTE_BGR;
		        image = new BufferedImage(w, h, type);
		        Graphics g = image.getGraphics();
		        g.drawString("이미지 형식을 읽을 수 없습니다.", 10, 50);
		    }

		    ImageIO.write(image, "png", out);
		}

		out.close();
		*/
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private byte[] getHosEmrScanFile(String hospitalId, String path) {
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			byte[] returnByte = sqlHelper.getHosEmrScanFile(path);
			return returnByte;
		} catch (Exception ex) {
			// byte[] returnByte = ex.getMessage().getBytes();
			// return returnByte;
			new LogWrite().errorWrite(getClass().getSimpleName(), "getHosEmrScanFile", "Exception", ex.getLocalizedMessage());
			return null;
		}
	}
	private byte[] getEmrScanFile(String hospitalId, String path) {
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			byte[] returnByte = sqlHelper.getEmrScanFile(path);
			return returnByte;
		} catch (Exception ex) {
			// byte[] returnByte = ex.getMessage().getBytes();
			// return returnByte;
			new LogWrite().errorWrite(getClass().getSimpleName(), "getEmrScanFile", "Exception", ex.getLocalizedMessage());
			return null;
		}
	}
	
	private byte[] getPicFile(String hospitalId, String path) {
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			byte[] returnByte = sqlHelper.getPicFile(path);
			return returnByte;
		} catch (Exception ex) {
			// byte[] returnByte = ex.getMessage().getBytes();
			// return returnByte;
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPicFile", "Exception", ex.getLocalizedMessage());
			return null;
		}
	}
	
//	private byte[] getHosLogoImageFile(String hospitalId) {
//		SqlHelper sqlHelper;
//		try {
//			sqlHelper = new SqlHelper(hospitalId);
//			byte[] returnByte = sqlHelper.getHosLogoImageFile();
//			return returnByte;
//		} catch (Exception ex) {
//			// byte[] returnByte = ex.getMessage().getBytes();
//			// return returnByte;
//			new LogWrite().errorWrite(getClass().getSimpleName(), "getHosImageFile", "Exception", ex.getLocalizedMessage());
//			return null;
//		}
//	}
	
	private byte[] getPreSavedCCFFile(String hospitalId, String path) {
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			byte[] returnByte = sqlHelper.getPreSavedCCFFile(path);
			return returnByte;
		} catch (Exception ex) {
			// byte[] returnByte = ex.getMessage().getBytes();
			// return returnByte;
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPreSavedCCFFile", "Exception", ex.getLocalizedMessage());
			return null;
		}
	}
	
	private byte[] getPreSavedCCFMP4File(String hospitalId, String path) {
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			byte[] returnByte = sqlHelper.getPreSavedCCFMP4File(path);
			return returnByte;
		} catch (Exception ex) {
			// byte[] returnByte = ex.getMessage().getBytes();
			// return returnByte;
			new LogWrite().errorWrite(getClass().getSimpleName(), "getPreSavedCCFMP4File", "Exception", ex.getLocalizedMessage());
			return null;
		}
	}
	
	private byte[] getCcfImageFile(String hospitalId, String ccfId) {
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			String path = getCcfFileName(hospitalId, ccfId);
			new LogWrite().debugWrite(getClass().getSimpleName(), "getCcfImageFile", "path=" + path);
			String filePath = Utility.concatFilePath(sqlHelper.getHomeUrl(), "Form" ,path);
			new LogWrite().debugWrite(getClass().getSimpleName(), "getCcfImageFile", "filePath=" + filePath);
			byte[] returnByte = sqlHelper.getFileForByte(filePath);
			return returnByte;
		} catch (Exception ex) {
			// byte[] returnByte = ex.getMessage().getBytes();
			// return returnByte;
			new LogWrite().errorWrite(getClass().getSimpleName(), "getCcfImageFile", "Exception", ex.getLocalizedMessage());
			return null;
		}
		
	}
	
	private String getCcfFileName(String hospitalId, String ccfId) throws Exception{
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		//String returnString;
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		sqlHelper = new SqlHelper(hospitalId);
		boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
		String sql = "";
		if(interfaceTableYn){
			sql = "select ccf_name,ccf_filename from consent_form_mast where ccf_id=?";
		}else{
			sql = "select cdnm as ccf_name, fld1qty as ccf_filename from ta88 where mst1cd='EMR' and mst2cd='FORM' and mst3cd=?";
		}
		para.put(1, ccfId);
		String rsString = sqlHelper.executeQuery(sql, para, null);
		rs = new ResultSetHelper(rsString);
		String ccfFilename = rs.getString(0, "ccf_filename");
		return ccfFilename;
	}
	
	private byte[] getSignImageFile(String hospitalId, String drid) {
		try {
			Hospital hos = HospitalInformation.getHospital(hospitalId);
			if ("Y".equalsIgnoreCase(hos.useDrSignTable)){
				return getSignImageFile_table(hospitalId, drid);
			}else{
				return getSignImageFile_file(hospitalId, drid);
			}
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSignImageFile", "Exception", ex.getLocalizedMessage());
			return null;
		}
	}
	
	private byte[] getSignImageFile_table(String hospitalId, String drid) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			Hospital hos = HospitalInformation.getHospital(hospitalId);
			byte[] imageBytes = null;
			String sql = "SELECT SignPIC FROM TA07_IMG WHERE DRID=?";
			con = SqlHelper.getDataSource(hos.databaseUrl).getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, drid);
			rs = pstmt.executeQuery();
			if (rs.next()){
				Blob blob = rs.getBlob("SignPIC");
				imageBytes = blob.getBytes(1, (int) blob.length());
			}
			return imageBytes;
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSignImageFile_table", "Exception", ex.getLocalizedMessage());
			return null;
		} finally {
			if (rs!=null) try{rs.close();} catch (Exception ex){}
			if (pstmt!=null) try{pstmt.close();} catch (Exception ex) {}
			if (con!=null) try{con.close();} catch (Exception ex) {}
		}
		
	}
	
	private byte[] getSignImageFile_file(String hospitalId, String drid) {
		SqlHelper sqlHelper;

		try {
			sqlHelper = new SqlHelper(hospitalId);
			String path = drid + ".jpg";
			String filePath = Utility.concatFilePath(sqlHelper.getHomeUrl(), "Sign" ,path);
			new LogWrite().debugWrite(getClass().getSimpleName(), "getSignImageFile_file", "filePath=" + filePath);
			byte[] returnByte = sqlHelper.getFileForByte(filePath);
			return returnByte;
		} catch (Exception ex) {
			// byte[] returnByte = ex.getMessage().getBytes();
			// return returnByte;
			new LogWrite().errorWrite(getClass().getSimpleName(), "getSignImageFile_file", "Exception", ex.getLocalizedMessage());
			return null;
		}
		
	}
	
	private String guessContentType(byte[] data) {
	    if (data == null || data.length < 8) {
	        return "application/octet-stream";
	    }

	    // PNG signature: 89 50 4E 47 0D 0A 1A 0A
	    if ((data[0] & 0xFF) == 0x89 &&
	        (data[1] & 0xFF) == 0x50 &&
	        (data[2] & 0xFF) == 0x4E &&
	        (data[3] & 0xFF) == 0x47 &&
	        (data[4] & 0xFF) == 0x0D &&
	        (data[5] & 0xFF) == 0x0A &&
	        (data[6] & 0xFF) == 0x1A &&
	        (data[7] & 0xFF) == 0x0A) {
	        return "image/png";
	    }

	    // JPEG signature: FF D8
	    if ((data[0] & 0xFF) == 0xFF &&
	        (data[1] & 0xFF) == 0xD8) {
	        return "image/jpeg";
	    }

	    return "application/octet-stream";
	}	
//	private byte[] getEmrScanFile_test(String hospitalId, String path) {
//		SqlHelper sqlHelper;
//		try {
//			sqlHelper = new SqlHelper(hospitalId);
//			byte[] returnByte = sqlHelper.getEmrScanFile_test(path);
//			return returnByte;
//		} catch (Exception ex) {
//			// byte[] returnByte = ex.getMessage().getBytes();
//			// return returnByte;
//			return null;
//		}
//	}
	
}
