import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONArray;

public class MFGetStructedText implements MFGet {
	private static MFGetStructedText mInstance=null;
	private MFGetStructedText(){
		
	}
	
	public static MFGetStructedText getInstance(){
		if(mInstance==null){
			mInstance = new MFGetStructedText();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String bdiv = (String)param.get("bdiv");
		String wdate = (String)param.get("wdate");
		String wtime = (String)param.get("wtime");
		String originalText = (String)param.get("currenttext");
		
		String returnString = "";
	    String apiKey = "";
	    SqlHelper sqlHelper;

	    new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "pid=" + pid + ", bededt=" + bededt + ", bdiv=" + bdiv + ", wdate=" + wdate + ", wtime=" + wtime);
	    
	    try{
			sqlHelper = new SqlHelper(hospitalId);
			String sql = "select fld2qty, fld3qty from ta972 where prjcd ='odi'and frmnm ='c#_VER_CONFIG' and seq ='44'";
			String rsString = sqlHelper.executeQuery(sql);
			ResultSetHelper rs = new ResultSetHelper(rsString);
			if (rs.getRecordCount() > 0) {
				apiKey = rs.getString(0, "fld2qty") + rs.getString(0, "fld3qty"); 
			}
			
			//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "apiKey=" + apiKey);
			
			/* 이전내역이 있으나 없으나 결과는 동일하여 막음.
			if ("2".equalsIgnoreCase(bdiv)) {
				sql = "";
				sql += "select top 5 *" + "\n";
				sql += "  from tv92" + "\n";
				sql += " where pid='" + pid + "'" + "\n";
				sql += "   and bededt='" + bededt + "'" + "\n";
				sql += "   and (wdate<'" + wdate + "'" + "\n";
				sql += "       or wdate='" + wdate + "' and wtime<'" + wtime + "'" + "\n";
				sql += "       )" + "\n";
				sql += "   and isnull(result,'')<>''" + "\n";
				sql += " order by wdate desc, wtime desc" + "\n";
			} else {
				sql = "";
				sql += "select top 5 *" + "\n";
				sql += "  from te93" + "\n";
				sql += " where pid='" + pid + "'" + "\n";
				sql += "   and (wdate<'" + wdate + "'" + "\n";
				sql += "       or wdate='" + wdate + "' and wtime<'" + wtime + "'" + "\n";
				sql += "       )" + "\n";
				sql += "   and isnull(upddt,'')=''" + "\n";
				sql += "   and isnull(result,'')<>''" + "\n";
				sql += " order by wdate desc, wtime desc" + "\n";
			}
			//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "sql=" + sql);
			rsString = sqlHelper.executeQuery(sql);
			rs = new ResultSetHelper(rsString);
			long cnt = rs.getRecordCount();
			String previousText = "";
			if (cnt < 1) {
				previousText = "없음";
			} else {
			    StringBuilder prev = new StringBuilder();
			    for (int i = 0; i < cnt; i++) {
			        prev.append(rs.getString(i, "wdate")).append(" ")
			            .append(rs.getString(i, "wtime")).append("\n")
			            .append(rs.getString(i, "result")).append("\n");
			    }
			    previousText = prev.toString();
			}
			*/
	
			String previousText = "없음";
			
		    // OpenAI에 전달할 프롬프트
		    String prompt = "";
		    prompt += "역할:\n";
		    prompt += "당신은 한국어 의료기록 정리 보조 시스텝입니다.\n";
		    prompt += "규칙:\n";
		    prompt += "1.원문에 없는 사실을 추가하기 마세요.\n";
		    prompt += "2.다음 간호기록 원문을 S,O,A,P 형식의 JSON으로 구조화하세요.\n";
		    prompt += "3.반드시 JSON으로 출력하세요\n";
		    prompt += "4.키 이름은 정확히 S, O, A, P 만 사용하세요.\n";
		    prompt += "5.항목에 해당하는 내용이 없으면 빈 문자열로 만드세요.\n";
		    prompt += "6.출력은 자연스러운 한국어로 하고, 원문 의미를 보존하세요.\n";
		    prompt += "7.이전에 작성된 기록지가 있으면 참고하세요.\n";
		    prompt += "\n";
		    prompt += "[이전내용]\n";
		    prompt += previousText;
		    prompt += "\n";
		    prompt += "[원문]\n";
		    prompt += originalText;
		    prompt += "\n";
		    prompt += "[출력예시]\n";
		    prompt += "{\"S\":\"...\",\"O\":\"...\",\"A\":\"...\",\"P\":\"...\"}";
		    
		    //new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "prompt=" + prompt);
	
	
		    URL url = new URL("https://api.openai.com/v1/responses");
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setRequestMethod("POST");
		    conn.setDoOutput(true);
		    conn.setRequestProperty("Authorization", "Bearer " + apiKey);
		    conn.setRequestProperty("Content-Type", "application/json");
	
		    JSONObject openAiReq = new JSONObject();
		    openAiReq.put("model", "gpt-5-mini"); // 모델 : gpt-5-nano < gpt-5-mini < gpt-5 
		    openAiReq.put("input", prompt);
	
		    JSONObject reasoning = new JSONObject();
		    reasoning.put("effort", "minimal");     // 얼마나 깊게 생각할지 : minimal < low < medium < high
		    openAiReq.put("reasoning", reasoning);
		    
		    OutputStream os = conn.getOutputStream();
		    os.write(openAiReq.toString().getBytes("UTF-8"));
		    os.flush();
		    os.close();
	
		    BufferedReader br = new BufferedReader(
		            new InputStreamReader(
		                    conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
		                            ? conn.getInputStream()
		                            : conn.getErrorStream(),
		                    "UTF-8"
		            )
		    );
	
		    String line;
		    StringBuilder openAiResText = new StringBuilder();
		    while ((line = br.readLine()) != null) {
		        openAiResText.append(line);
		    }
		    br.close();
		    conn.disconnect();
	
		    
		    JSONObject openAiRes = new JSONObject(openAiResText.toString());

		    String structuredTextJSON = "";

		    JSONArray outputArray = openAiRes.optJSONArray("output");
		    if (outputArray != null) {
		        for (int i = 0; i < outputArray.length(); i++) {
		            JSONObject outputItem = outputArray.optJSONObject(i);
		            if (outputItem == null) continue;

		            if ("message".equals(outputItem.optString("type"))) {
		                JSONArray contentArray = outputItem.optJSONArray("content");
		                if (contentArray == null) continue;

		                for (int j = 0; j < contentArray.length(); j++) {
		                    JSONObject contentItem = contentArray.optJSONObject(j);
		                    if (contentItem == null) continue;

		                    if ("output_text".equals(contentItem.optString("type"))) {
		                        structuredTextJSON = contentItem.optString("text", "");
		                        break;
		                    }
		                }
		            }

		            if (!"".equals(structuredTextJSON)) break;
		        }
		    }
		    //new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "structuredText=" + structuredText);
		    
			JSONArray result = new JSONArray();
			JSONArray status = new JSONArray();
			JSONArray rowData = new JSONArray();
			JSONObject columns = null;

		    try{
				String structuredText = "";
				
			    JSONObject json = new JSONObject(structuredTextJSON);
			    String S = json.optString("S", "");
			    String O = json.optString("O", "");
			    String A = json.optString("A", "");
			    String P = json.optString("P", "");
			    
			    if (!"".equalsIgnoreCase(S)) {
			    	structuredText += "S" + "\n";
			    	structuredText += S + "\n";
			    }
			    if (!"".equalsIgnoreCase(O)) {
			    	structuredText += "O" + "\n";
			    	structuredText += O + "\n";
			    }
			    if (!"".equalsIgnoreCase(A)) {
			    	structuredText += "A" + "\n";
			    	structuredText += A + "\n";
			    }
			    if (!"".equalsIgnoreCase(P)) {
			    	structuredText += "P" + "\n";
			    	structuredText += P + "\n";
			    }
			    
				columns = new JSONObject();	
				columns.put("result", structuredText);
				rowData.put(columns);
				
				columns = new JSONObject();
				columns.put("return_code", 1);
				columns.put("return_desc", "ok");
				status.put(columns);
				
		    } catch (Exception e) {
		    	// 오류를 반환한다.
				columns = new JSONObject();	
				columns.put("result", "");
				rowData.put(columns);
				
				columns = new JSONObject();
				columns.put("return_code", -1);
				columns.put("return_desc", e.getMessage());
				status.put(columns);
		    }
		    
			// 반환자료
			result.put(status);
			result.put(rowData);
			
			new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "result=" + result.toString());
			
			returnString = result.toString();
	    } catch (SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	    
	}

}
