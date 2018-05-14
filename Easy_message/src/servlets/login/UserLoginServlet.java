package servlets.login;

import com.google.gson.Gson;
import model.property.User;
import tools.Login;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "UserLoginServlet",urlPatterns = "/UserLogin")
public class UserLoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
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
                Login.changeStatus(userID);                                 //修改好友看自己的状态
                Login.changeStatusInGroup(userID);                          //修改群中自己的状态

                User user=null;
                try {
                    user = Login.getUserInfo(userID);
                } catch (Exception e) {
                    System.out.println("获得用户数据失败!UserLoginServlet");
                    e.printStackTrace();
                }
                Gson gson = new Gson();
                if (user != null) {
                    user.setPassWord(passWord);                             //加上密码
                    pw.print(gson.toJson(user));
                } else {
                    pw.print("none");
                }
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
