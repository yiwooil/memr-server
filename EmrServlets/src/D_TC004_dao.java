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
 * 마스터 TC004를 매번 병원서버에 접속하지 않고 메모리에 올려놓고 사용한다.
 * 메모리에 올린 일자가 변경되면 다시 읽어온다.
 */
public class D_TC004_dao {
	
	private static D_TC004_dao biz = null;
	
	private Map<String,D_TC004_testcd> tc004map;
	
	private D_TC004_dao() {
		tc004map = null;
	}
	
	public static D_TC004_dao getInstance() {
		if(biz==null) biz = new D_TC004_dao();
		return biz;
	}
	
	public D_TC004 get(String hospitalid,String agediv,String blooddt,String testcd,String equipcd,String spccd,String sex,String age,String appdt,String apptm){
		D_TC004 tc004=null;
		if(tc004map==null) tc004map = new HashMap<String,D_TC004_testcd>();
		String key = hospitalid + "," + testcd + "," + equipcd + "," + spccd + "," + sex + "," + age + "," + appdt + "," + apptm;
		boolean b = false;
		D_TC004_testcd tc004_testcd = tc004map.get(key);
		if(tc004_testcd==null){
			// 읽어서 채운다.
			setMap(hospitalid,testcd,equipcd,spccd,sex,age,appdt,apptm);
			tc004_testcd = tc004map.get(key);
		}else if(tc004_testcd.isReRead()==true){
			// 읽어서 채운다.
			setMap(hospitalid,testcd,equipcd,spccd,sex,age,appdt,apptm);
			//tc004_testcd = tc004map.get(key);
		}else{
			b = true;
		}
		// 찾는다.
		Integer ageInt = 0;
		try{
			ageInt = Integer.parseInt(age);
		}catch(NumberFormatException ex) {
			ageInt = 0;
		}
		if(tc004_testcd!=null){
			for(String keyStr : tc004_testcd.getTC004s().keySet()){
				D_TC004 tc004_tmp = tc004_testcd.getTC004s().get(keyStr);
				if("A".equals(tc004_tmp.sex)||sex.equals(tc004_tmp.sex)){
					if("".equals(tc004_tmp.agefr)&&"".equals(tc004_tmp.ageto)){
						tc004 = tc004_tmp;
						break;
					}else{
						if("".equals(agediv)||"".equals(blooddt)){
							if(ageInt>=tc004_tmp.getAgefr()&&ageInt<=tc004_tmp.getAgeto()){
								tc004 = tc004_tmp;
								break;
							}
						}else{
							if("D".equals(agediv)){
								if(ageInt>=tc004_tmp.getAgefr()&&ageInt<=tc004_tmp.getAgeto()){
									tc004 = tc004_tmp;
									break;
								}
							}else if("M".equals(agediv)){
								DateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
								Date bloodDate;
								try {
									bloodDate = sdFormat.parse(blooddt);
									Calendar bldCal = Calendar.getInstance();
									bldCal.setTime(bloodDate);
									Calendar ageCal = Calendar.getInstance();
									ageCal.setTime(bloodDate);
									ageCal.add(Calendar.DATE, ageInt*(-1));
									Integer ageMonthInt = (bldCal.get(Calendar.MONTH) - ageCal.get(Calendar.MONTH)) * 30;	
									//
									if(ageMonthInt>=tc004_tmp.getAgefr()&&ageMonthInt<=tc004_tmp.getAgeto()){
										tc004 = tc004_tmp;
										break;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
								}
							}else if("Y".equals(agediv)){
								DateFormat sdFormat = new SimpleDateFormat("yyyyMMdd");
								Date bloodDate;
								try {
									bloodDate = sdFormat.parse(blooddt);
									Calendar bldCal = Calendar.getInstance();
									bldCal.setTime(bloodDate);
									Calendar ageCal = Calendar.getInstance();
									ageCal.setTime(bloodDate);
									ageCal.add(Calendar.DATE, ageInt*(-1));
									DateFormat fmt = new SimpleDateFormat("MMdd");
									String ageCalString = fmt.format(ageCal.getTime());
									String bldCalString = fmt.format(bldCal.getTime());
									Integer ageYearInt = 0;
									if(ageCalString.compareTo(bldCalString)>0) {
										ageYearInt = (bldCal.get(Calendar.YEAR) - ageCal.get(Calendar.YEAR) - 1) * 360;
									}else{
										ageYearInt = (bldCal.get(Calendar.YEAR) - ageCal.get(Calendar.YEAR)) * 360;
									}
									//
									if(ageYearInt>=tc004_tmp.getAgefr()&&ageYearInt<=tc004_tmp.getAgeto()){
										tc004 = tc004_tmp;
										break;
									}
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		
		//if(b==true) tc004.refer+="true";
		return tc004;
	}
	
	private void setMap(String hospitalid,String testcd,String equipcd,String spccd,String sex,String age,String appdt,String apptm){
		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		SqlHelper sqlHelper;
		String key = hospitalid + "," + testcd + "," + equipcd + "," + spccd + "," + sex + "," + age + "," + appdt + "," + apptm;
		try {
			int idx=0;
			String sql = 
					"select testcd, equipcd, spccd, appdt, apptm, sex, agefr, ageto, agediv," +
					"       referfr, signfr, signto, referto, refer, seq, deldt" +
				    "  from tc004 " +
				    " where testcd=?" +
					"   and equipcd=?" +
					"   and spccd=?" +
				    "   and appdt=?" +
				    "   and apptm=?";
			para.put(++idx, testcd);
			para.put(++idx, equipcd);
			para.put(++idx, spccd);
			para.put(++idx, appdt);
			para.put(++idx, apptm);
			sqlHelper = new SqlHelper(hospitalid);
			String rsString=sqlHelper.executeQuery(sql,para,null);
			ResultSetHelper rs004=new ResultSetHelper(rsString);
			if (rs004.getRecordCount()>0) {
				D_TC004_testcd tc004_testcd = new D_TC004_testcd();
				for (int i=0;i<rs004.getRecordCount();i++) {
					D_TC004 tc004 = new D_TC004();
					tc004.testcd=rs004.getString(i, "testcd");
					tc004.equipcd=rs004.getString(i, "equipcd");
					tc004.spccd=rs004.getString(i, "spccd");
					tc004.appdt=rs004.getString(i, "appdt");
					tc004.apptm=rs004.getString(i, "apptm");
					tc004.sex=rs004.getString(i, "sex");
					tc004.agefr=rs004.getString(i, "agefr");
					tc004.ageto=rs004.getString(i, "ageto");
					tc004.agediv=rs004.getString(i, "agediv");
					tc004.referfr=rs004.getString(i, "referfr");
					tc004.signfr=rs004.getString(i, "signfr");
					tc004.signto=rs004.getString(i, "signto");
					tc004.referto=rs004.getString(i, "referto");
					tc004.refer=rs004.getString(i, "refer");
					tc004.seq=rs004.getString(i, "seq");
					tc004.deldt=rs004.getString(i, "deldt");
					
					tc004_testcd.setTC004(tc004);
				}
				tc004map.put(key, tc004_testcd);
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
