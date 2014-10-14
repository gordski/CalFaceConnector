package net.gordski.calfaceconnector;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.util.Log;

/**
 * Created by gordski on 14/09/2014.
 */
public class CalFaceConnector extends Service
{
    Handler             m_main_handler;
    CalendarObserver    m_observer;
    Cursor              m_cal_db_cursor;

    @Override
    public void onCreate()
    {
        super.onCreate();

        m_main_handler = new Handler();
        m_observer = new CalendarObserver(m_main_handler);

        String[] projection = new String[] { CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND };
        Uri events = Uri.parse("content://com.android.calendar/events");

        m_cal_db_cursor = getContentResolver().query(events, projection, null, null, null);
    }



    @Override
    public void onDestroy()
    {
        if(m_cal_db_cursor != null) m_cal_db_cursor.unregisterContentObserver(m_observer);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(m_cal_db_cursor != null) m_cal_db_cursor.registerContentObserver(m_observer);

        while(m_cal_db_cursor.moveToNext())
        {
            Log.i("Calendar", m_cal_db_cursor.getString(0));
        }
        return START_STICKY;
    }




}
