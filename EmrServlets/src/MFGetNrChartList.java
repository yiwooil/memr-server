import java.sql.SQLException;
import java.util.HashMap;

public class MFGetNrChartList implements MFGet {
	private static MFGetNrChartList mInstance=null;
	private MFGetNrChartList(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetNrChartList getInstance(){
		if(mInstance==null){
			mInstance = new MFGetNrChartList();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String frdt = (String)param.get("frdt");
		String todt = (String)param.get("todt");
		String bdiv = (String)param.get("bdiv");

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		try {
			sqlHelper = new SqlHelper(hospitalId);
			
			String sql = "";
			if("2".equals(bdiv)){
				sql += "select v92.wdate, v92.seq, v92.wtime, v92.result, v92.pnures as empid, a13.empnm" + "\n";
				sql += "  from tv92 v92 left join ta13 a13 on a13.empid=v92.pnures" + "\n";
				sql += " where v92.pid=?" + "\n";
				sql += "   and v92.bededt=?" + "\n";
				sql += "   and v92.wdate between ? and ?" + "\n";
				sql += " order by v92.wdate desc, v92.wtime desc, v92.seq desc" + "\n";
				
				para.put(1, pid);
				para.put(2, bededt);
				para.put(3, frdt);
				para.put(4, todt);
			}else{
				sql += "select e93.wdate, e93.seq, e93.wtime, e93.result, e93.empid, a13.empnm" + "\n";
				sql += "  from te93 e93 left join ta13 a13 on a13.empid=e93.empid" + "\n";
				sql += " where e93.pid=?" + "\n";
				sql += "   and e93.wdate between ? and ?" + "\n";
				sql += "   and isnull(e93.upddt,'')=''" + "\n"; // 취소된 내역 제외
				sql += " order by e93.wdate desc, e93.wtime desc, e93.seq desc" + "\n";
				
				para.put(1, pid);
				para.put(2, frdt);
				para.put(3, todt);
			
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

}
