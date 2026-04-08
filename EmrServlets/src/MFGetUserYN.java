import java.util.HashMap;


public class MFGetUserYN implements MFGet {
	private static MFGetUserYN mInstance=null;
	private MFGetUserYN(){
		
	}
	
	public static MFGetUserYN getInstance(){
		if(mInstance==null){
			mInstance = new MFGetUserYN();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String password = (String)param.get("password");
		String ver = (String)param.get("ver");
		
		if(ver==null) ver="";
		if(userId.equalsIgnoreCase("mmsdev")) userId = "mms"; // 2022.06.14 WOOIL - mmsdev이면 mms로 로그인되도록.
		
		//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "userId="+userId+", password="+password+", ver="+ver);
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		String returnString="no";
		SqlHelper sqlHelper;
		ResultSetHelper rsHelper;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			boolean pwdSkpYn = sqlHelper.getPwdSkpYn();
			boolean existsMEMR = getExistsMEMR(interfaceTableYn, hospitalId);
			String sql = getSql(interfaceTableYn, existsMEMR);
			para.put(++idx, userId);
			String rsString = sqlHelper.executeQuery(sql,para,null);
			rsHelper = new ResultSetHelper(rsString);
			int cnt=rsHelper.getReturnCode();
			if (cnt<0) {
				returnString=rsHelper.getReturnDesc();
			}else if(cnt==0) {
				returnString="인가되지 않은 사용자입니다①.";
			}else if (cnt>0) {
				returnString="인가되지 않은 사용자입니다②.";
				for(int i=0;i<cnt;i++){
					String passwd=rsHelper.getString(i, "passwd");
					passwd=passwd.trim(); // 비밀번호뒤에 스페이스문자가 있는 경우가 있음.
					if("****".equals(passwd)){
						// 비밀번호가 암호화되어 있다.
						String passwdenc=rsHelper.getString(i, "passwdenc"); // 암호화되어 저장된 값.
						String encryption = getEncryption(hospitalId, password);
						if(encryption.equalsIgnoreCase(passwdenc)){ // 비교해서 맞으면
							returnString="yes";
							if("2".equals(ver)) returnString+=rsHelper.getString(i,"usrnm"); // 사용자 이름을 같이 넘긴다.
							break;
						}
					}else if(pwdSkpYn==true || password.equalsIgnoreCase(passwd)){
						// 비밀번호를 점검하지 않는 옵션 추가
						returnString="yes";
						if("2".equals(ver)) returnString+=rsHelper.getString(i,"usrnm"); // 사용자 이름을 같이 넘긴다.
						break;
					}
				}
			}
		}
		catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString=ex.getMessage();
		}
		return returnString;
	}
	
	private String getSql(boolean interfaceTableYn, boolean existsMEMR) {
		String sql="";
		//if(interfaceTableYn){
		//	sql = "select password passwd,user_name usrnm from login_user_mast where user_id=?";
		//}else{
			if(existsMEMR){
				// 2019.07.25 WOOIL - 사용자 로그인 관리를 할 수 있도록
				sql = "select usrnm,passwd,passwdenc from ta94 where usrid=? and prjid='MEMR'";
			}else{
				sql = "select usrnm,passwd,passwdenc from ta94 where usrid=?";
			}
		//}
		return sql;
	}
	
	private boolean getExistsMEMR(boolean interfaceTableYn, String hospitalId) throws Exception {
		boolean bRet = false;
		if(interfaceTableYn){
			bRet = false;
		}else{
			bRet = false;
			SqlHelper sqlHelper = new SqlHelper(hospitalId);
			String sql = "select * from ta91 where prjcd='MEMR'";
			String rsString = sqlHelper.executeQuery(sql,null,null);
			ResultSetHelper rsHelper = new ResultSetHelper(rsString);
			int cnt=rsHelper.getReturnCode();
			bRet = cnt>0;
		}
		return bRet;
	}
	
	private String getEncryption(String hospitalId, String password) throws Exception {
		SqlHelper sqlHelper = new SqlHelper(hospitalId);
		String sql = "SELECT dbo.MFN_GET_ENCSTR('" + password + "') 'ENCRYPTION'"; // 입력한 비밀번호를 암호화한다.
		String rsString = sqlHelper.executeQuery(sql); // 암호화 sql 실행
		ResultSetHelper rsHelper = new ResultSetHelper(rsString); // 결과를 구함.
		String encryption = rsHelper.getString(0, "encryption"); // 암호화된 값.
		return encryption;
	}

}
