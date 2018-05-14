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

@WebServlet(name = "OffLineChatServlet", urlPatterns = "/OfflineChat")
public class OffLineChatServlet extends HttpServlet {
    public static void main(String[] args) throws SQLException {
        String userID = "8076357234";
        String anotherID = "1005221246";
        String message = "test";
        String sendTime = "2018-04-20 13:15:22";
        Timestamp timestamp = Timestamp.valueOf(sendTime);
        Chat.insertChatMessage(userID, anotherID, message, timestamp);
//        Chat.updateContactStatus(userID, anotherID);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        String anotherID = request.getParameter("anotherID");
        String message = request.getParameter("message");
        String sendTime = request.getParameter("sendTime");
        Timestamp timestamp = Timestamp.valueOf(sendTime);

        PrintWriter pw = response.getWriter();
        try {
            Chat.insertChatMessage(userID, anotherID, message, timestamp);
//            Chat.updateContactStatus(userID, anotherID);
            pw.print("success");
        } catch (SQLException e) {
            e.printStackTrace();
            pw.print("false");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
