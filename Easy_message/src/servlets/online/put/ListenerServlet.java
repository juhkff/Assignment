package servlets.online.put;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import connection.Conn;
import model.contact.Contact;
import tools.Chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ListenerServlet", urlPatterns = "/Listener")
public class ListenerServlet extends HttpServlet {

    private String userID;
    private ArrayList<Contact> contactList;
    private Map<String, Contact> contacts;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        userID = request.getParameter("userID");

        String ID;
        String nickName;
        InputStream inputStream;
        byte[] headIcon;
        byte types;
        boolean status;

        Connection connection = Conn.getConnection();
        String sql1 = "SELECT * FROM user_" + userID + "_contactlist WHERE isupdate=1 AND types=0";
        String sql2 = "UPDATE user_" + userID + "_contactlist SET isupdate=0 WHERE isupdate=1 AND types=0";
        try {
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            contactList = new ArrayList<Contact>();
            contacts = new HashMap<String, Contact>();
            ResultSet resultSet = preparedStatement1.executeQuery();
            while (resultSet.next()) {
                ID = resultSet.getString("ID");
                nickName = resultSet.getString("nickName");
                inputStream = resultSet.getBinaryStream("headIcon");
                headIcon = null;
                if (inputStream != null) {
                    headIcon = new byte[inputStream.available()];
                    inputStream.read(headIcon,0,inputStream.available());
                    inputStream.close();
                }
                types = resultSet.getByte("types");
                status = resultSet.getBoolean("status");
                Contact contact = new Contact(ID, nickName, headIcon, types, status);
                contactList.add(contact);
            }
            if (contactList != null) {
                /*String sql = "SELECT *,MAX(sendTime) FROM user_" + userID + "_chatdata WHERE anotherID IN ( SELECT ID FROM user_"
                        +userID+"_contactlist ) GROUP BY anotherID";*/
                String sql = "SELECT * FROM user_" + userID + "_chatdata WHERE sendTime IN ( SELECT MAX(sendTime) FROM user_"
                        + userID + "_chatdata GROUP BY anotherID ) AND anotherID IN (SELECT ID FROM user_" + userID + "_contactlist ) GROUP BY anotherID ";
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = connection.prepareStatement(sql);
                    resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {
                        String anotherID = resultSet.getString("anotherID");
                        if (anotherID == null)
                            break;
                        String message = null;
                        InputStream inputStream1=null;
                        message=resultSet.getString("message");
                        if(message!=null)
                            message=Chat.decodeChinese(message);
                        else if(message==null) {
                            inputStream1 = resultSet.getBinaryStream("img");
                            if (inputStream1 != null) {
                                byte[] bytes1 = new byte[inputStream1.available()];
                                inputStream1.read(bytes1);
                            }
                        }
                        String theLatestTextTime = String.valueOf(resultSet.getTimestamp("sendTime"));
                        for (Contact contact : contactList) {
                            if (contact.getID().equals(anotherID)) {
                                if(message!=null)
                                    contact.setTheLatestText(message);
                                else if (inputStream1!=null)
                                    contact.setTheLatestText("<图片>");
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
            preparedStatement2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Conn.Close();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
