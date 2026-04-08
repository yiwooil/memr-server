import java.io.File;
import java.util.HashMap;

public class MFGetFileExistsYN implements MFGet {
	private static MFGetFileExistsYN mInstance=null;
	private MFGetFileExistsYN(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetFileExistsYN getInstance(){
		if(mInstance==null){
			mInstance = new MFGetFileExistsYN();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String fileName = (String)param.get("file_name");
		String fileType = (String)param.get("file_type");
		String preSave = (String)param.get("pre_save");
		
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
			String filePath = "";
			if(folder.endsWith("/")){
				filePath = folder + fileName;
			}else{
				filePath = folder + "/" + fileName;
			}
			new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "filePath="+filePath);
			
			File file = new File(filePath);
			String ret = "N";
			if(file.exists()) ret = "Y";
			
			new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "ret="+ret);
			
			return ret;
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			return "N";
		}
	}

}
