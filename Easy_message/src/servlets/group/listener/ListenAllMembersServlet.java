package servlets.group.listener;

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
import java.util.ArrayList;

@WebServlet(name = "ListenAllMembersServlet",urlPatterns = "/ListenAllMembers")
public class ListenAllMembersServlet extends HttpServlet {
    public static void main(String[] args){
        ArrayList<GroupMember> groupMemberArrayList=null;
        //ArrayList<GroupMember> groupMemberArrayList1=new ArrayList<GroupMember>();
        //groupMemberArrayList=groupMemberArrayList1;
        Gson gson=new Gson();
        if(gson.toJson(groupMemberArrayList)==null){
            System.out.println("this is null");
        }
        System.out.println(gson.toJson(groupMemberArrayList));
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter printWriter=response.getWriter();

        String groupID=request.getParameter("groupID");
        ArrayList<GroupMember> groupMemberArrayList=null;
        groupMemberArrayList=Group.checkUpdatedMember(groupID);
        Group.updateGroupMemberInfo(groupID);

        Gson gson=new Gson();
        String result=gson.toJson(groupMemberArrayList);
        if(result.equals("null")||result.equals("[]")||result.equals("{}")){
            printWriter.print("none");
        }else {
            printWriter.print(result);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
