import java.sql.SQLException;
import java.util.HashMap;


public class MFGetCertificatePreSavedList implements MFGet {
	private static MFGetCertificatePreSavedList mInstance=null;
	private MFGetCertificatePreSavedList(){
		
	}
	
	public static MFGetCertificatePreSavedList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCertificatePreSavedList();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid =  (String)param.get("pid");
		String returnString=null;
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
			if(interfaceTableYn){
				sql += "select 'pre_saved' ccf_id,sub_class_name ccf_name,file_path ccf_filename,'임시저장' ccf_group,scan_date exdt,scan_seq seq,emr_scan_class";
				sql += "     , '' as sub_page_list";
				sql += "     , '' as sub_page_no";
				sql += "     , '' as pre_saved_ccf_id";
				sql += "     , '' as pre_saved_bdiv";
				sql += "  from presaved_consent_form_hist";
				sql += " where pat_id=? and isnull(del_flag,'')=''"; 
				sql += " order by scan_date desc,scan_seq desc";
			}else{
				sql += "select 'pre_saved' ccf_id,rptnm2 ccf_name,path ccf_filename,'임시저장' ccf_group,exdt,seq,rptcd2 as emr_scan_class";
				sql += "     , isnull(sub_page_list,'') as sub_page_list";
				sql += "     , isnull(sub_page_no,'') as sub_page_no";
				sql += "     , ccf_id as pre_saved_ccf_id";
				sql += "     , bdiv as pre_saved_bdiv";
				sql += "  from tg02t";
				sql += " where pid=? and isnull(delfg,'')=''";
				sql += " order by exdt desc,seq desc";
			}
			para.put(1, pid);
			String rsString = sqlHelper.executeQuery(sql,para,null);
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
