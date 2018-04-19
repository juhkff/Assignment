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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

@WebServlet(name = "ChatHistoryServlet",urlPatterns = "/getChatHistory")
public class ChatHistoryServlet extends HttpServlet {
    public static void main(String[] args){
        /*Scanner scanner=new Scanner(System.in);
        System.out.println("\n--------------------------------------------------------------------------------------------------");
        System.out.println("|                                                                                                 |\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        System.out.println("|                                                                                                 |\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        System.out.println("|                                                                                                 |\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
*/
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request,response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID=request.getParameter("userID");
        String anotherID=request.getParameter("anotherID");
        ArrayList<ChatMessage> chatMessages = null;
        try {
            chatMessages=Chat.getChatHistoryList(userID,anotherID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter=response.getWriter();
        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        String chatMessageList=gson.toJson(chatMessages);
        printWriter.print(chatMessageList);                                                             //chatMessageList: null / ChatMessage列表
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
