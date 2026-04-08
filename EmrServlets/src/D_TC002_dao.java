import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

/***
 * 
 * @author WILEE
 * 마스터 TC002를 매번 병원서버에 접속하지 않고 메모리에 올려놓고 사용한다.
 * 메모리에 올린 일자가 변경되면 다시 읽어온다.
 */
public class D_TC002_dao {
	
	private static D_TC002_dao biz = null;
	
	private Map<String,D_TC002_testcd> tc002map;
	
	private D_TC002_dao() {
		tc002map = null;
	}
	
	public static D_TC002_dao getInstance() {
		if(biz==null) biz = new D_TC002_dao();
		return biz;
	}
	
	public D_TC002 get(String hospitalid,String testcd, String appdt, String apptm, String equipcd, String spccd){
		D_TC002 tc002=null;
		if(tc002map==null) tc002map = new HashMap<String,D_TC002_testcd>();
		String key = hospitalid + "," + testcd + "," + appdt + "," + apptm;
		boolean b = false;
		D_TC002_testcd tc002_testcd = tc002map.get(key);
		
		if(tc002_testcd==null){
			// 읽어서 채운다.
			setMap(hospitalid,testcd,appdt,apptm);
			tc002_testcd = tc002map.get(key);
		}else if(tc002_testcd.isReRead()==true){
			// 읽어서 채운다.
			setMap(hospitalid,testcd,appdt,apptm);
			//tc004_testcd = tc004map.get(key);
		}else{
			b = true;
		}
		// 찾는다.
		if(tc002_testcd!=null){
			for(String keyStr : tc002_testcd.getTC002s().keySet()){
				D_TC002 tc002_tmp = tc002_testcd.getTC002s().get(keyStr);
				if(!"".equals(equipcd)&&!"".equals(spccd)){
					if(equipcd.equals(tc002_tmp.equipcd)&&spccd.equals(tc002_tmp.spccd)){
						tc002 = tc002_tmp;
						break;
					}
				}else if(!"".equals(equipcd)&&"".equals(spccd)){
					if(equipcd.equals(tc002_tmp.equipcd)){
						tc002 = tc002_tmp;
						break;
					}
				}else if("".equals(equipcd)&&!"".equals(spccd)){
					if(spccd.equals(tc002_tmp.spccd)){
						tc002 = tc002_tmp;
						break;
					}
				}else{
					tc002 = tc002_tmp;
					break;
				}
			}
		}
		
		return tc002;
	}
	
	private void setMap(String hospitalid,String testcd, String appdt, String apptm){
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		SqlHelper sqlHelper;
		String key = hospitalid + "," + testcd + "," + appdt + "," + apptm;
		try {
			int idx=0;
			String sql = 
					"select testcd, equipcd, spccd, appdt, apptm, equipseq, spcseq, autoverify," +
					"       tubecd, volcd, unitcd, barcnt, turnday, turntime, morfg, panicfg, panicfr," +
					"       panicto, deltafg, deltatype, deltafr, deltato, deldt, isnull(b.field1,'') as unitnm" +
				    "  from tc002 a left join tc032 b on b.cddiv='C207' and b.cdval1=a.unitcd" +
				    " where testcd=?" +
				    "   and appdt=?" +
				    "   and apptm=?" +
				    " order by equipseq,spcseq";
			para.put(++idx, testcd);
			para.put(++idx, appdt);
			para.put(++idx, apptm);
			sqlHelper = new SqlHelper(hospitalid);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			ResultSetHelper rs002=new ResultSetHelper(rsString);
			if (rs002.getRecordCount()>0) {
				D_TC002_testcd tc002_testcd = new D_TC002_testcd();
				for (int i=0;i<rs002.getRecordCount();i++) {
					D_TC002 tc002 = new D_TC002();
					tc002.testcd=rs002.getString(i, "testcd");
					tc002.equipcd=rs002.getString(i, "equipcd");
					tc002.spccd=rs002.getString(i, "spccd");
					tc002.appdt=rs002.getString(i, "appdt");
					tc002.apptm=rs002.getString(i, "apptm");
					tc002.equipseq=rs002.getString(i, "equipseq");
					tc002.spcseq=rs002.getString(i, "spcseq");
					tc002.autoverify=rs002.getString(i, "autoverify");
					tc002.tubecd=rs002.getString(i, "tubecd");
					tc002.volcd=rs002.getString(i, "volcd");
					tc002.unitcd=rs002.getString(i, "unitcd");
					tc002.barcnt=rs002.getString(i, "barcnt");
					tc002.turnday=rs002.getString(i, "turnday");
					tc002.turntime=rs002.getString(i, "turntime");
					tc002.morfg=rs002.getString(i, "morfg");
					tc002.panicfg=rs002.getString(i, "panicfg");
					tc002.panicfr=rs002.getString(i, "panicfr");
					tc002.panicto=rs002.getString(i, "panicto");
					tc002.deltafg=rs002.getString(i, "deltafg");
					tc002.deltatype=rs002.getString(i, "deltatype");
					tc002.deltafr=rs002.getString(i, "deltafr");
					tc002.deltato=rs002.getString(i, "deltato");
					tc002.deldt=rs002.getString(i, "deldt");
					tc002.unitnm=rs002.getString(i, "unitnm");
					
					tc002_testcd.setTC002(tc002);
				}
				tc002map.put(key, tc002_testcd);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "setMap", "SQLException", e.getLocalizedMessage());
			//e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "setMap", "JSONException", e.getLocalizedMessage());
			//e.printStackTrace();
		} catch (Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "setMap", "Exception", e.getLocalizedMessage());
		}
	}
}
