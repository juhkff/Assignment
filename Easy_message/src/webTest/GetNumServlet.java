package webTest;

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

@WebServlet(name = "GetNumServlet",urlPatterns = "/GetNum")
public class GetNumServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("num获得!");
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String nouse=request.getParameter("nouse");

        Connection connection=Conn.getConnection();
        String sql="SELECT headIcon FROM userinfo WHERE !ISNULL(headIcon)";
        int num=0;
        try {
            PreparedStatement preparedStatement=connection.prepareStatement(sql);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                num++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PrintWriter printWriter=response.getWriter();
        printWriter.print(num);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
