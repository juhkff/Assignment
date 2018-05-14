package wrapper.test;

import model.file.FileProgress;
import model.message.FileMessage;
import test.Client.Request;
import test.Client.RequestProperty;
import tools.file.model.FileReceiverThread;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

import static wrapper.StaticVariable.URL_ADDRESS;

public class TestFileMessageThread implements Runnable{
    private BlockingQueue<FileMessage> fileMessages;
    private BlockingQueue<FileProgress> fileProgresses;

    public TestFileMessageThread(BlockingQueue<FileMessage> fileMessages,BlockingQueue<FileProgress> fileProgresses) {
        this.fileMessages = fileMessages;
        this.fileProgresses=fileProgresses;
    }

    @Override
    public void run() {
        FileMessage fileMessage = null;
        while (true){
            try {
                fileMessage=fileMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("\n在线发送请求:来自" + fileMessage.getSenderID() + "\t昵称" + fileMessage.getSenderNickName()
                    + "\t文件名" + fileMessage.getFileName() + "\t文件大小" + fileMessage.getFileSize() + "\n");
            System.out.println("\n是否同意接收?(Y/N)");
            Scanner scanner = new Scanner(System.in);
            String ifAgree = scanner.next();  /**同意或拒绝**/

            /**可以在任何时候接收或拒绝**/
            while (!ifAgree.equals("Y") && !ifAgree.equals("N")) {
                System.out.println("输入格式错误...");
                System.out.println("\n是否同意接收?(Y/N)");
                ifAgree = scanner.next();  /**同意或拒绝**/
            }

            if (ifAgree.equals("Y")) {
                //System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀) (PS:路径可以有中文、但上传的文件本身不能含中文! 不能发送文件夹!):");
                System.out.println("\n请输入您的存储路径(目标文件夹全路径):");
                String save_path = scanner.next();
                /*if (!save_path.endsWith("\\"))
                    save_path += ("\\" + fileMessage.getFileName());
                else
                    save_path += fileMessage.getFileName();*/
                if(save_path.endsWith("\\"))
                    save_path=save_path.substring(0,save_path.length()-1);
                FileReceiverThread fileReceiverThread = new FileReceiverThread(save_path,fileProgresses);
                Thread thread=new Thread(fileReceiverThread);
                thread.start();

                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("senderID", fileMessage.getSenderID());
                parameters.put("fileName", fileMessage.getFileName());

//                System.out.println(fileMessage.getFileName());

                Request request = new Request(URL_ADDRESS + "/overrideProcess", parameters, RequestProperty.APPLICATION);
                String result = request.doPost();


            }
        }
    }
}
