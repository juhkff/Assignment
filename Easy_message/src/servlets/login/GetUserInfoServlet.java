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

@WebServlet(name = "GetUserInfoServlet", urlPatterns = "/GetUserInfo")
public class GetUserInfoServlet extends HttpServlet {
    public static void main(String[] args) throws Exception {
        String userID="8133523681";
        User user=null;
        user = Login.getUserInfo(userID);
        Gson gson = new Gson();
        //PrintWriter pw = response.getWriter();
        if (user != null) {
            System.out.println(gson.toJson(user));
        } else {
            System.out.println("none");
        }
        System.out.println(user.getExitTime());
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        User user = null;
        try {
            user = Login.getUserInfo(userID);
        } catch (Exception e) {
            System.out.println("获得用户数据失败!GetUserInfoServlet");
            e.printStackTrace();
        }
        Gson gson = new Gson();
        PrintWriter pw = response.getWriter();
        if (user != null) {
            pw.print(gson.toJson(user));
        } else {
            pw.print("none");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
