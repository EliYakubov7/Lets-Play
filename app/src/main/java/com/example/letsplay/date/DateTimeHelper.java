package com.example.letsplay.date;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class to return current date and time as strings
 */
public  class DateTimeHelper {
    //return current time as a string
    public static String getTime(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String time = formatter.format(date).substring(11);
        return time;
    }
    //return current date as a string
    public static String getDate(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateToday = formatter.format(date).substring(0,10);
        return dateToday;
    }
}
