package servlets.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tools.Login;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "LoginServlet", urlPatterns = "/Login")

//登录验证密码

public class LoginServlet extends HttpServlet {
    public static void main(String[] args) throws SQLException {
        String userID="6979682227";
        String passWord="aqko251068";
        String password = Login.pswVerification(userID);
        if (password == null)
            System.out.println("null");                                           //（帐号不存在）登录失败
        else if (!password.equals(passWord))
            System.out.println("false");                                          //（密码错误）登录失败
        else if (password.equals(passWord)) {
            System.out.println("true");                                           //登录成功
            Login.changeStatus(userID);                                 //修改好友看自己的状态
        }
        /**
         * pw使用多次print方法会有干扰吗
         * **/
        else
            System.out.println("error");                                          //其它情况
    }

    private Map<String, String> contactList;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        String passWord = request.getParameter("passWord");
        PrintWriter pw = response.getWriter();

        try {
            String password = Login.pswVerification(userID);
            if (password == null)
                pw.print("null");                                           //（帐号不存在）登录失败
            else if (!password.equals(passWord))
                pw.print("false");                                          //（密码错误）登录失败
            else if (password.equals(passWord)) {
                pw.print("true");                                           //登录成功
                Login.changeStatus(userID);                                 //修改好友看自己的状态
                Login.changeStatusInGroup(userID);                          //修改群中自己的状态
            }
            /**
             * pw使用多次print方法会有干扰吗
             * **/
            else
                pw.print("error");                                          //其它情况
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
