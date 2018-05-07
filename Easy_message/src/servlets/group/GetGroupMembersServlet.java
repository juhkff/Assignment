package servlets.group;

import com.google.gson.Gson;
import model.group.GroupMember;
import tools.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(name = "GetGroupMembersServlet", urlPatterns = "/GetGroupMembers")
public class GetGroupMembersServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String groupID = request.getParameter("groupID");

        Map<String, GroupMember> memberList = Group.getGroupMemberList(groupID);

        Gson gson=new Gson();
        String memberList_Trans=gson.toJson(memberList);

        PrintWriter printWriter=response.getWriter();
        printWriter.print(memberList_Trans);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
