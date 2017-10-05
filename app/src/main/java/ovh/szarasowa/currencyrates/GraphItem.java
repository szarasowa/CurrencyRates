package ovh.szarasowa.currencyrates;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Em on 2017-10-05.
 */

public class GraphItem {
    private String type;
    private Integer price;
    private long date;

    public GraphItem(String type, Integer price, long date) {
        this.type = type;
        this.price = price;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public String getPrice() {
        return String.valueOf(price);
    }

    public String getTime() {
        Date getdate = new Date(date * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+2")); // give a timezone reference for formating (see comment at the bottom
        return sdf.format(getdate);
    }
}
