package servlets.group;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tools.DateTime;
import tools.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

@WebServlet(name = "CreateGroupServlet",urlPatterns = "/CreateNewGroup")
public class CreateGroupServlet extends HttpServlet {
    public static void main(String[] args){
        /*Boolean a=Boolean.parseBoolean(null);
        System.out.println(a);*/

        String creatorID="8133523681";
        String groupName="forTest";
        byte[] headIcon=null;
        Timestamp timestamp=new DateTime().getCurrentDateTime();
        String creatorName="juhkff";

        try {
            String newGroupID=Group.createNewGroup(groupName,creatorID,creatorName,headIcon,timestamp);                   /**headIcon可能为null类型**/
            System.out.println(newGroupID);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error!CreateGroupServlet in Group.createNewGroup");
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String groupName=request.getParameter("groupName");
        String creatorID=request.getParameter("creatorID");
        String headIcon_Trans=request.getParameter("headIcon");
        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<byte[]>(){}.getType();
        byte[] headIcon=gson.fromJson(headIcon_Trans,type);
        String createTime=request.getParameter("createTime");
        String creatorName=request.getParameter("creatorName");
        Timestamp timestamp = Timestamp.valueOf(createTime);

        PrintWriter printWriter=response.getWriter();
        try {
            String newGroupID=Group.createNewGroup(groupName,creatorID,creatorName,headIcon,timestamp);                   /**headIcon可能为null类型**/
            printWriter.print(newGroupID);
        } catch (SQLException e) {
            e.printStackTrace();
            printWriter.print("error!CreateGroupServlet in Group.createNewGroup");
        }
        //Group.createNewGroup();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
