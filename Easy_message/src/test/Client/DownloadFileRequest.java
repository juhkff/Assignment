package test.Client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFileRequest {
    private String senderID=null;
    private String receiverID=null;
    private File file=null;
    private String localPath=null;
    private String remotePath=null;
    private FileOutputStream fileOutputStream;
    private HttpURLConnection httpURLConnection;
    private InputStream inputStream = null;
    private String fileName=null;
    private String groupID=null;
    public DownloadFileRequest(String senderID, String receiverID, String localPath, String fileName) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.localPath = localPath;
        this.remotePath = "content/" + this.senderID + "_To_" + this.receiverID + "/" + fileName;
        this.file = new File(this.localPath);
        if (!this.file.exists())
            this.file.mkdirs();
        this.fileName = fileName;
    }

    public DownloadFileRequest(String groupID,String localPath,String fileName){
        this.groupID=groupID;
        this.localPath=localPath;
        this.remotePath="content/"+this.groupID+"/"+fileName;
        this.file=new File(this.localPath);
        if(!this.file.exists())
            this.file.mkdirs();
        this.fileName=fileName;
    }

    public final String downLoad() throws IOException {
        try {
            URL url = new URL("http://123.207.13.112:8080/Easy_message/" + this.remotePath);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            //以Post方式提交表单，默认get方式
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            // post方式不能使用缓存
            httpURLConnection.setUseCaches(false);
            //连接指定的资源
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            //判断文件的保存路径后面是否以/结尾
            if (!localPath.endsWith("/"))
                localPath += "/";
            //写入到文件(注意文件保存路径的后面一定要加上文件的名称)
            fileOutputStream = new FileOutputStream(localPath + fileName);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            //System.out.println("确认接收准备过程正常");
            byte[] bytes = new byte[4096];
            int length = bufferedInputStream.read(bytes);
            //保存文件
            //System.out.println("确认接收开始正常");
            while (length != -1) {
                bufferedOutputStream.write(bytes, 0, length);
                //System.out.println("确认接收过程正常");
                length = bufferedInputStream.read(bytes);
            }
            bufferedOutputStream.close();
            bufferedInputStream.close();
            httpURLConnection.disconnect();
            return "successSaved";
        } catch (Exception e) {
            System.out.println("下载失败...");
            e.printStackTrace();
            return "false";
        }
    }
}
