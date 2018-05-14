package servlets.client;

import tools.Online;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "CheckPhoneServlet",urlPatterns = "/CheckPhone")
public class CheckPhoneServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        String phoneNum=request.getParameter("phoneNum");

        boolean resultResult=false;
        resultResult=Online.checkPhone(userID,phoneNum);

        PrintWriter printWriter=response.getWriter();
        String result="Wrong";
        if(resultResult){
            result="Right";
        }else {
            result="Wrong";
        }

        printWriter.print(result);          // result: Right / Wrong
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
