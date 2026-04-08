
public class D_TC004 {
	String testcd; // pk
	String equipcd; // pk
	String spccd; // pk
	String appdt; // pk
	String apptm; // pk
	String sex; // pk
	String agefr; // pk
	String ageto; // pk
	String agediv;
	String referfr;
	String signfr;
	String signto;
	String referto;
	String refer;
	String seq;
	String deldt;
	
	public D_TC004(){
		testcd="";
		equipcd="";
		spccd="";
		appdt="";
		apptm="";
		sex="";
		agefr="";
		ageto="";
		agediv="";
		referfr="";
		signfr="";
		signto="";
		referto="";
		refer="";
		seq="";
		deldt="";
	}
	
	public Integer getAgefr(){
		Integer agefrInt = 0;
		try{
			agefrInt = Integer.parseInt(agefr);
		}catch(NumberFormatException ex) {
			agefrInt = 0;
		}
		return agefrInt;
	}
	
	public Integer getAgeto(){
		Integer agetoInt = 0;
		try{
			agetoInt = Integer.parseInt(ageto);
		}catch(NumberFormatException ex) {
			agetoInt = 0;
		}
		return agetoInt;
	}
}
