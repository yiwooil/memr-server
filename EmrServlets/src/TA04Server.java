import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


public class TA04Server implements Runnable {

	private static boolean serverRunning = false;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println(Utility.getCurrentDateTime() + " : TA04Server - run");
		if(serverRunning) return;
		serverRunning = true;
		
		// 병원id를 불러온다. 한 서버에서 여러 병원을 처리하는 경우를 대비한 작업.
		List<String> hosList=getHosList();
		System.out.println(Utility.getCurrentDateTime() + " : TA04Server " + hosList);
		
		int turnno=0;
		while(true){
			++turnno;
			//System.out.println(Utility.getCurrentDateTime() + " : TA04Server turnno = " + turnno);
			for(int i=0;i<hosList.size();i++){
				String hospitalid=hosList.get(i);
				new TA04ServerThread(hospitalid, turnno).run();
			}
			sleep(600); // 600초 = 10분
			//sleep(30); // <-- 테스트할때
		}
	}
	
	public void start() {
		System.out.println(Utility.getCurrentDateTime() + " : TA04Server - start");
		if(!serverRunning){
			new Thread(this).start();
		}
	}
	
	private void sleep(long sec){
		try {
			Thread.sleep(sec*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "sleep", "InterruptedException", e.getLocalizedMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "sleep", "Exception", e.getLocalizedMessage());
		}
	}
	
	private List<String> getHosList(){
		List<String> hosList=null;
		try {
			hosList = Utility.getXmlValueList("config", "hospital", "id");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getHosList", "ParserConfigurationException", e.getLocalizedMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getHosList", "SAXException", e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getHosList", "IOException", e.getLocalizedMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			new LogWrite().errorWrite(getClass().getSimpleName(), "getHosList", "Exception", e.getLocalizedMessage());
		}
		return hosList;
	}
	
	class TA04ServerThread implements Runnable {
		String hospitalid="";
		int turnno=0;
		public TA04ServerThread(String hospitalId,int turnNo){
			hospitalid=hospitalId;
			turnno=turnNo;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			D_TA04_list instance = D_TA04_list.getInstance();
			instance.set(hospitalid,turnno);
		}
	}
	
}
