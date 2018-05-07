package servlets.File;

import model.contact.Contact;
import tools.file.File;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

@WebServlet(name = "ReadFileResponseServlet",urlPatterns = "/ReadFileResponse")
public class ReadFileResponseServlet extends HttpServlet {
    private String userID;
    private String anotherID;
    private String fileName;
    private ArrayList<Contact> contactList;
    private Map<String, Contact> contacts;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        userID = request.getParameter("userID");
        anotherID=request.getParameter("anotherID");
        fileName=request.getParameter("fileName");
        String isAccepted="";
        try {
            isAccepted=File.checkRequest(userID,anotherID,fileName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("method error! ( ERROR IN ReadFileResponseServlet ) ");
        }
        PrintWriter printWriter=response.getWriter();
        printWriter.print(isAccepted);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
