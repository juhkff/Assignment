package servlets.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.message.ChatMessage;
import tools.Chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "ChatHistoryServlet", urlPatterns = "/getChatHistory")
public class ChatHistoryServlet extends HttpServlet {
    public static void main(String[] args){
        String userID="1578184936";
        String anotherID="7272022651";

        ArrayList<ChatMessage> chatMessages = null;
        try {
            chatMessages = Chat.getChatHistoryList(userID, anotherID);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String chatMessageList = gson.toJson(chatMessages);
        System.out.println(chatMessageList);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        String anotherID = request.getParameter("anotherID");
        ArrayList<ChatMessage> chatMessages = null;
        try {
            chatMessages = Chat.getChatHistoryList(userID, anotherID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter = response.getWriter();
        if (chatMessages == null) {
            printWriter.print("null");
        } else {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            String chatMessageList = gson.toJson(chatMessages);
            printWriter.print(chatMessageList);                                                             //chatMessageList: null / ChatMessage列表
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
