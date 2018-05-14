package servlets.online.add;

import com.google.gson.Gson;
import model.group.SimpleGroup;
import tools.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet(name = "SearchGroupServlet",urlPatterns = "/SearchGroup")
public class SearchGroupServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        String searchID=request.getParameter("searchID");

        ArrayList<SimpleGroup> groupList=Group.searchGroupList(userID,searchID);
        Gson gson=new Gson();
        String groupListTrans=gson.toJson(groupList);
        PrintWriter printWriter=response.getWriter();
        printWriter.print(groupListTrans);                                      /** groupListTrans可能为空JSON **/
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
