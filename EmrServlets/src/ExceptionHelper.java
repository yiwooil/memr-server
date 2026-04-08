import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ExceptionHelper {
//	private Exception ex;
//	public ExceptionHelper(Exception ex) {
//		this.ex=ex;
//	}
	public static String toJSONString(Exception ex) {
		// 리턴값과 메시지
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONObject columns = null;
		
		columns = new JSONObject();
		columns.put("return_code",-1);
		columns.put("return_desc",ex.getMessage());
		status.add(columns);
		// 반환자료
		result.add(status);
		return result.toJSONString();
	}
	
	public static String toJSONString(int code, String message) {
		// 리턴값과 메시지
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONObject columns = null;
		
		columns = new JSONObject();
		columns.put("return_code",code);
		columns.put("return_desc",message);
		status.add(columns);
		// 반환자료
		result.add(status);
		return result.toJSONString();
	}
}
