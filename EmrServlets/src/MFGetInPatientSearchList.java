import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class MFGetInPatientSearchList implements MFGet {
	private static MFGetInPatientSearchList mInstance=null;
	private MFGetInPatientSearchList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetInPatientSearchList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetInPatientSearchList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("usrid");
		String sortOrder = (String)param.get("sortorder");
		String ward = (String)param.get("ward");
		String dept = (String)param.get("dept");
		String searchText = (String)param.get("searchtext");
		String exdt = (String)param.get("exdt");
		String exdtto = (String)param.get("exdtto"); // 2023.03.27 WOOIL - 
		String searchiofg = (String)param.get("searchiofg"); // 2025.08.07 WOOIL - 0:외래+입원 1:외래만 2:입원만
		
		if(exdt==null) exdt="";
		if(exdtto==null) exdtto=exdt;
		if(searchiofg==null) searchiofg="0";
		
		String pidYn = ""; // 환자id검색인지
		String pnmYn = ""; // 환자명검색인지
		String residYn = ""; // 주민번호검색인지
		
		if(searchText.length()==9 && (searchText.startsWith("T")||searchText.startsWith("t"))){
			// T로 시작하고 9자리이면 환자ID로 검색.
			pidYn = "Y";
			pnmYn = "";
			residYn = "";
		}else if(searchText.matches("\\d{9}")){
			// 9자리 숫자이면 환자ID로 검색.
			pidYn = "Y";
			pnmYn = "";
			residYn = "";
		}else if(searchText.matches("\\d{6}")){
			// 6자리 숫자이면 주민번호 앞 6자리와 환자id로 검색.
			pidYn = "Y";
			pnmYn = "";
			residYn = "";
			// 6자리 숫자이고 일자 형식인 경우만 주민번호 앞자리로 간주한다.
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			sdf.setLenient(false);
			try{
				sdf.parse(searchText);
				residYn = "Y";
			}catch(Exception ex){}
		}else if(searchText.matches("\\d+")){
			// 모두 숫자임 환자id로 검색.
			pidYn = "Y";
			pnmYn = "";
			residYn = "";
		}else{
			// 환자명으로 검색.
			pidYn = "";
			pnmYn = "Y";
			residYn = "";
		}
		
		// 환자ID검색을 9자로 하기위한 처리
		String pidText = "";
		String pnmText = "";
		String residText = "";
		
		if("Y".equals(pidYn)){
			pidText = searchText;
			while(true){
				if(pidText.length()>=9) break;
				pidText = "0" + pidText; 
			}
		}
		if("Y".equals(pnmYn)){
			pnmText = searchText;
		}
		if("Y".equals(residYn)){
			residText = searchText;
		}
		

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			String sql="";
			sql=getSql(sortOrder,exdt,searchiofg,pidYn,pnmYn,residYn);
 
			if("".equals(exdt)){
				// 현재 재원환자 중에서 검색. 
				// 화면에 진료일자가 추가되지 않은 앱에서 호출할 경우를 대비하기 위한 용도임.
				if("Y".equals(pnmYn)) para.put(++idx, pnmText + "%"); // 이름
				if("Y".equals(residYn)) para.put(++idx, residText); // 주민번호 6자리
				if("Y".equals(pidYn)) para.put(++idx, pidText); // 환자ID
			}else{
				// 외래 접수환자중에서 검색(시작일~종료일)
				para.put(++idx, exdt);
				para.put(++idx, exdtto);
				if("Y".equals(pnmYn)) para.put(++idx, pnmText + "%"); // 이름
				if("Y".equals(residYn)) para.put(++idx, residText); // 주민번호 6자리
				if("Y".equals(pidYn)) para.put(++idx, pidText); // 환자ID
				// 재원 환자중에서 검색(시작일~종료일)
				if("0".equals(searchiofg)){
					// iofg=0 인 경우만 외래+입원에서 검색하므로 이 블럭이 필요함.
					//           아니면 외래나 입원 중 하나만 검색하므로 이 블럭이 필요없음.
					para.put(++idx, exdt);
					para.put(++idx, exdtto);
					if("Y".equals(pnmYn)) para.put(++idx, pnmText + "%"); // 이름
					if("Y".equals(residYn)) para.put(++idx, residText); // 주민번호 6자리
					if("Y".equals(pidYn)) para.put(++idx, pidText); // 환자ID
				}
			}
			
			String rsString=sqlHelper.executeQuery(sql, para, null);
			returnString=rsString;
			
		}catch(SQLException ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "SQLException", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}catch(Exception ex) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", ex.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(ex);
		}
		return returnString;
	}
	
	private String getSql(String sortOrder, String exdt, String searchiofg, String pidYn, String pnmYn, String residYn){
		String key=sortOrder;
		if(!"".equals(exdt)) key+=",exdt";
		key+=","+searchiofg+","+pidYn+","+pnmYn+","+residYn;
		if(sqlMap.containsKey(key)==false){
			if("".equals(exdt)){
				// 2023.03.27 WOOIL - 일자가 없으면(화면에 일자가 없으면) 재원환자에서만 찾는다.
				String sql = "";
				sql += "select a01.pnm";
				sql += "     , a01.psex";
				sql += "     , a04.dptcd";
				sql += "     , a04.bededt";
				sql += "     , a04.pid";
				sql += "     , isnull(a04.bedodt,'') as bedodt";
				sql += "     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age";
				sql += "     , a04.wardid+case when isnull(a04.rmid,'')<>'' then '-' else '' end+isnull(a04.rmid,'')+case when isnull(a04.bedid,'')<>'' then '-' else '' end+isnull(a04.bedid,'') as ward";
				sql += "     , (select a07.drnm from ta07 a07 (nolock) where a07.drid=a04.pdrid) as pdrnm";
				sql += "     , a04.qlfycd as qfycd";
				sql += "     , (select a88.cdnm from ta88 a88 (nolock) where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=a04.qlfycd) as qfycdnm";
				sql += "     , isnull((select top 1 dxd from tt05 t05 (nolock) where t05.pid=a04.pid and t05.bdedt=a04.bededt order by convert(numeric,t05.ptysq),t05.seq),'') as dxd";
				sql += "     , a01.bthdt";
				sql += "     , a04.pdrid as drid"; // 2024.06.21 WOOIL -
				sql += "     , a01.resid1"; // 2024.09.09 WOOIL -
				sql += "  from ta04 a04 (nolock) inner join ta01 a01 (nolock) on a01.pid=a04.pid ";
				sql += " where isnull(a04.bedodiv,'') in ('0','')";
				sql += "   /*and a04.wardid<>'er1'*/ ";
				if("Y".equals(pnmYn)){
					sql += "   and a01.pnm like ? ";	
				}
				else if("Y".equals(pidYn)&&"Y".equals(residYn)){
					sql += "   and (a01.resid1=? or";
					sql += "        a04.pid=?";
					sql += "       )";
				}
				else if("Y".equals(pidYn)){
					sql += "   and a04.pid=? ";
				}
				else if("Y".equals(residYn)){
					sql += "   and a01.resid1=? ";
				}
				sqlMap.put(key, sql);
			}else{
				// 2023.03.27 WOOIL - 일자 범위에 외래 접수 환자와 재원 환자를 찾는다.
				String sql = "";
				if("0".equals(searchiofg)||"1".equals(searchiofg)){
					// 외래+입원 이거나 외래만
					sql += "select a01.pnm" + "\r\n";
					sql += "     , a01.psex" + "\r\n";
					sql += "     , s21.dptcd" + "\r\n";
					sql += "     , s21.exdt as bededt" + "\r\n";
					sql += "     , s21.pid" + "\r\n";
					sql += "     , s21.hms as bedodt" + "\r\n";
					sql += "     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age" + "\r\n";
					sql += "     , case when s21.dptcd='ER' then '응급' else '외래' end as ward" + "\r\n";
					sql += "     , (select a07.drnm from ta07 a07 (nolock) where a07.drid=s21.drid) as pdrnm" + "\r\n";
					sql += "     , s21.qfycd as qfycd" + "\r\n";
					sql += "     , (select a88.cdnm from ta88 a88 (nolock) where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=s21.qfycd) as qfycdnm" + "\r\n";
					sql += "     , isnull((select top 1 dxd from ts06 s06 (nolock) where s06.pid=s21.pid and s06.exdt=s21.exdt and s06.dptcd=s21.dptcd order by convert(numeric,s06.ptysq),s06.seq),'') as dxd" + "\r\n";
					sql += "     , a01.bthdt" + "\r\n";
					sql += "     , s21.drid" + "\r\n"; // 2024.06.21 WOOIL - 
					sql += "     , a01.resid1"; // 2024.09.09 WOOIL -
					sql += "  from ts21 s21 (nolock) inner join ta01 a01 (nolock) on a01.pid=s21.pid " + "\r\n";
					sql += " where s21.exdt>=?" + "\r\n";
					sql += "   and s21.exdt<=?" + "\r\n";
					
					if("Y".equals(pnmYn)){
						sql += "   and a01.pnm like ? ";	
					}
					else if("Y".equals(pidYn)&&"Y".equals(residYn)){
						sql += "   and (a01.resid1=? or";
						sql += "        s21.pid=?";
						sql += "       )";
					}
					else if("Y".equals(pidYn)){
						sql += "   and s21.pid=? ";
					}
					else if("Y".equals(residYn)){
						sql += "   and a01.resid1=? ";
					}
					
					sql += "   and isnull(s21.ccfg,'') in ('','0')" + "\r\n";
				}
				if("0".equals(searchiofg)){
					// 외래+입원
					sql += " union all " + "\r\n";
				}
				if("0".equals(searchiofg)||"2".equals(searchiofg)){
					// 외래+입원 이거나 입원만
					sql += "select a01.pnm" + "\r\n";
					sql += "     , a01.psex" + "\r\n";
					sql += "     , a04.dptcd" + "\r\n";
					sql += "     , a04.bededt" + "\r\n";
					sql += "     , a04.pid" + "\r\n";
					sql += "     , isnull(a04.bedodt,'') as bedodt" + "\r\n";
					sql += "     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age" + "\r\n";
					sql += "     , a04.wardid+case when isnull(a04.rmid,'')<>'' then '-' else '' end+isnull(a04.rmid,'')+case when isnull(a04.bedid,'')<>'' then '-' else '' end+isnull(a04.bedid,'') as ward" + "\r\n";
					sql += "     , (select a07.drnm from ta07 a07 (nolock) where a07.drid=a04.pdrid) as pdrnm" + "\r\n";
					sql += "     , a04.qlfycd as qfycd" + "\r\n";
					sql += "     , (select a88.cdnm from ta88 a88 (nolock) where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=a04.qlfycd) as qfycdnm" + "\r\n";
					sql += "     , isnull((select top 1 dxd from tt05 t05 (nolock) where t05.pid=a04.pid and t05.bdedt=a04.bededt order by convert(numeric,t05.ptysq),t05.seq),'') as dxd" + "\r\n";
					sql += "     , a01.bthdt" + "\r\n";
					sql += "     , a04.pdrid as drid" + "\r\n";
					sql += "     , a01.resid1"; // 2024.09.09 WOOIL -
					sql += "  from ta04 a04 (nolock) inner join ta01 a01 (nolock) on a01.pid=a04.pid " + "\r\n";
					sql += " where (a04.bedodt>=? or isnull(a04.bedodt,'')='')" + "\r\n";
					sql += "   and a04.bededt<=?" + "\r\n";
					
					if("Y".equals(pnmYn)){
						sql += "   and a01.pnm like ? ";	
					}
					else if("Y".equals(pidYn)&&"Y".equals(residYn)){
						sql += "   and (a01.resid1=? or";
						sql += "        a04.pid=?";
						sql += "       )";
					}
					else if("Y".equals(pidYn)){
						sql += "   and a04.pid=? ";
					}
					else if("Y".equals(residYn)){
						sql += "   and a01.resid1=? ";
					}
					
					sql += "   and a04.wardid<>'er1' " + "\r\n";
				}
				if (sortOrder.equals("1")) {
					sql +=
						" order by 1"; // a01.pnm ";
				}else if (sortOrder.equals("2")) {
					sql +=   
						" order by 8,1"; //a04.ward,a01.pnm ";
				}else if (sortOrder.equals("3")) {
					sql +=   
						" order by 3,1"; //a04.dptcd,a01.pnm ";
				}
				sqlMap.put(key, sql);
				
			}
		}
		return sqlMap.get(key);
	}

	
}
