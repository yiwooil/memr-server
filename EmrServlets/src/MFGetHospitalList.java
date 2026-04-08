import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MFGetHospitalList implements MFGet {
	private static MFGetHospitalList mInstance=null;
	private MFGetHospitalList(){
		
	}
	
	public static MFGetHospitalList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetHospitalList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String xmlFileName = "config";
		
		JSONArray result = new JSONArray();
		JSONArray status = new JSONArray();
		JSONArray rowData = new JSONArray();
		JSONObject columns = null;
		
		int rowCount=0;
		
		String fileName = "." + File.separator
		        + "webapps" + File.separator 
                + "emrdroid" + File.separator
                + "WEB-INF" + File.separator
                + "classes" + File.separator
                + xmlFileName + ".xml";

		new LogWrite().debugWrite(getClass().getSimpleName(), "fileName = ", fileName);
		
		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		
		//doc.getDocumentElement().getNodeName() <-- root 엘리먼트를 구할 수 있음.
		
		// hospital 엘리먼트 리스트
		NodeList hosList = doc.getElementsByTagName("hospital");
		
		for(int i=0 ; i<hosList.getLength();i++){
			Node hosNode = hosList.item(i);
			
			// hospital 엘리먼트
			Element hosElement = (Element)hosNode;
			
			// id 태그
			NodeList idList = hosElement.getElementsByTagName("id");
			Element idElement = (Element)idList.item(0);
			Node id = idElement.getFirstChild();
			
			// id 값
			String idValue = id.getNodeValue();
			
		
			rowCount++;
			columns = new JSONObject();
			columns.put("hospital_id", idValue);
			
			NodeList nodeList = null;
			Element element = null;
			Node node = null;
			String nodeValue = "";
			// hospital_name
			nodeList = hosElement.getElementsByTagName("hospital_name");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("hospital_name", nodeValue);
			// mask_yn
			nodeList = hosElement.getElementsByTagName("mask_yn");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("mask_yn", nodeValue);
			// database_url
			nodeList = hosElement.getElementsByTagName("database_url");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("database_url", nodeValue);
			// home_url
			nodeList = hosElement.getElementsByTagName("home_url");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("home_url", nodeValue);
			// scan_url
			nodeList = hosElement.getElementsByTagName("scan_url");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("scan_url", nodeValue);
			// presave_url
			nodeList = hosElement.getElementsByTagName("presave_url");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("presave_url", nodeValue);
			// mp4_url
			nodeList = hosElement.getElementsByTagName("mp4_url");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("mp4_url", nodeValue);
			// pic_url
			nodeList = hosElement.getElementsByTagName("pic_url");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("pic_url", nodeValue);
			// interface_table_yn
			nodeList = hosElement.getElementsByTagName("interface_table_yn");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("interface_table_yn", nodeValue);
			// emr_company
			nodeList = hosElement.getElementsByTagName("emr_company");
			if(nodeList.getLength()>0){
				element = (Element)nodeList.item(0);
				node = element.getFirstChild();
				nodeValue = node.getNodeValue();
			}else{
				nodeValue="";
			}
			columns.put("emr_company", nodeValue);
			//
			rowData.add(columns);
		}
		// 리턴값과 메시지
		columns = new JSONObject();
		columns.put("return_code",rowCount);
		columns.put("return_desc","ok");
		status.add(columns);
		// 반환자료
		result.add(status);
		result.add(rowData);
					
		return result.toJSONString();
	}
}
