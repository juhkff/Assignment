package servlets.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import connection.Conn;
import model.Contact;
import tools.Chat;
import tools.Login;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ContactServlet", urlPatterns = "/ContactList")
public class ContactServlet extends HttpServlet {

    ArrayList<Contact> contactList;
    Map<String, Contact> contacts = new HashMap<String, Contact>();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String userID = request.getParameter("userID");
        try {
            contactList = Login.getContactList(userID);
        } catch (SQLException e) {
            System.out.println("获取联系人列表失败!servlets.login.ContactServlet");
            e.printStackTrace();
        }
        if (contactList != null) {
            Connection connection = Conn.getConnection();
            String sql = "SELECT * FROM user_" + userID + "_chatdata WHERE sendTime IN ( SELECT MAX(sendTime) FROM user_" + userID + "_chatdata GROUP BY anotherID ) GROUP BY anotherID ";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String anotherID = resultSet.getString("anotherID");
                    if (anotherID == null)
                        break;
                    String message = Chat.decodeChinese(resultSet.getString("message"));
                    String theLatestTextTime = String.valueOf(resultSet.getTimestamp("sendTime"));
                    for (Contact contact : contactList) {
                        if (contact.getID().equals(anotherID)) {
                            contact.setTheLatestText(message);
                            contact.setTheLatestTextTime(theLatestTextTime);
                            break;
                        }
                    }

                }
                for (Contact contact : contactList) {
                    contacts.put(contact.getID(), contact);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();                    //创建Gson对象
            PrintWriter pw = response.getWriter();
            pw.print(gson.toJson(contacts));                                                         //向前端发送Gson对象
        } else {
            PrintWriter printWriter = response.getWriter();
            printWriter.print("none");                                                                  //发送none
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
