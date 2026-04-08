import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ContentHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.sax.WriteOutContentHandler;

public class Utility {
	/***
	 * 
	 * @return 현재일자를 yyyyMMdd 형식으로 반환한다.
	 */
	public static String getCurrentDate() {
		java.util.Date d = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd"); 
		String currentDate = df.format(d);
		return currentDate;
	}
	
	/***
	 * 
	 * @return 현재일자시간을 yyyy-MM-dd HH:mm:ss 형식으로 반환한다.
	 */
	public static String getCurrentDateTime() {
		java.util.Date d = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		String currentDateTime = df.format(d);
		return currentDateTime;
	}
	
	/***
	 * 
	 * @param v 일자로 yyyyMMdd형식임.
	 * @return 일자를 yyyy.MM.dd 형식으로 반환한다.
	 */
	public static String getFormattedDate(String v) {
		if(v==null) return "";
		if(v.equals("")) return "";
		if(v.length()<8) return v;
		return v.substring(0, 4)+"."+v.substring(4, 6)+"."+v.substring(6, 8);
	}
	
	public static String getFormattedDate(String v, String dateFormat) {
		if(v==null) return "";
		if(v.equals("")) return "";
		if(v.length()<8) return v;
		return v.substring(0, 4)+"-"+v.substring(4, 6)+"-"+v.substring(6, 8);
	}
	
	// 일자로 MM.dd.yyyy로 반환
	public static String getFormattedDate_mdy(String v) {
		if(v==null) return "";
		if(v.equals("")) return "";
		if(v.length()<8) return v;
		return v.substring(4, 6)+"."+v.substring(6, 8)+"."+v.substring(0, 4);
	}
	
	// 일자로 dd.MM.yyyy로 반환
	public static String getFormattedDate_dmy(String v) {
		if(v==null) return "";
		if(v.equals("")) return "";
		if(v.length()<8) return v;
		return v.substring(6, 8)+"."+v.substring(4, 6)+"."+v.substring(0, 4);
	}

	// 숫자월을 영문으로 변경
	public static String getMmEng(String v) {
		if("01".equals(v)) return "Jan.";
		if("02".equals(v)) return "Feb.";
		if("03".equals(v)) return "Mar.";
		if("04".equals(v)) return "Apr.";
		if("05".equals(v)) return "May";
		if("06".equals(v)) return "June";
		if("07".equals(v)) return "Jul.";
		if("08".equals(v)) return "Aug.";
		if("09".equals(v)) return "Sept.";
		if("10".equals(v)) return "Oct.";
		if("11".equals(v)) return "Nov.";
		if("12".equals(v)) return "Dec.";
		return v;
	}
	
	public static String getYy(String v) {
		if(v==null) return "";
		if(v.equals("")) return "";
		if(v.length()<8) return v;
		return v.substring(0, 4);		
	}
	
	public static String getMm(String v){
		if(v==null) return "";
		if(v.equals("")) return "";
		if(v.length()<8) return v;
		return v.substring(4, 6);
		
	}
	
	public static String getDd(String v){
		if(v==null) return "";
		if(v.equals("")) return "";
		if(v.length()<8) return v;
		return v.substring(6, 8);		
	}
	
	public static String getFormattedResidWithMark(String v) {
		if(v==null) return "";
		if(v.equals("")) return "";
		if(v.length()<7) return v;
		return v.substring(0, 6)+"-"+v.substring(6, 7)+"******";
	}
	
