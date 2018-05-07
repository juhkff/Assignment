package servlets.login;

import com.google.gson.Gson;
import model.message.ChatMessage;
import tools.Login;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

@WebServlet(name = "UnreadChatServlet", urlPatterns = "/GetChat")
public class UnreadChatServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID = request.getParameter("userID");
        Timestamp timestamp = null;
        String chatList = null;
        try {
            timestamp = Login.getExitTime(userID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            ArrayList<ChatMessage> chatMessages = Login.getChatMessage(userID, timestamp);       //获得所有最新聊天消息
            Gson gson = new Gson();
            chatList = gson.toJson(chatMessages);                              //ChatMessage列表的Json格式
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PrintWriter pw = response.getWriter();
        if (!chatList.equals("null") && !chatList.equals("") && chatList != null && !chatList.equals("[]") && !chatList.equals("")) {                             //Json列表若为空，返回的是字符串的null而非null类型
            pw.print(chatList);
        } else {
            pw.print("none");                                                           /** pw:列表/none **/
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
