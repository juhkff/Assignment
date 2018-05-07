package tools;


import java.net.DatagramPacket;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTime {

    public static void main(String[] args) {
        /*DateTime dateTime = new DateTime();
        System.out.println("" + dateTime.getCurrentDateTime());*/

        //String birthday=new Scanner(System.in).nextLine();
        String birthday="2017-12-06 23:41:55";
        //birthday=String.format("%tY%n",birthday);
        Timestamp timestamp = null;
        //Pattern pattern=Pattern.compile("\\d\\d\\d\\d\\d[-]\\d\\d[-]\\d\\d\\s\\d\\d[:]\\d\\d[:]\\d\\d");
        //String pattern="\\d\\d\\d\\d-\\d\\d-\\d{2} \\d{2}:\\d{2}:\\d{2}";
        /*String pattern="\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d";
        boolean isMatch=Pattern.matches(pattern,birthday);
        System.out.println(isMatch);*/
        //Matcher matcher=pattern.matcher(birthday);
        //boolean b=matcher.matches();
        if (birthday.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d")) {
            timestamp = Timestamp.valueOf(birthday);
        }
        /*if (matcher.find()){
            timestamp=Timestamp.valueOf(birthday);
        }*/
        //System.out.println(timestamp);
        //DateFormat dateFormat=new SimpleDateFormat("yyyyMMdd hhmmss");
        System.out.println(timestamp);
    }


    private Date date;
    Timestamp timeStamp;

    public DateTime() {
        this.date = new Date();
    }

    public Timestamp getCurrentDateTime() {
        this.timeStamp = new Timestamp(date.getTime());
        return timeStamp;
    }
}
