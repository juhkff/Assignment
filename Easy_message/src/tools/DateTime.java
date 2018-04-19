package tools;


import java.net.DatagramPacket;
import java.sql.Timestamp;
import java.util.Date;

public class DateTime {
    public static void main(String[] args){
        DateTime dateTime=new DateTime();
        System.out.println(""+dateTime.getCurrentDateTime());
    }
    private Date date;
    Timestamp timeStamp;

    public DateTime() {
        this.date = new Date();
    }

    public Timestamp getCurrentDateTime(){
        this.timeStamp=new Timestamp(date.getTime());
        return timeStamp;
    }
}
