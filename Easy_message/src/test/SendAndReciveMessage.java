package test;

import java.io.IOException;
import java.net.*;

public class SendAndReciveMessage {
    public static void main(String[] args){
        final int SERVER_PORT=1111;
        final String SERVER_IP="123.207.13.112";
        //final String SERVER_IP="localhost";

        String senderID="juhkff";
        String receiverID="juhkgf";
        SocketAddress socketAddress=new InetSocketAddress(SERVER_IP,SERVER_PORT);
        DatagramSocket ds = null;
        try {
            ds=new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] buff=senderID.getBytes();
        byte[] buffer=new byte[16];
        DatagramPacket dp=new DatagramPacket(buff,0,buff.length,socketAddress);
        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
