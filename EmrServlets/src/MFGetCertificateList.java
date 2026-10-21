import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetCertificateList implements MFGet {
	private static MFGetCertificateList mInstance=null;
	private MFGetCertificateList(){
		
	}
	
	public static MFGetCertificateList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCertificateList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String returnString=null;

		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
			if(interfaceTableYn){
				sql += "select a.ccf_id";
				sql += "     , a.ccf_name";
				sql += "     , case when isnull(a.ccf_filename,'')='' then a.ccf_name else a.ccf_filename end as ccf_filename";
				sql += "     , case when isnull(a.ccf_group,'')='' then '기타' else a.ccf_group end ccf_group";
				sql += "     , isnull(a.emr_scan_class,'') as emr_scan_class";
				sql += "     , isnull(b.scan_class_name,'') as emr_scan_class_name";
				sql += "     , '' as sub_page_list";
				sql += "     , '' as sub_page_no";
				sql += "     , '' as hx_type";
				sql += "  from consent_form_mast a left join scan_class_mast b on b.scan_class=a.emr_scan_class";
				sql += " where isnull(a.del_yn,'')=''";
				sql += " order by convert(numeric,disp_order) ";
			}else{
				sql += "select a.mst3cd ccf_id" + "\r\n";
				sql += "     , a.cdnm ccf_name" + "\r\n";
				sql += "     , case when isnull(a.fld1qty,'')='' then cdnm else a.fld1qty end as ccf_filename" + "\r\n";
				sql += "     , case when isnull(a.fld2cd,'')='' then '기타' else a.fld2cd end ccf_group" + "\r\n";
				sql += "     , isnull(a.fld3cd,'') as emr_scan_class" + "\r\n";
				sql += "     , isnull(b.rptnm,'') as emr_scan_class_name" + "\r\n";
				sql += "     , isnull(a.fld2qty,'') as sub_page_list" + "\r\n";
				sql += "     , isnull(a.fld3qty,'') as sub_page_no" + "\r\n";
				sql += "     , isnull(a.fld6cd,'') as hx_type" + "\r\n";
				sql += "  from ta88 a left join tg01 b on b.rptcd=a.fld3cd" + "\r\n";
				sql += " where a.mst1cd='EMR' and a.mst2cd='FORM'" + "\r\n";
				sql += "   and isnull(a.fld5cd,'')<>'2'" + "\r\n"; // 2025.09.18 WOOIL - 제외기능
				sql += " order by case when isnull(a.fld1cd,'')='' or isnumeric(a.fld1cd)=0" + "\r\n"; // 2026.10.19 WOOIL - varchar -> numeric 오류 수정
				sql += "       then 0" + "\r\n";
				sql += "       else convert(numeric,a.fld1cd)" + "\r\n";
				sql += "  end";				
			}
			String rsString = sqlHelper.executeQuery(sql);
			returnString = rsString;
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
