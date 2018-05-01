package servlets.register;

import tools.Register;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

public class RegisterServlet extends javax.servlet.http.HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        System.out.println("尝试注册新用户...");
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String nickName = request.getParameter("nickName");
        String passWord = request.getParameter("passWord");
        String phoneNum = request.getParameter("phone_num");
        // String Male=request.getParameter("isMale");
       /* boolean isMale;
        if(Male.equals("true"))
            isMale=true;
        else
            isMale=false;
       */
        //boolean isMale = Boolean.parseBoolean(request.getParameter("isMale"));
        // String biryhday = request.getParameter("birthday");

        String result = null;
        try {
            //result = Register.createNewID(nickName, passWord, isMale, biryhday,email);
            result = Register.createNewID(nickName, passWord, phoneNum);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter = response.getWriter();
        if (result != null && Integer.parseInt(result.split(":")[0]) == 1) {
            //printWriter.print("注册成功！请记好您的帐号："+result.split(":")[1]);
            String userID = result.split(":")[1];
            //printWriter.print(result.split(":")[1]);
            printWriter.print(userID);
            try {
                Register.createUserTable(userID);
            } catch (SQLException e) {
                System.out.println("注册时建表异常！");
                e.printStackTrace();
            }
            //System.out.println("注册成功！帐号："+result.split(":")[1]);
            System.out.println("注册成功！帐号:" + userID);

        }
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
