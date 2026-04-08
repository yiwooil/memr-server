//import java.io.IOException;
//import java.io.PrintWriter;

//import javax.servlet.ServletException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

public class LoadData implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// TODO Auto-generated method stub
		// new TA04Server().start(); // <-- 추후 이곳의 코멘트를 풀면 된다.
	}
}

//
// web.xml 파일에 아래와 같이 클래스를 설정해 준다.
//
// <web-app...>
// ...
// <listener>
//     <listener-class>클래스</listener-class>
// </listener>
// ...
// </web-app...>


// web.xml에 servlet정의를 하시고 
// <load-on-startup>1</load-on-startup>등과 같이 지정하시면 됩니다.