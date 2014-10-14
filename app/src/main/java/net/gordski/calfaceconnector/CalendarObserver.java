package net.gordski.calfaceconnector;

import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

/**
 * Created by gordski on 14/09/2014.
 */
public class CalendarObserver extends ContentObserver
{
    public CalendarObserver(Handler handler) {
        super(handler);
    }

    public boolean deliverSelfNotifications()
    {
        return true;
    }

    public void onChange(boolean selfChange)
    {
        super.onChange(selfChange);
        Log.i("", "Calendar changes");
    }
}
