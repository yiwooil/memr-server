import java.util.HashMap;

public class MFGetInPatientInfo implements MFGet {
	private static MFGetInPatientInfo mInstance=null;
	private MFGetInPatientInfo(){
		
	}
	
	public static MFGetInPatientInfo getInstance(){
		if(mInstance==null){
			mInstance = new MFGetInPatientInfo();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");

		HashMap<Integer, Object>para=new HashMap<Integer,Object>();
		HashMap<Integer, String>paraType=new HashMap<Integer,String>();
		
		String returnString;
		SqlHelper sqlHelper;
		ResultSetHelper rs;
		try {
			int idx=0;
			sqlHelper = new SqlHelper(hospitalId);
			String sql =  
					"select a04.pid" +
					"     , a01.pnm" +
					"     , a01.psex" +
					"     , a04.wardid+case when isnull(a04.rmid,'')='' then '' else '-' end+isnull(a04.rmid,'')+case when isnull(a04.bedid,'')='' then '' else '-' end+isnull(a04.bedid,'') as ward" +
					"     , dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112)) as age" +
				    "  from ta04 a04 with (nolock) inner join ta01 a01 with (nolock) on a01.pid=a04.pid" +
				    " where a04.pid=?" + 
				    "   and a04.bededt=(select max(x.bededt) from ta04 x (nolock) where x.pid=a04.pid)" +
				    "   and isnull(a04.bedodiv,'') in ('','0')";
			para.put(++idx, pid); paraType.put(idx, "C");
			String rsString=sqlHelper.executeQuery(sql,para,paraType);
			returnString=rsString;
		}catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", e.getLocalizedMessage());
			returnString=ExceptionHelper.toJSONString(e);
		}
		return returnString;
	}
}
