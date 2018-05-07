package webTest;

import connection.Conn;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "TestImgServlet",urlPatterns = "/getImg")
public class TestImgServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("servlet开始!");
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String thenum=request.getParameter("num");
        System.out.println("thenum:"+thenum);
        int num= Integer.parseInt(thenum);

        Connection connection= Conn.getConnection();
        String sql="SELECT headIcon FROM userinfo WHERE !ISNULL(headIcon)";
        InputStream inputStream = null;
        OutputStream outputStream=null;
        int i=0;
        try {
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                /**从i=1开始有图片**/
                i++;
                if(i==num) {
                    System.out.println("第"+i+"of"+num);
                    inputStream = resultSet.getBinaryStream("headIcon");
                    break;
                }
            }

            byte[] bytes=new byte[inputStream.available()];
            inputStream.read(bytes,0,inputStream.available());

            outputStream=new BufferedOutputStream(response.getOutputStream());
            outputStream.write(bytes,0,bytes.length);

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
