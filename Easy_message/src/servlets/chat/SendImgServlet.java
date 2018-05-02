package servlets.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tools.Chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Timestamp;

@WebServlet(name = "SendImgServlet",urlPatterns = "/SendImg")
public class SendImgServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        String anotherID=request.getParameter("anotherID");
        String FileBytes=request.getParameter("FileBytes");
        String sendTime=request.getParameter("sendTime");
        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<byte[]>(){}.getType();
        byte[] bytes=gson.fromJson(FileBytes,type);
        PrintWriter printWriter=response.getWriter();
        try {
            Chat.insertChatMessage(userID,anotherID,bytes,Timestamp.valueOf(sendTime));
            Chat.updateContactStatus(userID,anotherID);
            printWriter.print("success");
        } catch (SQLException e) {
            e.printStackTrace();
            printWriter.print("error");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

    }
}
