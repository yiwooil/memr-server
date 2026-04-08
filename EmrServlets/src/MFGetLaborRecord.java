import java.sql.SQLException;
import java.util.HashMap;

public class MFGetLaborRecord implements MFGet {
	private static MFGetLaborRecord mInstance=null;
	private MFGetLaborRecord(){
		
	}
	private static HashMap<String, String> sqlMap=null; // sql문장을 메모리에 한번만 올려놓고 다음부터는 계속 사용한다.
	
	public static MFGetLaborRecord getInstance(){
		if(mInstance==null){
			mInstance = new MFGetLaborRecord();
			sqlMap = new HashMap<String, String>();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String userId = (String)param.get("userid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");
		
		new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "pid=" + pid + ", bededt=" + bededt);
		
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		
		SqlHelper sqlHelper;
		String returnString;
		
		try{
			sqlHelper = new SqlHelper(hospitalId);
			String sql="";
			sql += "select a.pid,a.bededt,a.seq a_seq,a.fld_t,a.fld_p,a.fld_a,a.fld_l,a.l_m_p,a.e_d_c,a.g_a,a.onset_labor,a.mem_adm,a.bld_type,a.bld_rh,a.hb_hct,a.gm_per,a.show,a.ks" + "\r\n";
			sql += "     , b.seq b_seq,b.exdt,b.extm,b.b_p,b.p,b.f_h_t,b.b_t,b.position,b.station,b.dilatation,b.interval,b.duration,b.membrane,b.medi_rmk,b.medi_rmk2,b.empid" + "\r\n";
			sql += "     , a13.empnm" + "\r\n";
			sql += "     , a04.dptcd,a04.pdrid,a04.wardid+'-'+a04.rmid+'-'+a04.bedid as ward" + "\r\n";
			sql += "     , a09.dptnm" + "\r\n";
			sql += "     , a07.drnm" + "\r\n";
			sql += "  from emr101_a a (nolock) inner join emr101a_a b (nolock) on b.pid=a.pid and b.bededt=a.bededt and b.seq_bededt=a.seq" + "\r\n";
			sql += "                           inner join ta13 a13 (nolock) on a13.empid=b.empid" + "\r\n";
			sql += "                           inner join ta04 a04 (nolock) on a04.pid=a.pid and a04.bededt=a.bededt" + "\r\n";
			sql += "                           inner join ta09 a09 (nolock) on a09.dptcd=a04.dptcd" + "\r\n";
			sql += "                           inner join ta07 a07 (nolock) on a07.drid=a04.pdrid" + "\r\n";
			sql += " where a.pid=?" + "\r\n";
			sql += "   and a.bededt=?" + "\r\n";
			sql += " order by a.pid,a.bededt,a.seq,b.seq" + "\r\n";
			
			//new LogWrite().debugWrite(getClass().getSimpleName(), "getData", "\r\n" + sql);
			
			para.put(1, pid);
			para.put(2, bededt);
			
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
