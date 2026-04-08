import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class MFGetHospitalSql implements MFGet {
	private static MFGetHospitalSql mInstance=null;
	private MFGetHospitalSql(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	private static HashMap<String, String> paraStringMap=null;
	private static HashMap<String, String> paraTypeStringMap=null;
	
	public static MFGetHospitalSql getInstance(){
		if(mInstance==null){
			mInstance = new MFGetHospitalSql();
			sqlMap = new HashMap<String, String>();
			paraStringMap = new HashMap<String, String>();
			paraTypeStringMap = new HashMap<String, String>();
		}
		return mInstance;
	}
	
	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String sqlId = (String)param.get("sqlid");
		String mode = (String)param.get("mode");
		if(mode==null) mode="";
		if("".equalsIgnoreCase(mode)) mode="sql";
		String returnString = "";
		if("sql".equalsIgnoreCase(mode)){
			returnString = getSql(hospitalId, sqlId);
		}else if("paraString".equalsIgnoreCase(mode)){
			returnString = getParaString(hospitalId, sqlId);
		}else if("paraTypeString".equalsIgnoreCase(mode)){
			returnString = getParaTypeString(hospitalId, sqlId);
		}
		return returnString;
	}
	
	private String getSql(String hospitalId, String sqlId) throws ParserConfigurationException, SAXException, IOException, Exception{
		if(sqlMap.containsKey(hospitalId + sqlId)==false){
			String sql = getSqlInXml2(hospitalId, sqlId);
			sqlMap.put(hospitalId + sqlId, sql);
		}
		return sqlMap.get(hospitalId + sqlId);
	}
	
	private String getParaString(String hospitalId, String sqlId) throws ParserConfigurationException, SAXException, IOException, Exception{
		if(paraStringMap.containsKey(hospitalId + sqlId)==false){
			String paraString = getParaStringInXml2(hospitalId, sqlId);
			paraStringMap.put(hospitalId + sqlId, paraString);
		}
		return paraStringMap.get(hospitalId + sqlId);
	}
	
	private String getParaTypeString(String hospitalId, String sqlId) throws ParserConfigurationException, SAXException, IOException, Exception{
		if(paraTypeStringMap.containsKey(hospitalId + sqlId)==false){
			String paraTypeString = getParaTypeStringInXml2(hospitalId, sqlId);
			paraTypeStringMap.put(hospitalId + sqlId, paraTypeString);
		}
		return paraTypeStringMap.get(hospitalId + sqlId);
	}
	
	private String getSqlInXml2(String hospitalId, String sqlId) throws ParserConfigurationException, SAXException, IOException, Exception{
		String xmlFileName = "sql";
		
		String fileName = "." + File.separator
		        + "webapps" + File.separator 
                + "emrdroid" + File.separator
                + "WEB-INF" + File.separator
                + "classes" + File.separator
                + xmlFileName + ".xml";

		File file = new File(fileName);
		if(!file.isFile()) return ""; // 파일이 없음.
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		
		String sqlValue = "";
		
		//doc.getDocumentElement().getNodeName() <-- root 엘리먼트를 구할 수 있음.
		
		NodeList sqlsNodeList = doc.getElementsByTagName("sqls");
		for(int i=0 ; i<sqlsNodeList.getLength() ; i++){
			Node sqlsNode = sqlsNodeList.item(i);
			Element sqlsElement = (Element)sqlsNode;
			String sqlsHospitalId = sqlsElement.getAttribute("hospitalid");
			if(sqlsHospitalId==null) sqlsHospitalId="";
			
			if("".equalsIgnoreCase(sqlsHospitalId) || hospitalId.equalsIgnoreCase(sqlsHospitalId)){
				// 등록된 sql문장에 병원이 등록되어있지 않거나, 동일한 병원인경우 처리
				NodeList sqlList = sqlsElement.getElementsByTagName("sql");
				for(int j=0 ; j<sqlList.getLength();j++){
					Node sqlNode = sqlList.item(j);
					Element sqlElement = (Element)sqlNode;
					String attrId = sqlElement.getAttribute("id");
					if(attrId==null) attrId="";
					if(attrId.equalsIgnoreCase(sqlId)){
						Node node = sqlElement.getFirstChild();
						sqlValue = node.getNodeValue();
						break;
					}
				}
			}
		}
		
		return sqlValue;
	}
	
	private String getParaStringInXml2(String hospitalId, String sqlId) throws ParserConfigurationException, SAXException, IOException, Exception{
		String xmlFileName = "sql";
		
		String fileName = "." + File.separator
		        + "webapps" + File.separator 
                + "emrdroid" + File.separator
                + "WEB-INF" + File.separator
                + "classes" + File.separator
                + xmlFileName + ".xml";

		File file = new File(fileName);
		if(!file.isFile()) return ""; // 파일이 없음.
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		
		String paraStringValue = "";
		
		//doc.getDocumentElement().getNodeName() <-- root 엘리먼트를 구할 수 있음.
		
		NodeList sqlsNodeList = doc.getElementsByTagName("sqls");
		for(int i=0 ; i<sqlsNodeList.getLength() ; i++){
			Node sqlsNode = sqlsNodeList.item(i);
			Element sqlsElement = (Element)sqlsNode;
			String sqlsHospitalId = sqlsElement.getAttribute("hospitalid");
			if(sqlsHospitalId==null) sqlsHospitalId="";
			
			if("".equalsIgnoreCase(sqlsHospitalId) || hospitalId.equalsIgnoreCase(sqlsHospitalId)){
				// 등록된 sql문장에 병원이 등록되어있지 않거나, 동일한 병원인경우 처리
				NodeList sqlList = sqlsElement.getElementsByTagName("sql");
				for(int j=0 ; j<sqlList.getLength();j++){
					Node sqlNode = sqlList.item(j);
					Element sqlElement = (Element)sqlNode;
					String attrId = sqlElement.getAttribute("id");
					if(attrId==null) attrId="";
					if(attrId.equalsIgnoreCase(sqlId)){
						paraStringValue = sqlElement.getAttribute("paraString");
						if(paraStringValue==null) paraStringValue="";
						break;
					}
				}
			}
		}
		
		return paraStringValue;
	}

	private String getParaTypeStringInXml2(String hospitalId, String sqlId) throws ParserConfigurationException, SAXException, IOException, Exception{
		String xmlFileName = "sql";
		
		String fileName = "." + File.separator
		        + "webapps" + File.separator 
                + "emrdroid" + File.separator
                + "WEB-INF" + File.separator
                + "classes" + File.separator
                + xmlFileName + ".xml";

		File file = new File(fileName);
		if(!file.isFile()) return ""; // 파일이 없음.
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		
		String paraTypeStringValue = "";
		
		//doc.getDocumentElement().getNodeName() <-- root 엘리먼트를 구할 수 있음.
		
		NodeList sqlsNodeList = doc.getElementsByTagName("sqls");
		for(int i=0 ; i<sqlsNodeList.getLength() ; i++){
			Node sqlsNode = sqlsNodeList.item(i);
			Element sqlsElement = (Element)sqlsNode;
			String sqlsHospitalId = sqlsElement.getAttribute("hospitalid");
			if(sqlsHospitalId==null) sqlsHospitalId="";
			
			if("".equalsIgnoreCase(sqlsHospitalId) || hospitalId.equalsIgnoreCase(sqlsHospitalId)){
				// 등록된 sql문장에 병원이 등록되어있지 않거나, 동일한 병원인경우 처리
				NodeList sqlList = sqlsElement.getElementsByTagName("sql");
				for(int j=0 ; j<sqlList.getLength();j++){
					Node sqlNode = sqlList.item(j);
					Element sqlElement = (Element)sqlNode;
					String attrId = sqlElement.getAttribute("id");
					if(attrId==null) attrId="";
					if(attrId.equalsIgnoreCase(sqlId)){
						paraTypeStringValue = sqlElement.getAttribute("paraTypeString");
						if(paraTypeStringValue==null) paraTypeStringValue="";
						break;
					}
				}
			}
		}
		
		return paraTypeStringValue;
	}
	
}
