package tools.file.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.*;

public class FileReceiver {
    private final String SAVE_FILE_PATH;
    private SocketAddress senderSocketAddress;
    //private SocketAddress receiverSocketAddress;
    private DatagramSocket dsk;
    private long fileSize;
    public FileReceiver(long file_Size, String SAVE_FILE_PATH, String senderAddr, String receiverAddr, DatagramSocket dsk) {
        this.SAVE_FILE_PATH = SAVE_FILE_PATH;
        this.fileSize= file_Size;
        this.senderSocketAddress=new InetSocketAddress(senderAddr.split(",")[0], Integer.parseInt(senderAddr.split(",")[1]));
        //this.receiverSocketAddress = ;
        this.dsk = dsk;
    }

    public void receive() {
        byte[] buf=new byte[UDPUtils.BUFFER_SIZE];
        System.out.println("AllSize:"+this.fileSize+" Bytes");
        DatagramPacket dpk=null;

        BufferedOutputStream bos=null;

        try {
            String[] parts=SAVE_FILE_PATH.split("\\\\");
            String path="";
            for(int i=0;i<parts.length-1;i++){
                path+=parts[i]+"\\";
            }
            File pathFile=new File(path);
            if(!pathFile.exists()) {                      //创建上级目录
                pathFile.mkdirs();
                System.out.println("创建上级目录: "+path);
            }
            File file = new File(SAVE_FILE_PATH);
            if (!file.exists()) {
                file.createNewFile();               //创建文件
                System.out.println("创建文件: "+SAVE_FILE_PATH);
            }
            dpk=new DatagramPacket(buf, buf.length,senderSocketAddress);
            //dsk=new DatagramSocket(UDPUtils.PORT+1,InetAddress.getByName("localhost"));
            bos=new BufferedOutputStream(new FileOutputStream(SAVE_FILE_PATH));
            System.out.println("wait for receive...");
            dsk.receive(dpk);

            int readSize=0;
            long readCount=0;
            int flushSize=0;
            while((readSize=dpk.getLength())!=0) {
                //validate client send exit flag
                if(UDPUtils.isEqualsByteArray(UDPUtils.exitData, buf, readSize)) {
                    System.out.println("server exit");
                    //send exit flag
                    dpk.setData(UDPUtils.exitData,0,UDPUtils.exitData.length);
                    dsk.send(dpk);
                    break;
                }

                bos.write(buf,0,readSize);
                if(++flushSize%1000 == 0) {
                    flushSize=0;
                    bos.flush();
                }

                dpk.setData(UDPUtils.successData, 0, UDPUtils.successData.length);
                dsk.send(dpk);

                dpk.setData(buf, 0, buf.length);
                ++readCount;
                //System.out.println("receive count of"+(++readCount)+"!");
                if (readCount%100==0)
                    System.out.println("Current: "+(readCount*readSize)+" /"+this.fileSize+" ("+((readCount*readSize*100)/this.fileSize)+"%)");
                dsk.receive(dpk);

            }

            //last flush
            bos.flush();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }finally {
            try {
                if(bos!=null)
                    bos.close();
                /*if(dsk!=null)
                    dsk.close(); */
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
    }
}
