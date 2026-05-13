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


public class MFGetHospitalInformation implements MFGet {
	private static MFGetHospitalInformation mInstance=null;
	private MFGetHospitalInformation(){
		
	}
	
	public static MFGetHospitalInformation getInstance(){
		if(mInstance==null){
			mInstance = new MFGetHospitalInformation();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String xmlFileName = (String)param.get("xml_file_name");
		xmlFileName = "config";
		
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
			
		
			if(idValue.equalsIgnoreCase(hospitalId)){
				rowCount++;
				columns = new JSONObject();
				
				NodeList nodeList = null;
				Element element = null;
				Node node = null;
				String nodeValue = "";
				
				columns.put("hospital_name", getElementValue(hosElement, "hospital_name"));
				columns.put("mask_yn", getElementValue(hosElement, "mask_yn"));
				columns.put("database_url", getElementValue(hosElement, "database_url"));
				columns.put("home_url", getElementValue(hosElement, "home_url")); // home_url/form 에서 동의서양식을 가져온다.
				columns.put("scan_url", getElementValue(hosElement, "scan_url")); // 이미지를 저장하는 폴더
				columns.put("presave_url", getElementValue(hosElement, "presave_url")); // 이미지를 임시로저장하는 폴더
				columns.put("mp4_url", getElementValue(hosElement, "mp4_url")); // 녹음파일을 저장하는 폴더
				columns.put("pic_url", getElementValue(hosElement, "pic_url")); // 사진파일을 저장하는 폴더
				columns.put("ccf_image_format", getElementValue(hosElement, "ccf_image_format")); // 우리 emr에서 png를 처리하지 못해서 jpg로 하면 jpg로 처리됨
				columns.put("interface_table_yn", getElementValue(hosElement, "interface_table_yn"));
				columns.put("emr_company", getElementValue(hosElement, "emr_company"));
				columns.put("emr_scan_url", getElementValue(hosElement, "emr_scan_url"));
				columns.put("emr_scan_url_format", getElementValue(hosElement, "emr_scan_url_format"));
				columns.put("emr_date_format", getElementValue(hosElement, "emr_date_format"));
				columns.put("emr_time_format", getElementValue(hosElement, "emr_time_format"));
				columns.put("emr_resid_format", getElementValue(hosElement, "emr_resid_format"));
				columns.put("in_patient_list_doct_deptnm", getElementValue(hosElement, "in_patient_list_doct_deptnm"));
				columns.put("in_patient_list_doct_popup_button_hide_yn", getElementValue(hosElement, "in_patient_list_doct_popup_button_hide_yn"));
				columns.put("tpr_edit_button_hide_yn", getElementValue(hosElement, "tpr_edit_button_hide_yn"));				
				columns.put("pwd_skp_yn", getElementValue(hosElement, "pwd_skp_yn"));
				columns.put("jain_com_pid_len", getElementValue(hosElement, "jain_com_pid_len"));
				columns.put("filename_prefix", getElementValue(hosElement, "filename_prefix"));
				columns.put("filename_prefix_presave", getElementValue(hosElement, "filename_prefix_presave"));
				columns.put("filename_prefix_pic", getElementValue(hosElement, "filename_prefix_pic"));
				columns.put("filename_prefix_mp4", getElementValue(hosElement, "filename_prefix_mp4"));
				columns.put("patient_safe_check_yn", getElementValue(hosElement, "patient_safe_check_yn"));
				columns.put("certificate_hide_yn", getElementValue(hosElement, "certificate_hide_yn"));
				columns.put("collapse_yn", getElementValue(hosElement, "collapse_yn"));// 2024.04.23 WOOIL - 동의서 리스트를 기본 펼치지 않을지 여부
				columns.put("use_dr_sign_table", getElementValue(hosElement, "use_dr_sign_table")); // 2025.08.12 WOOIL - 의사 사인을 테이블에서 읽을지 여부
				columns.put("barcode_scanner_yn", getElementValue(hosElement, "barcode_scanner_yn")); // 2026.01.29 WOOIL - 환자안전관리 화면에서 내장 카메라로 바코드를 읽을지 여부
				columns.put("nr_chart_ai_yn", getElementValue(hosElement, "nr_chart_ai_yn")); // 2026.03.20 WOOIL - 간호기록지에 AI기능 활성화 여부
				columns.put("presaved_consent_form_list_collapse_yn", getElementValue(hosElement, "presaved_consent_form_list_collapse_yn")); // 2026.05.13 WOOIL - 임시저장동의서리서트 조회시 동의서+환자명인 경우 동의서별로 접혀서 조회되는지 여부
				// -------------------------------------------------------------------------
				rowData.add(columns);
			}
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
	
	private String getElementValue(Element parentElement, String tagName) {
	    if (parentElement == null || tagName == null) {
	        return "";
	    }

	    NodeList nodeList = parentElement.getElementsByTagName(tagName);

	    if (nodeList == null || nodeList.getLength() == 0) {
	        return "";
	    }

	    Node node = nodeList.item(0);

	    if (node == null) {
	        return "";
	    }

	    String value = node.getTextContent();

	    if (value == null) {
	        return "";
	    }

	    return value.trim();
	}
	
}
