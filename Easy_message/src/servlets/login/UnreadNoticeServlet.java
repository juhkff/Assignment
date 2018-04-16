package servlets.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.ChatMessage;
import model.NoticeMessage;
import tools.Login;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

@WebServlet(name = "UnreadNoticeServlet",urlPatterns = "/GetNotice")
public class UnreadNoticeServlet extends HttpServlet {
    public static void main(String[] args) throws SQLException {
        String userID="8076357234";
        Timestamp timestamp = null;
        timestamp=Login.getExitTime(userID);
        ArrayList<NoticeMessage> noticeMessages=null;
        String noticeList = null;
        try {
            noticeMessages=Login.getNoticeMessage(userID,timestamp); //获得所有通知
            Gson gson=new Gson();
            noticeList=gson.toJson(noticeMessages);                          //noticeMessage列表的Json格式
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Login.deleteAllNoticeMessage(userID);
        System.out.println(noticeList);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request,response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String userID=request.getParameter("userID");
        Timestamp timestamp = null;
        String noticeList = null;
        try {
            timestamp=Login.getExitTime(userID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<NoticeMessage> noticeMessages=null;
        try {
            noticeMessages=Login.getNoticeMessage(userID,timestamp); //获得所有通知
            Gson gson=new Gson();
            noticeList=gson.toJson(noticeMessages);                          //noticeMessage列表的Json格式
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*try {
            if(noticeMessages!=null) {
                //System.out.println("deleteAllNoticeMessage数据要被删");
                Login.deleteAllNoticeMessage(userID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        PrintWriter pw=response.getWriter();
        if(!noticeList.equals("[]")&&!noticeList.equals("")&&!noticeList.equals("null")&&noticeList!=null&&!noticeList.equals("")) {                          //Json列表若为空，返回的是字符串的'[]'而非null类型
            pw.print(noticeList);
        }else {
            pw.print("none");                                                           /** pw:列表/none **/
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
