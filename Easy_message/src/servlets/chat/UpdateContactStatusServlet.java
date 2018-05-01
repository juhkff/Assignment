package servlets.chat;

import tools.Chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;

@WebServlet(name = "UpdateContactStatusServlet",urlPatterns = "/updateContactStatus")
public class UpdateContactStatusServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        String anotherID = request.getParameter("anotherID");
        String message = request.getParameter("message");
        String sendTime = request.getParameter("sendTime");
        Timestamp timestamp = Timestamp.valueOf(sendTime);

        PrintWriter pw = response.getWriter();
        Chat.updateContactStatus(userID, anotherID);
        pw.print("success");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
