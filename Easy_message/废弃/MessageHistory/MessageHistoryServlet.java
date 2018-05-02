package servlets.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.ChatMessage;
import tools.Chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "MessageHistoryServlet",urlPatterns = "/MessageHistory")
public class MessageHistoryServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        String anotherID = request.getParameter("anotherID");
        ArrayList<ChatMessage> chatMessages = null;

        try {
            chatMessages=Chat.getHistoryMessage(userID,anotherID);
        }catch (Exception e){
            e.printStackTrace();
        }
        PrintWriter printWriter = response.getWriter();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String chatMessageList = gson.toJson(chatMessages);
        printWriter.print(chatMessageList);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
