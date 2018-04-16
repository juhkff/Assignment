package servlets.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import connection.Conn;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ListenerServlet", urlPatterns = "/Listener")
public class ListenerServlet extends HttpServlet {
    public static void main(String[] args) {
        String userID = "4965757872";
        Connection connection = Conn.getConnection();
        String sql1 = "SELECT ID,status FROM user_" + userID + "_contactlist WHERE isupdate=1";
        String sql2 = "UPDATE user_" + userID + "_contactlist SET isupdate=0 WHERE isupdate=1";
        Map<String, String> userLista = new HashMap<String, String>();
        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            ResultSet resultSet = preparedStatement1.executeQuery();
            while (resultSet.next()) {
                userLista.put(resultSet.getString("ID"), resultSet.getString("status"));
            }
            int i = preparedStatement2.executeUpdate();
            if (i > 1 || i < 0)
                throw new SQLException("好友状态获取失败!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(userLista);
    }

    private String userID;
    private Map<String, String> userList;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        userID = request.getParameter("userID");

        Connection connection = Conn.getConnection();
        String sql1 = "SELECT ID,status FROM user_" + userID + "_contactlist WHERE isupdate=1";
        String sql2 = "UPDATE user_" + userID + "_contactlist SET isupdate=0 WHERE isupdate=1";
        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            userList = new HashMap<String, String>();
            ResultSet resultSet = preparedStatement1.executeQuery();
            while (resultSet.next()) {
                userList.put(resultSet.getString("ID"), resultSet.getString("status"));
            }
            int i = preparedStatement2.executeUpdate();
            if (i > 1 || i < 0)
                throw new SQLException("好友状态获取失败!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();                                    //生成Gson对象
        PrintWriter pw = response.getWriter();
        pw.print(gson.toJson(userList));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
