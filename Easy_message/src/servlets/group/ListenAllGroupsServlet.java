package servlets.group;

import com.google.gson.Gson;
import connection.Conn;
import model.group.SimpleGroup;
import tools.DateTime;
import tools.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ListenAllGroupsServlet",urlPatterns = "/ListenAllGroups")
public class ListenAllGroupsServlet extends HttpServlet {
    public static void main(String[] args) throws SQLException {
        String groupID="950876";
        String content="Test";
        String sendTime= String.valueOf(new DateTime().getCurrentDateTime());
        Connection connection=Conn.getConnection();
        String sql="UPDATE groups SET theLatestText=?,theLatestTextTime=? WHERE groupID=?";
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.setString(1,content);
        preparedStatement.setTimestamp(2, Timestamp.valueOf(sendTime));
        preparedStatement.setString(3,groupID);
        preparedStatement.execute();
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        PrintWriter printWriter=response.getWriter();
        Gson gson=new Gson();
        Map<String,SimpleGroup> simpleGroupMap=null;
        simpleGroupMap=Group.checkUpdatedGroup(userID);
        simpleGroupMap=Group.getFullGroupList(userID,simpleGroupMap);

        if(simpleGroupMap==null)
            printWriter.print("none");
        else
            printWriter.print(gson.toJson(simpleGroupMap));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
