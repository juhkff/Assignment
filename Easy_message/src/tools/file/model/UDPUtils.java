package tools.file.model;

public class UDPUtils {
    private UDPUtils() {}

    public static final int BUFFER_SIZE=50*1024;

    public static final int PORT=50000;

    public static final byte[] successData="success data mark".getBytes();

    public static final byte[] exitData="exit data mark".getBytes();

    public static void main(String[] args) {
        byte[] b=new byte[1];
        System.out.println(isEqualsByteArray(successData,b));
    }

    private static boolean isEqualsByteArray(byte[] compareBuf, byte[] buf) {
        // TODO Auto-generated method stub
        if(buf == null || buf.length == 0)
            return false;

        boolean flag=true;
        if(buf.length == compareBuf.length) {
            for(int i=0;i<buf.length;i++) {
                if(buf[i]!=compareBuf[i]) {
                    flag=false;
                    break;
                }
            }
        }else
            return false;
        return flag;
    }

    public static boolean isEqualsByteArray(byte[] compareBuf,byte[] buf,int len) {
        if(buf == null || buf.length == 0 || buf.length < len || compareBuf.length<len)
            return false;
        boolean flag=true;

        int innerMinLen=Math.min(compareBuf.length,len);

        for(int i = 0;i<innerMinLen;i++) {
            if(buf[i]!=compareBuf[i]) {
                flag=false;
                break;
            }
        }
        return flag;
    }
}
