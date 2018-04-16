package servlets;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import tools.Upload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "UploadFileServlet",urlPatterns = "/Upload")

//实现将文件上传保存到服务器的指定位置，数据库存储结构暂未确定和实现

public class UploadFileServlet extends HttpServlet {
    public static void main(String[] args){
        Date today=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddEHHmmss");
        String date=sdf.format(today);
        System.out.println(date);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request,response);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        //获得当前用户帐号
        String userID = null;

        //设置上传文件最大为 10 G
        final long GB=1*1024*1024*1024;
        DiskFileItemFactory factory=new DiskFileItemFactory();
        //设置工厂的内存缓冲区大小为5MB（默认是10KB）
        factory.setSizeThreshold(1024*1024*5);
        //当上传文件的大小大于缓冲区大小时，将使用临时文件目录缓存
        factory.setRepository(new File("C:\\Easy_message\\Upload\\temp_directory"));
        ServletFileUpload upload=new ServletFileUpload(factory);
        upload.setSizeMax(10*GB);
        //设置单个文件上传的最大值( 2 G )
        upload.setFileSizeMax(2*GB);

        upload.setHeaderEncoding("UTF-8");
        try {
            List<FileItem> itemList=upload.parseRequest(request);
            for(FileItem item:itemList){
                if(item.isFormField()){
                    //当输入类型为普通输入项而非文件时
                    userID=item.getString("UTF-8");
                    System.out.println(userID);
                }else{
                    //当输入类型为文件时
                    String fileName=item.getName();
                    if(fileName.contains("\\"))
                        fileName=fileName.split("\\\\")[fileName.split("\\\\").length-1];
                    System.out.println("用户 "+userID+" 请求：上传文件："+fileName+"至服务器");

                    //带上传文件的MD5值
                    String theMD5= DigestUtils.md5Hex(item.getInputStream());                               //可能会出错
                    boolean repetitive=false;
                    //获得数据库中存储的所有文件的MD5值
                    Set<String> MD5_List=new HashSet<>(Upload.getMD5List(userID));
                    Iterator<String> itr=MD5_List.iterator();
                    while (itr.hasNext()){
                        if(theMD5.equals(itr.next())){
                            repetitive=true;
                            break;
                        }
                    }

                    PrintWriter pw=response.getWriter();

                    if(repetitive){
                        System.out.println("用户目录中已有相同文件");
                    }
                    else if(!repetitive){
                        //待上传文件与服务器中该用户已上传文件内容不相同时执行下面的操作

                        //给文件重命名
                        //获得当前精确到秒的日期字符串（有"星期几"的字符）
                        Date today=new Date();
                        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddEHHmmss");
                        String date=sdf.format(today);
                        //拼接成新名字
                        //目的是防止出现上传名字相同而内容不同的文件引起冲突
                        //数据库中既保存原文件名也保存新文件名
                        String saveName=fileName+date;
                        System.out.println("自动更改文件名为："+saveName);
                        File file=new File("C:\\Easy_message\\Upload\\"+userID+"\\");
                        if(!file.exists())
                            file.mkdir();
                        File thefile=new File("C:\\Easy_message\\Upload\\"+userID+"\\"+saveName);
                        item.write(thefile);
                        System.out.println("文件已成功存放于："+thefile.getAbsolutePath());
                        item.delete();
                    }
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PrintWriter pw=response.getWriter();
        pw.print("成功？");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
    }
}
