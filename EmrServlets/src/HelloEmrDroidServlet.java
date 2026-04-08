import java.io.*;

import javax.servlet.*;
//import javax.servlet.annotation.*;
import javax.servlet.http.*;

public class HelloEmrDroidServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Hello EmrDroid!</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Hello EmrDroid!</h1>");
        out.println("</body>");
        out.println("</html>");
    }
    
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
}
