package org.elasticsearch.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    /**
     * 判断今天是不是当月的最后一天
     * @return
     */
    public static boolean endOfMonth() {
        GregorianCalendar calendar = new GregorianCalendar(Locale.CHINA);
        //SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        //System.out.println(format.format(calendar.getTime()));

        //当前月的实际值
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        //当月的最后一天
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DATE);
        return dayOfMonth == lastDayOfMonth;
    }

    public static void main(String[] args) {
        System.out.println (DateUtils.endOfMonth());
    }
}
