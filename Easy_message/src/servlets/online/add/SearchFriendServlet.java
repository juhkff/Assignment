package servlets.online.add;

import com.google.gson.Gson;
import model.contact.Contact;
import tools.Online;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "SearchFriendServlet",urlPatterns = "/SearchFriend")
public class SearchFriendServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        String searchID=request.getParameter("searchID");

        Map<String, Contact> userList = new HashMap<>();
        try {
            userList = Online.searchFriendList(userID,searchID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String result = "";
        Gson gson = new Gson();
        if (userList.size() > 0) {
            result = gson.toJson(userList);
        }else {
            userList=new HashMap<>();
            result=gson.toJson(userList);
        }
        PrintWriter pw = response.getWriter();
        pw.print(result);
        //result的可能值:"" ; Map序列的Json形式
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
