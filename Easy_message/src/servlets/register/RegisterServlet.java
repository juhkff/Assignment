package servlets.register;

import tools.Register;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

public class RegisterServlet extends javax.servlet.http.HttpServlet {
    public static void main(String[] args){
        String nickName="juhgff";
        String passWord="aqko251068";

        String email="363257597@qq.com";
        String result = null;
        try {
          //  result= Register.createNewID(nickName,passWord,isMale,birthday,email);
            result= Register.createNewID(nickName,passWord,email);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(result!=null&&Integer.parseInt(result.split(":")[0])==1)
            System.out.println("注册成功！请记好您的帐号："+result.split(":")[1]);
        else
            System.out.println("注册失败，请排查原因...");
        String content="{\"apikey\":\"02034fa17e5b0ba3a5c51f653e60cb6d\",\"mobile\":\""+"17860536820"+"\",\"content\":\"%d1%e9%d6%a4%c2%eb%a3%ba%a3%ac%bd%f6%d3%c3%d3%da%d5%ca%ba%c5%d7%a2%b2%e1\",\"svrtype\":\"\"}";
        System.out.println(content);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        System.out.println("尝试注册新用户...");
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String nickName = request.getParameter("nickName");
        String passWord = request.getParameter("passWord");
        String phoneNum=request.getParameter("phone_num");
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
            result= Register.createNewID(nickName,passWord,phoneNum);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter=response.getWriter();
        if (result != null&&Integer.parseInt(result.split(":")[0])==1) {
            //printWriter.print("注册成功！请记好您的帐号："+result.split(":")[1]);
            String userID=result.split(":")[1];
            //printWriter.print(result.split(":")[1]);
            printWriter.print(userID);
            try {
                Register.createUserTable(userID);
            } catch (SQLException e) {
                System.out.println("注册时建表异常！");
                e.printStackTrace();
            }
            //System.out.println("注册成功！帐号："+result.split(":")[1]);
            System.out.println("注册成功！帐号:"+userID);

            /*try {
                int result1=Register.createUserTable(userID);
                if(result1!=1)
                    throw new Exception();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("注册时建表异常！");
                e.printStackTrace();
            }*/
        }
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
