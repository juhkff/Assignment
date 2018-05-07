package servlets.group;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.group.Group;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "ChangeGroupInfoServlet",urlPatterns = "/ChangeGroupInfo")
public class ChangeGroupInfoServlet extends HttpServlet {
    public static void main(String[] args){
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String groupTrans=request.getParameter("groupTrans");

        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<Group>(){}.getType();

        /**获得更新后的群对象**/
        Group group=gson.fromJson(groupTrans,type);
        tools.Group.changeGroupInfo(group);

        /**获得群成员查询结果集**/
        ResultSet resultSet= tools.Group.getMemberIDList(group.getGroupID());
        String userID;

        /**对每个成员都修改其表中的isupdate值**/
        try {
            while (resultSet.next()){
                userID=resultSet.getString("userID");
                tools.Group.updateGroupInfo(userID,group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error in ChangeGroupInfoServlet when resultSet.next() !");
        }

        PrintWriter printWriter=response.getWriter();
        printWriter.print("success");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
