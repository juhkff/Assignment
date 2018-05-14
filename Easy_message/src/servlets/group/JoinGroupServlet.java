package servlets.group;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.group.SimpleGroup;
import test.Client.LoginClient;
import tools.Group;
import wrapper.StaticVariable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Scanner;
import static wrapper.StaticVariable.*;

@WebServlet(name = "JoinGroupServlet",urlPatterns = "/JoinGroup")
public class JoinGroupServlet extends HttpServlet {
    public static void main(String[] args){
        /*byte[] bytes=null;
        InputStream inputStream=new ByteArrayInputStream(bytes);
        Scanner scanner=new Scanner(System.in);
        scanner.next();*/

        //InputStream inputStream=new ByteArrayInputStream(null);         /** 在此构造方法里不能出现null! **/

        String userID="8133523681";
        String URL_ADDRESS=/*LoginClient.*/StaticVariable.URL_ADDRESS;
        String groupID="950876";
        String userName="juhkgf";
        String userHeadIconTrans=null;

        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<byte[]>(){}.getType();
        byte[] userHeadIcon=gson.fromJson(userHeadIconTrans,type);

        model.group.Group group=Group.getGroup(groupID,URL_ADDRESS);

        /**将用户添加到群表中**/
        Group.joinGroup(userID,userName,userHeadIcon,groupID);
        /**将群添加到用户表中**/
        Group.addGroup(userID,group);

//        PrintWriter printWriter=response.getWriter();
        System.out.println("success");
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String URL_ADDRESS=request.getParameter("URL_ADDRESS");
        String userID=request.getParameter("userID");
        String groupID=request.getParameter("groupID");
        String userName=request.getParameter("userName");
        String userHeadIconTrans=request.getParameter("userHeadIconTrans");
        Gson gson=new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type=new TypeToken<byte[]>(){}.getType();
        byte[] userHeadIcon=gson.fromJson(userHeadIconTrans,type);

        model.group.Group group=Group.getGroup(groupID,URL_ADDRESS);

        /**将用户添加到群表中**/
        Group.joinGroup(userID,userName,userHeadIcon,groupID);
        /**将群添加到用户表中**/
        Group.addGroup(userID,group);
        /**获得新添加的群的SimpleGroup对象**/
        SimpleGroup simpleGroup=Group.getNewGroup(groupID);
        String simpleGroup_Trans=new Gson().toJson(simpleGroup);

        PrintWriter printWriter=response.getWriter();
        /**返回给客户端的是新的群的SimpleGroup对象**/
        printWriter.print(simpleGroup_Trans);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
