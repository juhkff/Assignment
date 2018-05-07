package servlets.group;

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
import java.util.Map;

@WebServlet(name = "GroupListServlet",urlPatterns = "/GroupList")
public class GroupListServlet extends HttpServlet {
    public static void main(String[] args){
        String userID="8133523681";
        //获取群列表(不含最新消息及其发送时间)
        Map<String, SimpleGroup> simpleGroupMap=Group.getGroupList(userID);

        //获取群列表(包含最新消息及其发送时间)
        simpleGroupMap=Group.getFullGroupList(userID,simpleGroupMap);

        Gson gson=new Gson();
        if(/*simpleGroupMap!=null&&!simpleGroupMap.equals("{}")*/simpleGroupMap.size()!=0) {
            String simpleGroupMap_Trans = gson.toJson(simpleGroupMap);
            System.out.println(simpleGroupMap_Trans);
        }else {
            System.out.println("none");
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        PrintWriter printWriter=response.getWriter();

        //获取群列表(不含最新消息及其发送时间)
        Map<String, SimpleGroup> simpleGroupMap=Group.getGroupList(userID);

        //获取群列表(包含最新消息及其发送时间)
        simpleGroupMap=Group.getFullGroupList(userID,simpleGroupMap);
        Gson gson=new Gson();
        if(/*simpleGroupMap!=null*/simpleGroupMap.size()!=0) {
            String simpleGroupMap_Trans = gson.toJson(simpleGroupMap);
            printWriter.print(simpleGroupMap_Trans);
        }else {
            printWriter.print("none");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
