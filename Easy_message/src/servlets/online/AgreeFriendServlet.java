package servlets.online;

import connection.Conn;
import model.Contact;
import tools.Online;

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

@WebServlet(name = "AgreeFriendServlet",urlPatterns = "/AgreeFriend")
public class AgreeFriendServlet extends HttpServlet {
    public static void main(String[] args){
        String userID="8076357234";
        String ID="1005221246";
        String nickName="juhkff";
        int property=Integer.parseInt("0");
        if(property!=0)
            try {
                throw new Exception("邀请分类错误!servlet.AgreeFriendServet");
            } catch (Exception e) {
                e.printStackTrace();
            }
        else{
            try {
                System.out.println("添加好友中...");
                Online.bothAddFriend(userID,ID,nickName);
                System.out.println("好友添加成功!");
                Online.sendAgreeResponse(userID,nickName,ID);
                System.out.println("成功给发送邀请者发送提示!");
            } catch (SQLException e) {
                System.out.println("添加新联系人出错!servlet.AgreeFriendServlet");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request,response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID=request.getParameter("userID");                       //自己的ID
        String ID=request.getParameter("ID");                               //请求者的ID
        String nickName=request.getParameter("nickName");
        int property= Integer.parseInt(request.getParameter("property"));

        if(property!=0)
            try {
                throw new Exception("邀请分类错误!servlet.AgreeFriendServet");
            } catch (Exception e) {
                e.printStackTrace();
            }
        else{
            try {
                System.out.println("添加好友中...");
                Online.bothAddFriend(userID,ID,nickName);
                System.out.println("好友添加成功!");
                Online.sendAgreeResponse(userID,nickName,ID);
                System.out.println("成功给发送邀请者发送提示!");
            } catch (SQLException e) {
                System.out.println("添加新联系人出错!servlet.AgreeFriendServlet");
                e.printStackTrace();
            }
            PrintWriter printWriter=response.getWriter();
            printWriter.print("success");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}