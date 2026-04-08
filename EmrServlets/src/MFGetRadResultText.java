import java.sql.SQLException;
import java.util.HashMap;


public class MFGetRadResultText implements MFGet {
	private static MFGetRadResultText mInstance=null;
	private MFGetRadResultText(){
		
	}
	
	public static MFGetRadResultText getInstance(){
		if(mInstance==null){
			mInstance = new MFGetRadResultText();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
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
			boolean interfaceTableYn = sqlHelper.getInterfaceTableYn();
			String sql = "";
			if(bdiv.equals("2")||bdiv.equals("3")) {
				if(interfaceTableYn){
					boolean isOracle = sqlHelper.isOracle();
					if(isOracle){
						sql = getSqlInInterOracle();
					}else{
						sql = getSqlInInter();
					}
				}else{
					sql = getSqlIn();
				}
				para.put(++idx, pid); paraType.put(idx, "C");
				para.put(++idx, bededt); paraType.put(idx, "D");
				para.put(++idx, odt); paraType.put(idx, "D");
				para.put(++idx, ono); paraType.put(idx, "N");
			}else if(bdiv.equals("1")) {
				if(interfaceTableYn){
					boolean isOracle = sqlHelper.isOracle();
					if(isOracle){
						sql = getSqlOutInterOracle();
					}else{
						sql = getSqlOutInter();
					}
				}else{
					sql = getSqlOut();
				}
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
				"select x01.acptdt,isnull(x01.phtdt,'') as phtdt,isnull(x01.rptdt,'') as rptdt,isnull(x02.rptxt,'') as rptxt,x02.result,x02.rec" +
			    "  from tv01 v01 inner join tx01 x01 on x01.pid=v01.pid and x01.bededt=v01.bededt and x01.odt=v01.odt and x01.ono=v01.ono" +
			    "                left join tx02 x02 on x02.rptdt=x01.rptdt and x02.rptno=x01.rptno" +
			    " where v01.pid = ?" +
			    "   and v01.bededt = ?" +
			    "   and v01.odt = ?" +
			    "   and v01.ono = ?" +
			    "";
		return sql;
	}
	
	private String getSqlInInter(){
		String sql =
				"select x01.accept_date acptdt,isnull(x01.photo_date,'') as phtdt,isnull(x01.reading_date,'') as rptdt,isnull(x01.reading_result,'') as rptxt,x01.reading_result result,''  rec" +
			    "  from in_pat_order_hist v01 inner join rad_result_hist x01 on x01.pat_id=v01.pat_id and x01.bed_in_date=v01.bed_in_date and x01.order_date=v01.order_date and x01.order_no=v01.order_no" +
			    " where v01.pat_id = ?" +
			    "   and v01.bed_in_date = ?" +
			    "   and v01.order_date = ?" +
			    "   and v01.order_no = ?" +
			    "";
		return sql;
	}
	
	private String getSqlInInterOracle(){
		String sql =
				"select x01.accept_date acptdt,x01.photo_date as phtdt,x01.reading_date as rptdt,x01.reading_result as rptxt,x01.reading_result result,''  rec" +
			    "  from in_pat_order_hist v01 inner join rad_result_hist x01 on x01.pat_id=v01.pat_id and x01.bed_in_date=v01.bed_in_date and x01.order_date=v01.order_date and x01.order_no=v01.order_no" +
			    " where v01.pat_id = ?" +
			    "   and v01.bed_in_date = ?" +
			    "   and v01.order_date = ?" +
			    "   and v01.order_no = ?" +
			    "";
		return sql;
	}

	private String getSqlOut(){
		String sql =
				"select x01.acptdt,isnull(x01.phtdt,'') as phtdt,isnull(x01.rptdt,'') as rptdt,isnull(x02.rptxt,'') as rptxt,x02.result,x02.rec" +
			    "  from te01 e01 inner join tx01 x01 on x01.pid=e01.pid and x01.odt=e01.odt and x01.ono=e01.ono" +
			    "                left join tx02 x02 on x02.rptdt=x01.rptdt and x02.rptno=x01.rptno" +
			    " where e01.pid = ?" +
			    "   and e01.odt = ?" +
			    "   and e01.ono = ?" +
			    "";
		return sql;
	}
	
	private String getSqlOutInter(){
		String sql =
				"select x01.accept_date acptdt,isnull(x01.photo_date,'') as phtdt,isnull(x01.reading_date,'') as rptdt,isnull(x01.reading_result,'') as rptxt,x01.reading_result result,''  rec" +
			    "  from out_pat_order_hist e01 inner join rad_result_hist x01 on x01.pat_id=e01.pat_id and x01.order_date=e01.order_date and x01.order_no=e01.order_no" +
			    " where e01.pat_id = ?" +
			    "   and e01.order_date = ?" +
			    "   and e01.order_no = ?" +
			    "";
		return sql;
	}
	
	private String getSqlOutInterOracle(){
		String sql =
				"select x01.accept_date acptdt,x01.photo_date as phtdt,x01.reading_date as rptdt,x01.reading_result as rptxt,x01.reading_result result,''  rec" +
			    "  from out_pat_order_hist e01 inner join rad_result_hist x01 on x01.pat_id=e01.pat_id and x01.order_date=e01.order_date and x01.order_no=e01.order_no" +
			    " where e01.pat_id = ?" +
			    "   and e01.order_date = ?" +
			    "   and e01.order_no = ?" +
			    "";
		return sql;
	}
}
