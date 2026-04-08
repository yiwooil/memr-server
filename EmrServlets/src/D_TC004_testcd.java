import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * 병원에 접속하여 마스터를 매번 읽는 시간을 줄이기 위하여 메모리에 올려놓는다.
 * 최종적으로 메모리에 올려 놓은지 24시간이 지나면 다시 읽는다.
 */
public class D_TC004_testcd {
	String testcdKey; // testcdKey = TestCd + EquipCd + SpcCd + AppDt + AppTm
	Calendar readCalendar;
	Map<String,D_TC004> tc004s;
	
	public D_TC004_testcd() {
		this.testcdKey="";
		this.readCalendar=null;
		this.tc004s=null;
	}
	
	public void init() {
		this.testcdKey="";
		this.readCalendar=null;
		this.tc004s=null;
	}
	
	public void setTC004(D_TC004 tc004) {
		String testcdKey = tc004.testcd + "," + tc004.equipcd + "," + tc004.spccd + "," + tc004.appdt + "," + tc004.apptm;
		String key = tc004.testcd + "," + tc004.equipcd + "," + tc004.spccd + "," + tc004.appdt + "," + tc004.apptm + "," + tc004.sex + "," + tc004.agefr + "," + tc004.ageto;
		if(this.readCalendar==null) this.readCalendar = Calendar.getInstance();
		if(this.tc004s==null) this.tc004s=new HashMap<String,D_TC004>();
		this.testcdKey = testcdKey;
		this.tc004s.put(key, tc004);
	}
	
	public Map<String,D_TC004> getTC004s() {
		return this.tc004s;
	}
	
	public boolean isReRead() {
		// 다시 읽어야 하는지 여부
		if(this.readCalendar==null) return true;
		Calendar c1 = Calendar.getInstance();
		if(readCalendar.get(Calendar.DATE)!=c1.get(Calendar.DATE)) return true;
		return false;
	}
}
