
import java.io.*;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload; 
import org.apache.commons.fileupload.disk.DiskFileItemFactory;


public class FileUploadServlet extends HttpServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		new LogWrite().debugWrite(getClass().getSimpleName(), "doPost", "시작");
		request.setCharacterEncoding("UTF-8"); // 파일한글명 처리
		
		String hospitalId = request.getParameter("hospital_id");
		String fileType = request.getParameter("file_type");
		String preSave = request.getParameter("pre_save");
		String resultString="";
		
		try{
			SqlHelper sqlHelper = new SqlHelper(hospitalId);
			String scanUrl = sqlHelper.getScanUrl(); // 동의서이미지 폴더
			String preSaveUrl = sqlHelper.getPreSaveUrl(); // 임시저장 동의서 이미지 폴더
			String mp4Url = sqlHelper.getMP4Url(); // 녹음파일 폴더
			String picUrl = sqlHelper.getPicUrl(); // 사진파일 폴더
			String homeUrl = sqlHelper.getHomeUrl(); // 홈... 
			String folder = scanUrl;
			if("ccf".equalsIgnoreCase(fileType)){
				folder = homeUrl + "/Form";
			}else if("pic".equalsIgnoreCase(fileType)){
				folder = picUrl;
			}else if("mp4".equalsIgnoreCase(fileType)){
				folder = mp4Url;
			}else{
				if("Y".equalsIgnoreCase(preSave)){
					folder = preSaveUrl;
				}else{
					folder = scanUrl;
				}
			}
			new LogWrite().debugWrite(getClass().getSimpleName(), "doPost", "folder="+folder);
	
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if(isMultipart){
				DiskFileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				
				//FileItemIterator iter = upload.getItemIterator(request);
				List<FileItem> items = upload.parseRequest(request);
				Iterator iter = items.iterator();
				while(iter.hasNext()){
					//FileItemStream item = iter.next();
					FileItem item = (FileItem)iter.next();
					if(!item.isFormField()){
						String fileName = item.getName();
						new LogWrite().debugWrite(getClass().getSimpleName(), "doPost", "fileName="+fileName);
						//String filePath = "D:/EmrDroid/UploadFile/Image/" + fileName;
						String filePath = "";
						if(folder.endsWith("/")){
							filePath = folder + fileName;
						}else{
							filePath = folder + "/" + fileName;
						}
						new LogWrite().debugWrite(getClass().getSimpleName(), "doPost", "filePath="+filePath);
						File f = new File(filePath);
						File dir = f.getParentFile();
						if(!dir.exists()){
							boolean status = dir.mkdirs();
						}
						item.write(f); 
						new LogWrite().debugWrite(getClass().getSimpleName(), "doPost", "파일쓰기종료");
					}
				}
				new LogWrite().debugWrite(getClass().getSimpleName(), "doPost", "종료");
				resultString = "success";
			}else{
				resultString = "isMultipart is false";
			}
		}catch(Exception ex){
			new LogWrite().errorWrite(getClass().getSimpleName(), "doPost", "Exception", ex.getLocalizedMessage());
			resultString = "error : " + ex.getMessage().toString();
		}
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println(resultString);
		out.close();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
