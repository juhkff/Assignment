package servlets.online.send;

import tools.Online;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static tools.Online.sendRequest;

@WebServlet(name = "SendNoticeServlet", urlPatterns = "/SendRequest")
public class SendNoticeServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID = request.getParameter("userID");
        String nickName = request.getParameter("nickName");
        String receiverID = request.getParameter("receiverID");
        //System.out.println("收到的数据:userID--"+userID+"\tnickName--"+nickName+"\treceiverID--"+receiverID);
        PrintWriter pw = response.getWriter();
        String result = sendRequest(userID, nickName, receiverID);
        pw.print(result);                   //pw:"CF"/"CG"
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
