import java.util.HashMap;


public class MFGetSaveCcfFileName implements MFGet {
	private static MFGetSaveCcfFileName mInstance=null;
	private MFGetSaveCcfFileName(){
		
	}
	
	public static MFGetSaveCcfFileName getInstance(){
		if(mInstance==null){
			mInstance = new MFGetSaveCcfFileName();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String mst3cd = (String)param.get("mst3cd");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String presave = (String)param.get("presave");
		String bdiv = (String)param.get("bdiv");
		String applyExdt = (String)param.get("apply_exdt"); // 2026.01.30 WOOIL - 사용자가 exdt를 수정할 수 있게 함.

		if (pid == null) pid = "";
		if (bededt == null) bededt = "";
		if (bdiv == null) bdiv = "";
		if (applyExdt == null) applyExdt = "";

		HashMap<Integer, Object>para = new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString = "";
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			//
			//String inoutfg = "I";
			//if(bdiv.equals("1")) inoutfg = "O"; // 외래
			String rptcd = "ZZ01"; // android에서 올린 사인이미지
			//String path = "";
			String sql = "";
			StringBuilder sb = new StringBuilder();
			if("Y".equalsIgnoreCase(presave)){
				// 2021.10.14 WOOIL - 임시저장인 경우 입원,외래에 관계없이 seq를 구한다.
				if(interfaceTableYn){
					sb.append("select isnull(max(convert(numeric,scan_seq)),0)+1 as seq,convert(varchar,getdate(),112) as sysdt,replace(convert(varchar,getdate(),8),':','') as systm");
					sb.append("  from presaved_consent_form_hist");
					sb.append(" where pat_id=?");
					sb.append("   and scan_class=?");
					if (!"".equalsIgnoreCase(applyExdt)) {
						sb.append("   and scan_date=?"); // 2026.01.30 WOOIL - exdt가 넘어온 경우 그대로 사용한다.
					}else{
						sb.append("   and scan_date=convert(varchar,getdate(),112)");
					}
				}else{
					sb.append("select isnull(max(convert(numeric,seq)),0)+1 as seq,convert(varchar,getdate(),112) as sysdt,replace(convert(varchar,getdate(),8),':','') as systm");
					sb.append("  from tg02t ");
					sb.append(" where pid=?");
					sb.append("   and rptcd=?");
					if (!"".equalsIgnoreCase(applyExdt)) {
						sb.append("   and exdt=?"); // 2026.01.30 WOOIL - exdt가 넘어온 경우 그대로 사용한다.
					}else{
						sb.append("   and exdt=convert(varchar,getdate(),112)");
					}
				}
				sql = sb.toString();
				para.put(++idx, pid);
				para.put(++idx, rptcd);
				if (!"".equalsIgnoreCase(applyExdt)) para.put(++idx, applyExdt);
			}else{
				if(interfaceTableYn){
					sb.append("select isnull(max(convert(numeric,scan_seq)),0)+1 as seq,convert(varchar,getdate(),112) as sysdt,replace(convert(varchar,getdate(),8),':','') as systm");
					sb.append("  from signed_consent_form_hist");
					sb.append(" where pat_id=?");
					sb.append("   and scan_class=?");				
					if (!"".equalsIgnoreCase(applyExdt)) {
						sb.append("   and scan_date=?"); // 2026.01.30 WOOIL - exdt가 넘어온 경우 그대로 사용한다.
					}else{
						sb.append("   and scan_date=convert(varchar,getdate(),112)");
					}
				}else{
					sb.append("select isnull(max(convert(numeric,seq)),0)+1 as seq,convert(varchar,getdate(),112) as sysdt,replace(convert(varchar,getdate(),8),':','') as systm");
					sb.append("  from tg02 ");
					sb.append(" where pid=?");
					sb.append("   and rptcd=?");
					if (!"".equalsIgnoreCase(applyExdt)) {
						sb.append("   and exdt=?"); // 2026.01.30 WOOIL - exdt가 넘어온 경우 그대로 사용한다.
					}else{
						sb.append("   and exdt=convert(varchar,getdate(),112)");
					}
				}
				sql = sb.toString();
				para.put(++idx, pid);
				para.put(++idx, rptcd);
				if (!"".equalsIgnoreCase(applyExdt)) para.put(++idx, applyExdt);
			}
			String rsString = sqlHelper.executeQuery(sql,para,null);
			returnString = rsString;
		} catch (Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString = ExceptionHelper.toJSONString(ex);
		}
		return returnString;			
	}

}
