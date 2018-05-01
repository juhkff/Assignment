package tools.file.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;

public class FileSender {
    private final String SEND_FILE_PATH;
   /* private String senderAddr;
    private String receiverAddr;*/
    //private SocketAddress senderSocketAddress;
    private SocketAddress receiverSocketAddress;
    private DatagramSocket dsk;
    private File file;
//    private static final String SEND_FILE_PATH="F:\\\\视频工具\\\\录制的视频\\\\1 智慧的痛苦\\\\1.1 课程概述.mp4";


    public FileSender(String SEND_FILE_PATH, String senderAddr, String receiverAddr,DatagramSocket dsk) {
        this.SEND_FILE_PATH = SEND_FILE_PATH;
        this.file=new File(this.SEND_FILE_PATH);
        String senderIp=senderAddr.split(",")[0];
        int senderPort= Integer.parseInt(senderAddr.split(",")[1]);
        //this.senderSocketAddress=new InetSocketAddress(senderIp,senderPort);
        this.receiverSocketAddress=new InetSocketAddress(receiverAddr.split(",")[0], Integer.parseInt(receiverAddr.split(",")[1]));
        this.dsk=dsk;
        /*this.senderAddr = senderAddr;
        this.receiverAddr = receiverAddr;*/
    }

    public String send() {
        System.out.println("start...");
        System.out.println("AllSize:"+this.file.length()+" Bytes");
        long startTime=System.currentTimeMillis();
        byte[] buf=new byte[UDPUtils.BUFFER_SIZE];
        byte[] receiveBuf = new byte[1];

        RandomAccessFile accessFile = null;
        DatagramPacket dpk = null;
        //DatagramSocket dsk = null;
        int readSize = -1;
        try {
            accessFile = new RandomAccessFile(SEND_FILE_PATH,"r");
            dpk = new DatagramPacket(buf, 0,buf.length,receiverSocketAddress);
            //dsk = new DatagramSocket(UDPUtils.PORT, InetAddress.getByName("localhost"));
            long sendCount = 0;
            while((readSize = accessFile.read(buf,0,buf.length)) != -1){
                dpk.setData(buf, 0, readSize);
                dsk.send(dpk);                                                          /**一次发送50KB**/
                // wait server response
                sendCount++;
                if (sendCount%100==0)
                    System.out.println("Current: "+(sendCount*readSize)+" /"+this.file.length()+" ("+((sendCount*readSize*100)/this.file.length())+"%)");
                {
                    while(true){
                        dpk.setData(receiveBuf, 0, receiveBuf.length);
                        dsk.receive(dpk);

                        // confirm server receive
                        if(!UDPUtils.isEqualsByteArray(UDPUtils.successData,receiveBuf,dpk.getLength())){
                            System.out.println("resend ...");
                            dpk.setData(buf, 0, readSize);
                            dsk.send(dpk);
                        }else
                            break;
                    }
                }

                //System.out.println("send count of "+(++sendCount)+"!");
            }
            // send exit wait server response
            while(true){
                System.out.println("client send exit message ....");
                dpk.setData(UDPUtils.exitData,0,UDPUtils.exitData.length);
                dsk.send(dpk);

                dpk.setData(receiveBuf,0,receiveBuf.length);
                dsk.receive(dpk);
                // byte[] receiveData = dpk.getData();
                if(!UDPUtils.isEqualsByteArray(UDPUtils.exitData, receiveBuf, dpk.getLength())){
                    System.out.println("client Resend exit message ....");
                    dsk.send(dpk);
                }else
                    break;
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                if(accessFile != null)
                    accessFile.close();
                /*if(dsk != null)
                    dsk.close();*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        String costTime="time:"+(endTime - startTime);
        //System.out.println("time:"+(endTime - startTime));
        return costTime;
    }
}