	/***
	 * 
	 * @return basecamp database connection string을 반환한다.
	 */
	public static String getBasecampDbConnectionUrl() {
		String ret = "jdbc:sqlserver://localhost:1433;databaseName=Basecamp;user=sa;password=mms;"; 
		try {
			ret = getXmlValue("config", "basecamp", "database_url");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/***
	 * 
	 * @param paraKeys
	 * @param request
	 * @return
	 */
	public static HashMap<String,Object> getParaMap(String[] paraKeys, HttpServletRequest request){
		HashMap<String,Object>para = new HashMap<String,Object>();
		if(paraKeys!=null && paraKeys.length>0){
			for(int i=0;i<paraKeys.length;i++){
				para.put(paraKeys[i], request.getParameter(paraKeys[i]));
			}
		}
		return para;
	}
	
	/***
	 * json 을 분석하여 html 테이블 형태로 반환한다.
	 * @param jsonString
	 * @return
	 */
	public static String jsonToHtmlTable(String jsonString){
		String returnString="";
		TreeMap<String,String>keyMap = new TreeMap<String,String>();
		try {
			JSONArray main = new JSONArray(jsonString);
			int mainCount=main.length();
			for(int i=0;i<mainCount;i++){
				returnString+="<table border=1>";
				JSONArray control = main.getJSONArray(i);
				int controlCount=control.length();
				if(controlCount<1) continue;
				// field명을 구한다
				returnString+="<tr>";
				returnString+="<th>[key]</th>";
				Iterator keys = control.getJSONObject(0).keys();
				while(keys.hasNext()){
					String key=(String)keys.next();
					returnString+="<th>"+key+"</th>";
				}
				returnString+="</tr>";
				// 값을 구한다.
				for(int j=0;j<controlCount;j++){
					returnString+="<tr>";
					returnString+="<td>"+j+"</td>";
					keys = control.getJSONObject(0).keys();
					while(keys.hasNext()){
						String key=(String)keys.next();
						String val=control.getJSONObject(j).getString(key);
						if(control.getJSONObject(j).isNull(key)){
							returnString+="<td><i>null</i></td>"; // null 이면 이탤릭체
						}else{
							if("".equals(val.trim())) val="&nbsp;"; // 공백도 보이게
							returnString+="<td>"+val+"</td>";
						}
					}
					returnString+="</tr>";
				}
				returnString+="</table>";
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			returnString = e.getMessage();
		} finally {
			return returnString;
		}
	}
	
	public static String rtfToPlainText(String rtfString){
		String returnString="";
		try {
			RTFParser rtfParser = new RTFParser();
			Metadata metadata = new Metadata();
			WriteOutContentHandler handler = new WriteOutContentHandler();
			
			InputStream is = new ByteArrayInputStream(rtfString.getBytes("UTF-8"));
			rtfParser.parse(is, handler, metadata, null);
			returnString=handler.toString();
			if("".equals(returnString.trim())) returnString=rtfString;
			return returnString;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			returnString = e.getMessage();
			return returnString;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			returnString = e.getMessage();
			return returnString;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			returnString = e.getMessage();
			return returnString;
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			returnString = e.getMessage();
			return returnString;
		}
	}
	
	public static String concatFilePath(String path, String file){
		String pathName = path;
		String fileName = file;
		if(!"".equalsIgnoreCase(pathName)){
			if(pathName.endsWith("/")) pathName = pathName.substring(0, pathName.length()-1);
			if(pathName.endsWith("\\")) pathName = pathName.substring(0, pathName.length()-1);
		}
		if(!"".equalsIgnoreCase(fileName)){
			if(fileName.startsWith("/")) fileName = fileName.substring(1);
			if(fileName.startsWith("\\")) fileName = fileName.substring(1);
		}
		return pathName + File.separator + fileName;
	}
	
	public static String concatFilePath(String path1, String path2, String file){
		String newPath = concatFilePath(path1, path2);
		return concatFilePath(newPath, file);
	}
	
	public static String getXmlValue(String xmlFileName, String element, String node) throws ParserConfigurationException, SAXException, IOException{
		// xml 파일으 기본구성
		// <?xml veersion="1.0" encoding="utf-8" ?>
		// <config>
		//     <hospital>
		//         <id>0000</id>
		//     </hospital>
		// </config>
		String fileName = "." + File.separator
				        + "webapps" + File.separator 
				        + "emrdroid" + File.separator
				        + "WEB-INF" + File.separator
				        + "classes" + File.separator
				        + xmlFileName + ".xml";
		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		
		//doc.getDocumentElement().getNodeName() <-- root 엘리먼트를 구할 수 있음.
		
		// hospital 엘리먼트 리스트
		NodeList hosList = doc.getElementsByTagName(element); //"hospital");
		Node hosNode = hosList.item(0);
		
		// hospital 엘리먼트
		Element hosElement = (Element)hosNode;
		
		// id 태그
		NodeList idList = hosElement.getElementsByTagName(node); //"id");
		Element idElement = (Element)idList.item(0);
		Node id = idElement.getFirstChild();
		
		// id 값
		String idValue = id.getNodeValue();
		return idValue;
	}
	
	public static List<String> getXmlValueList(String xmlFileName, String element, String node) throws ParserConfigurationException, SAXException, IOException{
		// xml 파일으 기본구성
		// <?xml veersion="1.0" encoding="utf-8" ?>
		// <config>
		//     <hospital>
		//         <id>0000</id>
		//     </hospital>
		//     <hospital>
		//         <id>0001</id>
		//     </hospital>
		// </config>
		List<String> retList = new ArrayList<String>();
		
		String fileName = "." + File.separator
				        + "webapps" + File.separator 
		                + "emrdroid" + File.separator
		                + "WEB-INF" + File.separator
		                + "classes" + File.separator
		                + xmlFileName + ".xml";
		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		
		//doc.getDocumentElement().getNodeName() <-- root 엘리먼트를 구할 수 있음.
		
		// hospital 엘리먼트 리스트
		NodeList hosList = doc.getElementsByTagName(element); //"hospital");
		
		for(int i=0 ; i<hosList.getLength();i++){
			Node hosNode = hosList.item(i);
			
			// hospital 엘리먼트
			Element hosElement = (Element)hosNode;
			
			// id 태그
			NodeList idList = hosElement.getElementsByTagName(node); //"id");
			Element idElement = (Element)idList.item(0);
			Node id = idElement.getFirstChild();
			
			// id 값
			String idValue = id.getNodeValue();
			retList.add(idValue);
		}
		return retList;
	}
	
	public static int getAgeYear(String birthDate, int errorValue) {
		// 일자인지 체크
		if(birthDate.length()!=8) return errorValue;
		String today="";//오늘 날짜
		int ageYear=0;//만 나이
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		today=formatter.format(new Date()); // 시스템 날짜
		// 현재일자
		int todayYear = Integer.parseInt(today.substring(0,4));
		int todayMonth = Integer.parseInt(today.substring(4,6));
		int todayDay = Integer.parseInt(today.substring(6,8));
		// 생년월일
		int birthYear = 0;
		int birthMonth = 0;
		int birthDay = 0;
		try{
			birthYear = Integer.parseInt(birthDate.substring(0,4));
			birthMonth = Integer.parseInt(birthDate.substring(4,6));
			birthDay = Integer.parseInt(birthDate.substring(6,8));
		}catch(NumberFormatException e){
			// 숫자로 변환 오류. 일자형식이 아니다.
			return errorValue;
		}
		
		ageYear = todayYear - birthYear;
		
		if(todayMonth<birthMonth){ // 생년월일 "월" 이 지났는지 체크
			ageYear--;
		}else if(todayMonth==birthMonth){ // 생년월일 "일"이 지났는지 체크
			if(todayDay<birthDay){
				ageYear--; // 생일이 안지났으면 (만나이 -1)
			}
		}
		
		return ageYear;
	}
	
	public static String getAgeYMD(String Fday, String Tday){
		if(valDt(Fday)==false || valDt(Tday)==false) return "";
		if(Fday.compareToIgnoreCase(Tday)>0) return "";

		int Dcnt[] = {31,28,31,30,31,30,31,31,30,31,30,31};

		int FYY = 0;
		int FMM = 0;
		int FDD = 0;

		int TYY = 0;
		int TMM = 0;
		int TDD = 0; 
			
		try{
			FYY = Integer.parseInt(Fday.substring(0, 4));
			FMM = Integer.parseInt(Fday.substring(4, 6));
			FDD = Integer.parseInt(Fday.substring(6, 8));
	
			TYY = Integer.parseInt(Tday.substring(0, 4));
			TMM = Integer.parseInt(Tday.substring(4, 6));
			TDD = Integer.parseInt(Tday.substring(6, 8));
		}catch(Exception ex){
			return "";
		}

		if(((FYY%4)==0)&&(((FYY%100)!=0)||((FYY%400)==0))){
			Dcnt[1] = 29;
		}
		
		int OYY, OMM, ODD;
		OYY = TYY - FYY - 1;
		OMM = (12 - FMM) + (TMM - 1);
		ODD = Dcnt[FMM - 1] - FDD + TDD;
		if(ODD>=Dcnt[FMM-1]){
			ODD = ODD - Dcnt[FMM-1];
			OMM += 1;
		}
		if(ODD>=Dcnt[TMM-1]){
			ODD = ODD - Dcnt[TMM-1];
			OMM += 1;
		}
		for(; OMM>=12;){
			OMM = OMM - 12;
			OYY = OYY + 1;
		}

		String rtn = "";
		rtn += Integer.toString(10000 + OYY).substring(1, 5);
		rtn += Integer.toString(100 + OMM).substring(1, 3);
		rtn += Integer.toString(100 + ODD).substring(1, 3);
		
		return rtn;
	}
	
	public static boolean valDt(String value){
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			sdf.setLenient(false);
			sdf.parse(value);
			return true;
		}catch(Exception ex){
			return false;
		}
	}
}
