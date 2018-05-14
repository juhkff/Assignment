package p2pserver.fileServerDao;

import java.io.*;
import java.net.Socket;

public class TransferThread implements Runnable {
    private Socket socket;
    private InputStream inputStream;
    private String fileDir;
    private String fileName;
    private String senderID;
    private String receiverID;


    public TransferThread(Socket socket,InputStream inputStream) {
        this.socket = socket;
        this.inputStream=inputStream;
    }

    @Override
    public void run() {
        FileOutputStream fileOutputStream = null;
        long fileLength = 0;
        long currentLength = 0;
        try {
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            fileName = dataInputStream.readUTF();
            senderID = dataInputStream.readUTF();
            receiverID = dataInputStream.readUTF();

            fileDir = "C:\\Easy_message\\Upload\\" + senderID + "_To_" + receiverID;
            File file = new File(fileDir);
            if (!file.exists()) {
                file.mkdirs();
            }

            fileLength = dataInputStream.readLong();
            currentLength = 0;

            fileOutputStream = new FileOutputStream(fileDir + File.separator + fileName);
            byte[] bytes = new byte[1024];

            int len = 0;
            double percent;
            while ((len = dataInputStream.read(bytes)) > 0) {
                fileOutputStream.write(bytes, 0, len);
                currentLength += len;
                percent = (double) currentLength / fileLength;
                int currentPer = (int) (percent * 100);

                System.out.println("已上传：" + (currentLength / 1024) + "KB" + "\t上传比例：" + currentPer + "%");
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error in inputStream=socket.getInputStream();");
        } finally {
            if (fileLength != currentLength) {
                //如果文件没传完，则删除传到服务器端的文件
                File file = new File(fileDir + File.separator + fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
}
