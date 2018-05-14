package servlets.online.get;

import com.google.gson.Gson;
import model.message.ChatMessage;
import tools.Chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "ReceiveChatServlet",urlPatterns = "/ReceiveChat")
public class ReceiveChatServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        String updateTime=request.getParameter("updateTime");

        ArrayList<ChatMessage> chatMessageArrayList=new ArrayList<ChatMessage>();
        chatMessageArrayList= Chat.getChatAfterTime(userID,updateTime);

        Gson gson=new Gson();
        String result=gson.toJson(chatMessageArrayList);
        PrintWriter printWriter=response.getWriter();
        printWriter.print(result);                                      //{} || list
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
