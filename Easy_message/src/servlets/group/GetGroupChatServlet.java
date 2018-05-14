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

@WebServlet(name = "GetGroupChatServlet",urlPatterns = "/GetGroupChat")
public class GetGroupChatServlet extends HttpServlet {
    public static void main(String[] args){
        String groupID="950876";
        String exitTime="2018-05-07 01:43:03.0";
        ArrayList<GroupMessage> groupMessages=Group.getChat(groupID,exitTime);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String groupID=request.getParameter("groupID");
        String exitTime=request.getParameter("exitTime");

        ArrayList<GroupMessage> groupMessages=Group.getChat(groupID,exitTime);

        PrintWriter printWriter=response.getWriter();
        printWriter.print(new Gson().toJson(groupMessages));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
