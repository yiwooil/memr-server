
public class LogWrite {

	public void errorWrite(String className, String methodName, String exceptionName, String errorMessage){
		System.err.print("\r\n" + Utility.getCurrentDateTime() + " : " + className + "." + methodName + "." + exceptionName + " " + errorMessage);
	}
	
	public void debugWrite(String className, String methodName, String debugMessage){
		System.out.print("\r\n" + Utility.getCurrentDateTime() + " : " + className + "." + methodName + " " + debugMessage);
	}
	
	public void debugWrite(String className, String methodName, int debugMessage){
		debugWrite(className, methodName, String.valueOf(debugMessage));
	}
}
