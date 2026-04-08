import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MFPutNrChart implements MFPut {
	private static MFPutNrChart mInstance=null;
	private MFPutNrChart(){
		
	}
	
	public static MFPutNrChart getInstance(){
		if(mInstance==null){
			mInstance = new MFPutNrChart();
		}
		return mInstance;
	}

	@Override
	public String putData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String bdiv = (String)param.get("bdiv");
		String wdate = (String)param.get("wdate");
		String seq = (String)param.get("seq");
		String wtime = (String)param.get("wtime");
		String result = (String)param.get("result");
		String pdrid = (String)param.get("pdrid");
		
		String iu_div = ("".equalsIgnoreCase(seq) ? "I" : "U");
		
		String pnm = "";
		String psex = "";
		String sysdt = "";
		String systm = "";
		
		String returnString = "";
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		SqlHelper sqlHelper;
		
		try{
			String insString = "";
			sqlHelper = new SqlHelper(hospitalId);
			
			String sql = "";
			sql += "select pnm,psex,convert(varchar,getdate(),112) as sysdt,replace(convert(varchar,getdate(),24),':','') as systm" + "\n";
			sql += "  from ta01" + "\n";
			sql += " where pid='" + pid + "'";
			
			String rsString = sqlHelper.executeQuery(sql);
			ResultSetHelper rs = new ResultSetHelper(rsString);
			if (rs.getRecordCount() > 0) {
				pnm = rs.getString(0, "pnm");
				psex = rs.getString(0, "psex");
				sysdt = rs.getString(0, "sysdt");
				systm = rs.getString(0, "systm");
			}
			
			if ("2".equalsIgnoreCase(bdiv)) {
				// 입원
				if ("U".equalsIgnoreCase(iu_div)) {
					// 수정
					sql = "";
					sql += "update tv92" + "\n";
					sql += "   set result=?" + "\n";
					sql += "     , sysdt=?" + "\n";
					sql += "     , systm=?" + "\n";
					sql += " where pid=?" + "\n";
					sql += "   and bededt=?" + "\n";
					sql += "   and seq=?" + "\n";
					sql += "   and wdate=?" + "\n";
					
					para.put(1, result);
					para.put(2, sysdt);
					para.put(3, systm);
					para.put(4, pid);
					para.put(5, bededt);
					para.put(6, seq);
					para.put(7, wdate);
					
					insString = sqlHelper.executeUpdate(sql, para);
					
				} else {
					// seq번호를 새로 구한다.
					sql = "";
					sql += "select isnull(max(seq),0) max_seq" + "\n";
					sql += "  from tv92" + "\n";
					sql += " where pid='" + pid + "'" + "\n";
					sql += "   and bededt='" + bededt + "'" + "\n";
					
					rsString = sqlHelper.executeQuery(sql);
					rs = new ResultSetHelper(rsString);
					if (rs.getRecordCount() > 0) {
						long max_seq = rs.getLong(0,  "max_seq");
						seq = (max_seq + 1) + "";
					}
					if ("".equalsIgnoreCase(seq)) seq = "1";
					
					// 신규
					sql = "";
					sql += "insert into tv92(pid,bededt,seq,wdate,wtime,pnm,psex,pdrid,pnures,result,sysdt,systm,bdiv)" + "\n";
					sql += "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
					
					para.put(1, pid);
					para.put(2, bededt);
					para.put(3, seq);
					para.put(4, wdate);
					para.put(5, wtime);
					para.put(6, pnm);
					para.put(7, psex);
					para.put(8, pdrid);
					para.put(9, userId);
					para.put(10, result);
					para.put(11, sysdt);
					para.put(12, systm);
					para.put(13, "2");
					
					insString = sqlHelper.executeUpdate(sql, para);
				}
				
			} else {
				// 외래
				// seq번호를 새로 구한다.
				String newSeq = "";
				sql = "";
				sql += "select isnull(max(seq),0) max_seq" + "\n";
				sql += "  from te93" + "\n";
				sql += " where pid='" + pid + "'" + "\n";
				sql += "   and exdt='" + bededt + "'" + "\n";
				
				rsString = sqlHelper.executeQuery(sql);
				rs = new ResultSetHelper(rsString);
				if (rs.getRecordCount() > 0) {
					long max_seq = rs.getLong(0,  "max_seq");
					newSeq = (max_seq + 1) + "";
				}
				if ("".equalsIgnoreCase(newSeq)) newSeq = "1";
				
				if ("U".equalsIgnoreCase(iu_div)) {
					List<String> sqlList = new ArrayList<String>();
					List<HashMap<Integer, Object>> paraList = new ArrayList<HashMap<Integer, Object>>(); 
					
					// 수정 = 이전내역 취소 + 신규
					sql = "";
					sql += "update te93" + "\n";
					sql += "   set updid=?" + "\n";
					sql += "     , upddt=?" + "\n";
					sql += "     , updtm=?" + "\n";
					sql += " where pid=?" + "\n";
					sql += "   and exdt=?" + "\n";
					sql += "   and seq=?" + "\n";
					sql += "   and wdate=?" + "\n";
					
					HashMap<Integer,Object> para1 = new HashMap<Integer,Object>();
					para1.put(1, userId);
					para1.put(2, sysdt);
					para1.put(3, systm);
					para1.put(4, pid);
					para1.put(5, bededt);
					para1.put(6, seq);
					para1.put(7, wdate);
					
					sqlList.add(sql);
					paraList.add(para1);
					
					sql = "";
					sql += "insert into te93(pid,exdt,seq,wdate,wtime,pnm,psex,pdrid,empid,result,sysdt,systm)" + "\n";
					sql += "values(?,?,?,?,?,?,?,?,?,?,?,?)";
					
					HashMap<Integer,Object> para2 = new HashMap<Integer,Object>();
					para2.put(1, pid);
					para2.put(2, bededt);
					para2.put(3, newSeq);
					para2.put(4, wdate);
					para2.put(5, wtime);
					para2.put(6, pnm);
					para2.put(7, psex);
					para2.put(8, pdrid);
					para2.put(9, userId);
					para2.put(10, result);
					para2.put(11, sysdt);
					para2.put(12, systm);
					
					sqlList.add(sql);
					paraList.add(para2);
					
					insString = sqlHelper.executeUpdate(sqlList, paraList);
					
				} else { 
				
					// 신규.
					sql = "";
					sql += "insert into te93(pid,exdt,seq,wdate,wtime,pnm,psex,pdrid,empid,result,sysdt,systm)" + "\n";
					sql += "values(?,?,?,?,?,?,?,?,?,?,?,?)";
					
					para.put(1, pid);
					para.put(2, bededt);
					para.put(3, newSeq);
					para.put(4, wdate);
					para.put(5, wtime);
					para.put(6, pnm);
					para.put(7, psex);
					para.put(8, pdrid);
					para.put(9, userId);
					para.put(10, result);
					para.put(11, sysdt);
					para.put(12, systm);
					
					insString = sqlHelper.executeUpdate(sql, para);
				}
					
			}
			
			
			if(ResultSetHelper.getReturnCount(insString)<0){
				returnString = ResultSetHelper.getReturnDesc(insString);
			}else{
				returnString = "Y";
			}
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
