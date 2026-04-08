import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * 병원에 접속하여 마스터를 매번 읽는 시간을 줄이기 위하여 메모리에 올려놓는다.
 * 최종적으로 메모리에 올려 놓은지 24시간이 지나면 다시 읽는다.
 */
public class D_TC002_testcd {
	String testcdKey; // testcdKey = TestCd+ AppDt + AppTm + EquipCd + SpcCd 
	Calendar readCalendar;
	Map<String,D_TC002> tc002s;
	
	public D_TC002_testcd() {
		this.testcdKey="";
		this.readCalendar=null;
		this.tc002s=null;
	}
	
	public void init() {
		this.testcdKey="";
		this.readCalendar=null;
		this.tc002s=null;
	}
	
	public void setTC002(D_TC002 tc002) {
		String testcdKey = tc002.testcd + "," + tc002.appdt + "," + tc002.apptm;
		String key = tc002.testcd+ "," + tc002.appdt + "," + tc002.apptm + "," + tc002.equipcd + "," + tc002.spccd ;
		if(this.readCalendar==null) this.readCalendar = Calendar.getInstance();
		if(this.tc002s==null) this.tc002s=new HashMap<String,D_TC002>();
		this.testcdKey = testcdKey;
		this.tc002s.put(key, tc002);
	}
	
	public Map<String,D_TC002> getTC002s() {
		return this.tc002s;
	}
	
	public boolean isReRead() {
		// 다시 읽어야 하는지 여부
		if(this.readCalendar==null) return true;
		Calendar c1 = Calendar.getInstance();
		if(readCalendar.get(Calendar.DATE)!=c1.get(Calendar.DATE)) return true;
		return false;
	}
}
