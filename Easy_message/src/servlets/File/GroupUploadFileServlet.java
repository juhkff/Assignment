package servlets.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import tools.Chat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "GroupUploadFileServlet", urlPatterns = "/GroupUploadFile")
public class GroupUploadFileServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        final long MAX_SIZE = 10 * 1024 * 1024 * 1024;      //设置上传文件最大为 10 G
        //创建一个解析器工厂
        DiskFileItemFactory factory = new DiskFileItemFactory();
        //设置工厂的内存缓冲区大小，默认是10K
        //factory.setSizeThreshold(1024*1024);
        //设置工厂的临时文件目录:当上传文件的大小大于缓冲区大小时，，将使用临时文件目录缓存
        factory.setRepository(new File("C:\\Program Files\\apache-tomcat-9.0.2-windows-x64\\apache-tomcat-9.0.2\\webapps\\Easy_message\\content\\temp_directory\\"));
        //文件上传解析器
        ServletFileUpload upload = new ServletFileUpload(factory);
        //设置所有上传数据的最大值，单位字节long 1M
        upload.setSizeMax(MAX_SIZE);
        //设置单个文件上传的最大值（ 1 G ）
        upload.setFileSizeMax(1024 * 1024 * 1024 * 1);
        //设置编码格式
        upload.setHeaderEncoding("UTF-8");


        try {
            String userID = null;
            String anotherID = null;
            String senderID = null;
            String senderName = null;
            String fileName = null;
            String path;

            //解析请求，将表单中每个输入项封装成一个FileItem对象
            List<FileItem> itemList = upload.parseRequest(request);
            for (FileItem item : itemList) {
                //判断输入的类型是普通输入项 还是文件
                if (item.isFormField()) {
                    //普通输入项，得到input中的name属性的值
                    String name = item.getFieldName();
                    //得到输入项中的值
                    if (name.equals("userID")) {
                        String value = item.getString("UTF-8");
                        System.out.println("name=" + name + " value=" + value);
                        userID = value;
                    } else if (name.equals("anotherID")) {
                        String value = item.getString("UTF-8");
                        System.out.println("name=" + name + " value=" + value);
                        anotherID = value;
                    } else if (name.equals("senderID")) {
                        String value = item.getString("UTF-8");
                        System.out.println("name=" + name + " value=" + value);
                        senderID = value;
                    } else if (name.equals("senderName")) {
                        String value = item.getString("UTF-8");
                        System.out.println("name=" + name + " value=" + value);
                        senderName = value;
                    }

                } else {
                    //上传的是文件，获得文件上传字段中的文件名
                    //注意IE或FireFox中获取的文件名是不一样的，IE中是绝对路径，FireFox中只是文件名
                    fileName = item.getName();
                    //fileName=URLEncoder.encode(fileName,"UTF-8");                                   /**暂时用这种方式凑合一下**/
                    if (anotherID == null) {
                        path = "C:\\Program Files\\apache-tomcat-9.0.2-windows-x64\\apache-tomcat-9.0.2\\webapps\\Easy_message\\content\\" + userID + "\\";
                        if (fileName.contains("\\"))
                            fileName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
                        //String[] tempName=fileName.split("\\\\");
                        System.out.println("上传文件至服务器：" + fileName);

                        /**System.out.println("自动更改文件名称");**/
                        //返回表单标签name属性的值
                        //String namede = item.getFieldName();
                        //System.out.println(namede);
                        File pathFile = new File(path);
                        if (!pathFile.exists())                                                                  //创建上级目录
                            pathFile.mkdirs();
                        File file = new File(path + fileName);
                        System.out.println("文件已存放于：" + file.getAbsolutePath());
                        item.write(file);
                        item.delete();
                    } else {
                        path = "C:\\Program Files\\apache-tomcat-9.0.2-windows-x64\\apache-tomcat-9.0.2\\webapps\\Easy_message\\content\\" + userID + "_To_" + anotherID + "\\";
                        if (fileName.contains("\\"))
                            fileName = fileName.split("\\\\")[fileName.split("\\\\").length - 1];
                        //String[] tempName=fileName.split("\\\\");
                        System.out.println("上传文件至服务器：" + fileName);

                        /**System.out.println("自动更改文件名称");**/
                        //返回表单标签name属性的值
                        //String namede = item.getFieldName();
                        //System.out.println(namede);
                        File pathFile = new File(path);
                        if (!pathFile.exists())                                                                  //创建上级目录
                            pathFile.mkdirs();
                        File file = new File(path + fileName);
                        System.out.println("文件已存放于：" + file.getAbsolutePath());
                        item.write(file);
                        item.delete();
                    }
                }
            }

            tools.file.File.upLoadNewFile(userID, senderID,senderName,fileName);
//            Chat.updateContactStatus(userID, anotherID);

            PrintWriter pw = response.getWriter();
            pw.print("success");
        } catch (FileUploadException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
