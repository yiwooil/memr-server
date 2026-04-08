import java.util.HashMap;


public class MFGetInPatientInfoString implements MFGet {
	private static MFGetInPatientInfoString mInstance=null;
	private MFGetInPatientInfoString(){
		
	}
	
	public static MFGetInPatientInfoString getInstance(){
		if(mInstance==null){
			mInstance = new MFGetInPatientInfoString();
		}
		return mInstance;
	}

	@Override
	public String getData(HashMap<String, Object> param) throws Exception {
		String hospitalId = (String)param.get("hospitalid");
		String pid = (String)param.get("pid");
		String bededt = (String)param.get("bededt");

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
					"     , a04.dptcd" +
					"     , a04.wardid+case when isnull(a04.rmid,'')='' then '' else '-' end+isnull(a04.rmid,'')+case when isnull(a04.bedid,'')='' then '' else '-' end+isnull(a04.bedid,'') as ward" +
					"     , isnull(a04.bedodt,'') as bedodt" +
					"     , dbo.mfi_get_age_y(a01.bthdt,convert(varchar,getdate(),112)) as age" +
				    "  from ta04 a04 with (nolock) inner join ta01 a01 with (nolock) on a01.pid=a04.pid" +
				    " where a04.pid=?" + 
				    "   and a04.bededt=?";
			para.put(++idx, pid); paraType.put(idx, "C");
			para.put(++idx, bededt); paraType.put(idx, "D");
			String rsString=sqlHelper.executeQuery(sql,para,paraType);
			rs = new ResultSetHelper(rsString,sqlHelper.getMasking());
			returnString = rs.getString(0, "pnm") + " " +
					       rs.getString(0, "psex") + "/" + rs.getString(0,"age") + " " +
					       rs.getString(0, "dptcd") + " " +
					       rs.getString(0, "ward") + " " +
					       Utility.getFormattedDate(bededt) + "~" + Utility.getFormattedDate(rs.getString(0, "bedodt"));
		}catch(Exception e) {
			new LogWrite().errorWrite(getClass().getSimpleName(), "getData", "Exception", e.getLocalizedMessage());
			returnString = e.getMessage();
		}
		return returnString;
	}

}
