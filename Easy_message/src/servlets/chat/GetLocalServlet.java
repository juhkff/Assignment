package servlets.chat;

import tools.Chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "GetLocalServlet", urlPatterns = "/GetLocalAddress")
public class GetLocalServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String senderID = request.getParameter("senderID");
        String anotherID = request.getParameter("anotherID");
        String address = Chat.getLocalAddress(anotherID);
        String myAddress = Chat.getLocalAddress(senderID);
        PrintWriter pw = response.getWriter();
        pw.print(myAddress + "/" + address);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
