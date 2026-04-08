
import org.json.*;


public class ResultSetHelper {
	final static int MASKING_DEFAULT=0;
	final static int MASKING_NONE=1;
	final static int MASKING_FORCE=2;
	
	private JSONArray main;
	private JSONArray control;
	private JSONArray data;
	private int returnCode;
	private String returnDesc;
	private boolean masking;
	
	public ResultSetHelper(String jsonString, boolean masking) throws JSONException {
		this.masking=masking;
		this.main = new JSONArray(jsonString);
		this.control = main.getJSONArray(0);
		returnCode = control.getJSONObject(0).getInt("return_code");
		returnDesc = control.getJSONObject(0).getString("return_desc");
		if (returnCode>0) {
			this.data = main.getJSONArray(1);
		}
		else {
			this.data=null;
		}
	}
	public ResultSetHelper(String jsonString) throws JSONException {
		this(jsonString, true);
	}
	
	public int getReturnCode() {
		return returnCode;
	}
	
	public String getReturnDesc() {
		return returnDesc;		
	}
	
	public int getRecordCount() {
		return data==null ? 0 : data.length();
	}
	
	public boolean getBoolean(int index, String key) throws JSONException {
		return data.getJSONObject(index).getBoolean(key);
	}
	
	public double getDouble(int index, String key) throws JSONException {
		return data.getJSONObject(index).getDouble(key);
	}

	public int getInt(int index, String key) throws JSONException {
		return data.getJSONObject(index).getInt(key);
	}
	
	public long getLong(int index, String key) throws JSONException {
		return data.getJSONObject(index).getLong(key);
	}

	public String getString(int index, String key) throws JSONException {
		return getString(index,key,ResultSetHelper.MASKING_DEFAULT);
	}
	
	public String getString(int index, String key, int maskAction) throws JSONException {
		boolean bMasking=false;
		String returnString="";
		if (maskAction==ResultSetHelper.MASKING_FORCE) {
			bMasking=true;
		}
		else if (maskAction==ResultSetHelper.MASKING_NONE) {
			bMasking=false;
		}
		else {
			bMasking=this.masking;
		}
		returnString=data.getJSONObject(index).getString(key);
		// 2013.08.08 WOOIL - null 이면 빈문자열로 변환
		//                    oracle은 빈문자열을 null로 처리한다.
		if(returnString.equals("null")) returnString="";
		if (bMasking==true) {
			if (key.equals("pnm")) {
				returnString+="  "; // String index out of range: 2 오류 방지용
				returnString=returnString.substring(0, 2) + "*";
			}
		}
		return returnString;
	}
	
	public void putValue(int index, String key, String value) throws JSONException {
		ensureData();

	    // index까지 JSONObject가 존재하도록 채움
	    while (index >= this.data.length()) {
	        this.data.put(new JSONObject());
	    }

	    JSONObject row = this.data.getJSONObject(index);

	    // value가 null이면 빈문자열로 처리(원하시면 JSONObject.NULL로 바꿔도 됨)
	    row.put(key, value == null ? "" : value);		
	}
	
	public String toJSONString() {
		return this.main.toString();
	}
	
	// data JSONArray가 없으면 생성해서 main[1]에 연결
	private void ensureData() throws JSONException {
	    if (this.data != null) return;

	    this.data = new JSONArray();

	    // main[1] 자리에 data를 넣어준다.
	    // main이 [control]만 있는 경우도 있을 수 있으므로 길이 체크
	    if (this.main.length() >= 2) {
	        this.main.put(1, this.data);
	    } else {
	        this.main.put(this.data); // index 1로 append됨
	    }
	}
	
	public static int getReturnCount(String jsonString) throws JSONException{
		JSONArray main = new JSONArray(jsonString);
		JSONArray control = main.getJSONArray(0);
		int count=control.getJSONObject(0).getInt("return_code");
		return count;
	}
	
	public static String getReturnDesc(String jsonString) throws JSONException{
		JSONArray main = new JSONArray(jsonString);
		JSONArray control = main.getJSONArray(0);
		String desc=control.getJSONObject(0).getString("return_desc");
		return desc;
	}
	
}
