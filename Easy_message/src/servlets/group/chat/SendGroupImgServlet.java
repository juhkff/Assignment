package servlets.group.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tools.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;

@WebServlet(name = "SendGroupImgServlet",urlPatterns = "/SendGroupImg")
public class SendGroupImgServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String senderID=request.getParameter("senderID");
        String senderName=request.getParameter("senderName");
        String sendTime=request.getParameter("sendTime");
        String groupID=request.getParameter("groupID");
        String ImgBytes=request.getParameter("ImgBytes");
        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<byte[]>(){}.getType();

        byte[] Img=gson.fromJson(ImgBytes,type);

        Group.insertImg(groupID,senderID,senderName,sendTime,Img);

        PrintWriter printWriter=response.getWriter();
        printWriter.print("success");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
