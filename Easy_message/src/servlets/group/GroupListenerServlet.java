package servlets.group;

import com.google.gson.Gson;
import model.group.GroupMessage;
import tools.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "GroupListenerServlet",urlPatterns = "/GroupListener")
public class GroupListenerServlet extends HttpServlet {
    public static void main(String[] args){
        String userID="8133523681";
        String groupID="950876";
        String referredTime="2018-05-07 01:43:03.0";
        ArrayList<GroupMessage> groupMessages=Group.getChat(userID,groupID,referredTime);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        String groupID=request.getParameter("groupID");
        String referredTime=request.getParameter("referredTime");

        ArrayList<GroupMessage> groupMessages=Group.getChat(userID,groupID,referredTime);
        PrintWriter printWriter=response.getWriter();
        Gson gson=new Gson();
        String groupMessages_Trans=gson.toJson(groupMessages);
        if(!groupMessages_Trans.equals("null")&& !groupMessages_Trans.equals("") && groupMessages_Trans != null && !groupMessages_Trans.equals("[]") && !groupMessages_Trans.equals(""))
            printWriter.print(groupMessages_Trans);
        else
            printWriter.print("none");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
