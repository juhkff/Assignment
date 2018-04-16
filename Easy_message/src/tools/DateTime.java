package tools;


import java.sql.Timestamp;
import java.util.Date;

public class DateTime {
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
