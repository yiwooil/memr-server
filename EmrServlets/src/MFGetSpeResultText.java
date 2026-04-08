import java.sql.SQLException;
import java.util.HashMap;

public class MFGetSpeResultText implements MFGet {
	private static MFGetSpeResultText mInstance=null;
	private MFGetSpeResultText(){
		
	}
	
	public static MFGetSpeResultText getInstance(){
		if(mInstance==null){
			mInstance = new MFGetSpeResultText();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		String hospitalid = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		String odt = (String)param.get("odt");
		String ono = (String)param.get("ono");
		String bdiv = (String)param.get("bdiv");
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		String returnString;
		
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalid);
			String sql = "";
			if(bdiv.equals("2")||bdiv.equals("3")) {
				sql = getSqlIn();
				para.put(++idx, pid); paraType.put(idx, "C");
				para.put(++idx, bededt); paraType.put(idx, "D");
				para.put(++idx, odt); paraType.put(idx, "D");
				para.put(++idx, ono); paraType.put(idx, "N");
			}else if(bdiv.equals("1")) {
				sql = getSqlOut();
				para.put(++idx, pid); paraType.put(idx, "C");
				para.put(++idx, odt); paraType.put(idx, "D");
				para.put(++idx, ono); paraType.put(idx, "N");
			}
			String rsString=sqlHelper.executeQuery(sql,para,paraType);
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
	
	private String getSqlIn(){
		String sql =
				"select q01.acptdt,isnull(q01.phtdt,'') as phtdt,isnull(q01.rptdt,'') as rptdt,isnull(q02.rptxt,'') as rptxt" +
			    "  from tv01 v01 inner join tq01 q01 on q01.pid=v01.pid and q01.bededt=v01.bededt and q01.odt=v01.odt and q01.ono=v01.ono" +
			    "                left join tq02 q02 on q02.rptdt=q01.rptdt and q02.rptno=q01.rptno" +
			    " where v01.pid = ?" +
			    "   and v01.bededt = ?" +
			    "   and v01.odt = ?" +
			    "   and v01.ono = ?" +
			    "";
		return sql;
	}
	
	private String getSqlOut(){
		String sql =
				"select q01.acptdt,isnull(q01.phtdt,'') as phtdt,isnull(q01.rptdt,'') as rptdt,isnull(q02.rptxt,'') as rptxt" +
			    "  from te01 e01 inner join tq01 q01 on q01.pid=e01.pid and q01.odt=e01.odt and q01.ono=e01.ono" +
			    "                left join tq02 q02 on q02.rptdt=q01.rptdt and q02.rptno=q01.rptno" +
			    " where e01.pid = ?" +
			    "   and e01.odt = ?" +
			    "   and e01.ono = ?" +
			    "";
		return sql;
	}

}
