import java.sql.SQLException;
import java.util.HashMap;


public class MFGetCertificatePreSavedListAll implements MFGet {
	private static MFGetCertificatePreSavedListAll mInstance=null;
	private MFGetCertificatePreSavedListAll(){
		
	}
	
	public static MFGetCertificatePreSavedListAll getInstance(){
		if(mInstance==null){
			mInstance = new MFGetCertificatePreSavedListAll();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String searchText = (String)param.get("searchtext");
		String sortOrder  = (String)param.get("sortorder");
		String dept       = (String)param.get("dept"); // 2026.02.20 WOOIL - 진료과
		String pdrid      = (String)param.get("pdrid"); // 2026.02.20 WOOIL - 의사
		
		if(searchText==null) searchText = "";
		if(searchText.length()<2) searchText = ""; // 최소한 두 글자이상이어야 함.
		String searchPid = "";
		if(!"".equalsIgnoreCase(searchText)){
			searchPid = searchText;
			while(true){
				if(searchPid.length()>=9) break;
				searchPid = "0" + searchPid;
			}
		}
		if(sortOrder==null) sortOrder = "";
		if(dept == null) dept = "";
		if(pdrid == null) pdrid = "";
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "searchText=" + searchText + ", dept=" + dept + ", pdrid=" + pdrid);
		
		String returnString=null;
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		// 2024.06.21 WOOIL - 입원환자인 경우 TA04에서 DPTCD와 DRID(PDRID)를 가져왔었는데 이를 TG02T에서 가져오도록 수정.
		SqlHelper sqlHelper;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			String sql = "";
			sql += "select a01.pnm" + "\r\n";
			sql += "     , a01.psex" + "\r\n";
			sql += "     , isnull(g02t.dptcd,'') as dptcd" + "\r\n"; // 2024.06.21 WOOIL - 진료과
			sql += "     , isnull(g02t.drid,'') as drid" + "\r\n"; // 2024.06.21 WOOIL - 의사
			sql += "     , g02t.exdt as bededt" + "\r\n";
			sql += "     , g02t.pid" + "\r\n";
			sql += "     , g02t.exdt as bedodt" + "\r\n"; // 외래는 진료일시.. 이곳에서는 구할 수 없다.
			sql += "     , convert(varchar,dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112))) as age" + "\r\n";
			sql += "     , '' as ward" + "\r\n";
			sql += "     , isnull((select a09.dptnm from ta09 a09 where a09.dptcd=g02t.dptcd),'') as dptnm" + "\r\n"; // 2024.06.21 WOOIL -
			sql += "     , isnull((select a07.drnm from ta07 a07 where a07.drid=g02t.drid),'') as pdrnm" + "\r\n"; // 2024.06.21 WOOIL -
			sql += "     , isnull(g02t.qfycd,'') as qfycd" + "\r\n";
			sql += "     , isnull((select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=g02t.qfycd),'') as qfycdnm" + "\r\n";
			sql += "     , a01.bthdt" + "\r\n";
			sql += "     , 'pre_saved' as ccf_id" + "\r\n";
			sql += "     , g02t.rptnm2 as ccf_name" + "\r\n";
			sql += "     , g02t.path as ccf_filename" + "\r\n";
			sql += "     , g02t.exdt" + "\r\n";
			sql += "     , g02t.seq" + "\r\n";
			sql += "     , g02t.bdiv" + "\r\n";
			sql += "     , g02t.rptcd2 as emr_scan_class" + "\r\n";
			sql += "     , isnull(g02t.sub_page_no,'') as sub_page_no" + "\r\n";
			sql += "     , isnull(g02t.sub_page_list,'') as sub_page_list" + "\r\n";
			sql += "     , g02t.bdiv as pre_saved_bdiv" + "\r\n";
			sql += "     , case when g02t.pid like 'T%' then 1 else 0 end as sort_col" + "\r\n";
			//
			sql += "     , '' as a04_dptcd" + "\r\n"; // 2024.06.21 WOOIL - 진료과(TA04) 외래는 없음.
			sql += "     , '' as a04_pdrid" + "\r\n"; // 2024.06.21 WOOIL - 의사(TA04) 외래는 없음.
			sql += "     , '' as a04_qfycd" + "\r\n";
			sql += "     , '' as a04_dptnm" + "\r\n"; // 2024.06.21 WOOIL - 외래는 없음.
			sql += "     , '' as a04_pdrnm" + "\r\n"; // 2024.06.21 WOOIL - 외래는 없음.
			sql += "     , '' as a04_qfycdnm" + "\r\n";
			sql += "  from tg02t g02t inner join ta01 a01 on a01.pid=g02t.pid " + "\r\n";
			sql += " where isnull(g02t.delfg,'')=''" + "\r\n";
			if(!"".equalsIgnoreCase(dept)){
				sql += "   and g02t.dptcd='" + dept + "'" + "\r\n"; // 진료과
			}
			if(!"".equalsIgnoreCase(pdrid)){
				sql += "   and g02t.drid='" + pdrid + "'" + "\r\n"; // 의사
			}
			if(!"".equalsIgnoreCase(searchText)){
				sql += "   and (g02t.pid = '" + searchPid + "'" + "\r\n"; // 환자ID로 검색
				sql += "       or a01.pnm like '" + searchText + "%'" + "\r\n"; // 환자명으로 검색
				sql += "       or g02t.exdt =  '" + searchText + "'" + "\r\n"; // 일자로
				sql += "       )" + "\r\n";
			}

			if("1".equals(sortOrder)){
				sql += " order by g02t.exdt desc";
				sql += "        , case when g02t.pid like 'T%' then 1 else 0 end" + "\r\n"; // 2023.03.29 WOOIL - 테스트환자가 맨 뒤로 가도록
				sql += "        , a01.pnm,g02t.pid,g02t.seq desc";
			} else if("2".equals(sortOrder)){
				sql += " order by g02t.rptnm2";
				sql += "        , case when g02t.pid like 'T%' then 1 else 0 end" + "\r\n"; // 2023.03.29 WOOIL - 테스트환자가 맨 뒤로 가도록
				sql += "        , a01.pnm,g02t.pid,g02t.seq desc";
			} else { 
				sql += " order by case when g02t.pid like 'T%' then 1 else 0 end" + "\r\n"; // 2023.03.29 WOOIL - 테스트환자가 맨 뒤로 가도록
				sql += "        , a01.pnm,g02t.pid,g02t.exdt desc,g02t.seq desc";
			}
			String rsString = sqlHelper.executeQuery(sql,para,null);
			
			// 
			ResultSetHelper rs = new ResultSetHelper(rsString);
			int rsCount = rs.getRecordCount();
			for (int i=0 ; i<rsCount; i++) {
				
				String dptcd = rs.getString(i, "dptcd");
				String drid = rs.getString(i, "drid");
				
				String bdiv = rs.getString(i, "bdiv");
				String pid = rs.getString(i, "pid");
				String exdt = rs.getString(i, "exdt");
				
				if ("O".equalsIgnoreCase(bdiv)) {
					rs.putValue(i, "ward", "외래");
				} else if ("I".equalsIgnoreCase(bdiv)) {
					rs.putValue(i, "ward", "병동");
					// 입원 환자면 TA04를 읽어서 추가 정보를 얻는다.
					
					String sqlA04 = "";
					sqlA04 += "select a04.bededt" + "\r\n";
					sqlA04 += "     , a04.bedodt" + "\r\n";
					sqlA04 += "     , a04.dptcd" + "\r\n";
					sqlA04 += "     , a04.pdrid" + "\r\n";
					sqlA04 += "     , a04.qlfycd" + "\r\n";
					sqlA04 += "     , a04.wardid" + "\r\n";
					sqlA04 += "     , a04.rmid" + "\r\n";
					sqlA04 += "     , a04.bedid" + "\r\n";
					sqlA04 += "     , (select a09.dptnm from ta09 a09 where a09.dptcd=a04.dptcd) as dptnm" + "\r\n";
					sqlA04 += "     , (select a07.drnm from ta07 a07 where a07.drid=a04.pdrid) as pdrnm" + "\r\n";
					sqlA04 += "     , (select a88.cdnm from ta88 a88 where a88.mst1cd='a' and a88.mst2cd='26' and a88.mst3cd=a04.qlfycd) as qfycdnm" + "\r\n";
					sqlA04 += "  from ta04 a04" + "\r\n";
					sqlA04 += " where a04.pid=?" + "\r\n";
					sqlA04 += "   and a04.bededt=(select max(x.bededt) from ta04 x where x.pid=a04.pid and x.bededt<=?)" + "\r\n";
					
					HashMap<Integer, Object>paraA04 = new HashMap<Integer,Object>();
					paraA04.put(1, pid);
					paraA04.put(2, exdt);
					
					String rsStrA04 = sqlHelper.executeQuery(sqlA04, paraA04, null);
					ResultSetHelper rsA04 = new ResultSetHelper(rsStrA04);
					if (rsA04.getRecordCount() > 0) {
						String wardid = rsA04.getString(0, "wardid");
						String rmid = rsA04.getString(0, "rmid");
						String bedid = rsA04.getString(0, "bedid");
						String ward = wardid;
						if (!"".equalsIgnoreCase(rmid)){
							ward += "-" + rmid;
							if(!"".equalsIgnoreCase(bedid)){
								ward += "-" + bedid;
							}
						}
						
						rs.putValue(i, "bededt", rsA04.getString(0, "bededt"));
						rs.putValue(i, "bedodt", rsA04.getString(0, "bedodt"));
						rs.putValue(i, "ward", ward);
						rs.putValue(i, "a04_dptcd", rsA04.getString(0, "dptcd"));
						rs.putValue(i, "a04_pdrid", rsA04.getString(0, "pdrid"));
						rs.putValue(i, "a04_qfycd", rsA04.getString(0, "qlfycd"));
						rs.putValue(i, "a04_dptnm", rsA04.getString(0, "dptnm"));
						rs.putValue(i, "a04_pdrnm", rsA04.getString(0, "pdrnm"));
						rs.putValue(i, "a04_qfycdnm", rsA04.getString(0, "qfycdnm"));
						
					}
				}
			}
			
			returnString = rs.toJSONString();
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
