import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class D_TA04_list {
	public static final Integer PNM=0;
	public static final Integer WARD=1;
	public static final Integer DPTCD=2;
	
//	private Map<String,D_TA04> ta04map_pnm; // 환자명
//	private Map<String,D_TA04> ta04map_ward; // 병동+병실+병상
//	private Map<String,D_TA04> ta04map_dptcd; // 진료과+환자명
	
	// 이하 병원별
	private Map<String,Map<String,D_TA04>> ta04map_pnm_hos; // 환자명
	private Map<String,Map<String,D_TA04>> ta04map_ward_hos; // 병동+병실+병상
	private Map<String,Map<String,D_TA04>> ta04map_dptcd_hos; // 진료과+환자명
	
	private static D_TA04_list biz = null;
	public static D_TA04_list getInstance() {
		if(biz==null) biz = new D_TA04_list();
		return biz;
	}
	private D_TA04_list() {
		ta04map_pnm_hos = new HashMap<String,Map<String,D_TA04>>();
		ta04map_ward_hos = new HashMap<String,Map<String,D_TA04>>();
		ta04map_dptcd_hos = new HashMap<String,Map<String,D_TA04>>();
	}
	
	public Map<String,D_TA04> get(String hospitalId, Integer queryMode) {
		if(queryMode==PNM){
			return ta04map_pnm_hos.get(hospitalId);
		}else if(queryMode==WARD){
			return ta04map_ward_hos.get(hospitalId);
		}else if(queryMode==DPTCD){
			return ta04map_dptcd_hos.get(hospitalId);
		}else{
			return ta04map_pnm_hos.get(hospitalId);
		}
	}
	
	public void set(String hospitalId,int turnNo){
		// 재원환자마스터에서 자료를 읽어 MAP에 넣는다.
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("hospitalid", hospitalId);
		MFGet instance = MFGetInPatientListBatch.getInstance();
		try {
			String jsonString=instance.getData(param);
			ResultSetHelper rs = new ResultSetHelper(jsonString, false);
			int count=rs.getRecordCount();
			for(int i=0;i<count;i++){
				String pid=rs.getString(i, "pid");
				String pnm=rs.getString(i, "pnm");
				String wardid=rs.getString(i, "wardid");
				String rmid=rs.getString(i, "rmid");
				String bedid=rs.getString(i, "bedid");
				String dptcd=rs.getString(i, "dptcd");
				// ---------------------------------------------------
				String key="";
				Map<String,D_TA04> map=null;
				D_TA04 dTA04 = null;
				// 환자명 순 -----------------------------------------
				key=pnm+pid;
				map = ta04map_pnm_hos.get(hospitalId);
				if(map==null){
					map = new TreeMap<String,D_TA04>();
					ta04map_pnm_hos.put(hospitalId, map);
				}
				if(map.containsKey(key)==false){
					dTA04 = new D_TA04();
					map.put(key, dTA04);
				}else{
					dTA04 = map.get(key);
				}
				dTA04.turnno=turnNo;
				dTA04.pid=rs.getString(i, "pid");
				dTA04.bededt=rs.getString(i, "bededt");
				dTA04.pnm=rs.getString(i, "pnm");
				dTA04.psex=rs.getString(i, "psex");
				dTA04.dptcd=rs.getString(i, "dptcd");
				dTA04.bedodt=rs.getString(i, "bedodt");
				dTA04.ward=rs.getString(i, "ward");
				dTA04.wardid=rs.getString(i, "wardid");
				dTA04.rmid=rs.getString(i, "rmid");
				dTA04.bedid=rs.getString(i, "bedid");
				dTA04.pdrid=rs.getString(i, "pdrid");
				dTA04.pdrnm=rs.getString(i, "pdrnm");
				dTA04.qfycd=rs.getString(i, "qfycd");
				dTA04.qfycdnm=rs.getString(i, "qfycdnm");
				dTA04.dxd=rs.getString(i, "dxd");
				dTA04.bthdt=rs.getString(i, "bthdt");
				dTA04.age="";
				// 병동 순 ----------------------------------------------------------
				key=wardid+rmid+bedid+pnm+pid;
				map = ta04map_ward_hos.get(hospitalId);
				if(map==null){
					map = new TreeMap<String,D_TA04>();
					ta04map_ward_hos.put(hospitalId, map);
				}
				if(map.containsKey(key)==false){
					dTA04 = new D_TA04();
					map.put(key, dTA04);
				}else{
					dTA04 = map.get(key);
				}
				dTA04.turnno=turnNo;
				dTA04.pid=rs.getString(i, "pid");
				dTA04.bededt=rs.getString(i, "bededt");
				dTA04.pnm=rs.getString(i, "pnm");
				dTA04.psex=rs.getString(i, "psex");
				dTA04.dptcd=rs.getString(i, "dptcd");
				dTA04.bedodt=rs.getString(i, "bedodt");
				dTA04.ward=rs.getString(i, "ward");
				dTA04.wardid=rs.getString(i, "wardid");
				dTA04.rmid=rs.getString(i, "rmid");
				dTA04.bedid=rs.getString(i, "bedid");
				dTA04.pdrid=rs.getString(i, "pdrid");
				dTA04.pdrnm=rs.getString(i, "pdrnm");
				dTA04.qfycd=rs.getString(i, "qfycd");
				dTA04.qfycdnm=rs.getString(i, "qfycdnm");
				dTA04.dxd=rs.getString(i, "dxd");
				dTA04.bthdt=rs.getString(i, "bthdt");
				dTA04.age="";
				// 진료과+환자명 순 --------------------------------------------------
				key=dptcd+pnm+pid;
				map = ta04map_dptcd_hos.get(hospitalId);
				if(map==null){
					map = new TreeMap<String,D_TA04>();
					ta04map_dptcd_hos.put(hospitalId, map);
				}
				if(map.containsKey(key)==false){
					dTA04 = new D_TA04();
					map.put(key, dTA04);
				}else{
					dTA04 = map.get(key);
				}
				dTA04.turnno=turnNo;
				dTA04.pid=rs.getString(i, "pid");
				dTA04.bededt=rs.getString(i, "bededt");
				dTA04.pnm=rs.getString(i, "pnm");
				dTA04.psex=rs.getString(i, "psex");
				dTA04.dptcd=rs.getString(i, "dptcd");
				dTA04.bedodt=rs.getString(i, "bedodt");
				dTA04.ward=rs.getString(i, "ward");
				dTA04.wardid=rs.getString(i, "wardid");
				dTA04.rmid=rs.getString(i, "rmid");
				dTA04.bedid=rs.getString(i, "bedid");
				dTA04.pdrid=rs.getString(i, "pdrid");
				dTA04.pdrnm=rs.getString(i, "pdrnm");
				dTA04.qfycd=rs.getString(i, "qfycd");
				dTA04.qfycdnm=rs.getString(i, "qfycdnm");
				dTA04.dxd=rs.getString(i, "dxd");
				dTA04.bthdt=rs.getString(i, "bthdt");
				dTA04.age="";
			}
			// 지우기
			// TA04에 있는 turnno와 파라메터로 넘어온 turnno가 다르면 삭제한다.
			Map<String,D_TA04> map=null;
			// 환자명순
			map = ta04map_pnm_hos.get(hospitalId);
			for(String key : map.keySet()){
				D_TA04 ta04 = map.get(key);
				if(turnNo!=ta04.turnno)	map.remove(key);
			}
			// 병동순
			map = ta04map_ward_hos.get(hospitalId);
			for(String key : map.keySet()){
				D_TA04 ta04 = map.get(key);
				if(turnNo!=ta04.turnno)	map.remove(key);
			}
			// 진료과+환자명순
			map = ta04map_dptcd_hos.get(hospitalId);
			for(String key : map.keySet()){
				D_TA04 ta04 = map.get(key);
				if(turnNo!=ta04.turnno)	map.remove(key);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "set", "Exception", e.getLocalizedMessage());
			//return ExceptionHelper.toJSONString(e);
		}
	}
}
