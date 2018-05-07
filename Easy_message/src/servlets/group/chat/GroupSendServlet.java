package servlets.group.chat;

import tools.Chat;
import tools.DateTime;
import tools.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "GroupSendServlet",urlPatterns = "/GroupSend")
public class GroupSendServlet extends HttpServlet {
    public static void main(String[] args){
        String groupID="950876";
        String Content="Test2";
        String sendTime= String.valueOf(new DateTime().getCurrentDateTime());
        Group.updateGroupMessage(groupID,Content,sendTime);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter printWriter=response.getWriter();

        String groupID=request.getParameter("groupID");
        String senderID=request.getParameter("senderID");
        String senderName=request.getParameter("senderName");
        String sendTime=request.getParameter("sendTime");
        String Status=request.getParameter("Status");
        String Content=request.getParameter("Content");

        /**转换编码**/
        Content=Chat.decodeChinese(Content);

        //插入群表中
        Group.sendGroupChat(groupID,senderID,senderName,sendTime,Status,Content);

        //更新群表中相应记录的最新消息及消息发送时间
        Group.updateGroupMessage(groupID,Content,sendTime);

        printWriter.print("success");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
