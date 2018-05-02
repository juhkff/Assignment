package servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet(name = "GetImgTestServlet", urlPatterns = "/getImg")
public class GetImgTestServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("servlet开始!");
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        //response.setHeader("Content-Type","image/jpg");
        //String userID=request.getParameter("userID");
//        PrintWriter printWriter=response.getWriter();
//        printWriter.print("aas");
        File file = new File("C:\\927_2015091021942371.jpg");

        InputStream inputStream = new FileInputStream(file);
        byte[] inby = new byte[inputStream.available()];
        inputStream.read(inby,0,inby.length);
        inputStream.close();

        InputStream inputStream1=new ByteArrayInputStream(inby);
        byte[] inby1=new byte[inputStream1.available()];
        inputStream1.read(inby1, 0, inputStream1.available());

        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
        try {
            outputStream.write(inby1, 0, inby1.length);
            inputStream1.close();
        } finally {
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
