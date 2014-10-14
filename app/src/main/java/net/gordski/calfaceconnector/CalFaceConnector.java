package net.gordski.calfaceconnector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class CalFaceConnector extends Service
{
    static final UUID PEBBLE_APP_UUID = UUID.fromString("5837b1f3-ae4c-4fbd-bafd-d90985cb0dc2");

    protected android.content.BroadcastReceiver pebble_handler;

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i("CalFaceConnector", "Service - onCreate()");

        pebble_handler = PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID)
        {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data)
            {
                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
                sendEvents();
            }
        });
    }

    protected void sendEvents()
    {

        String[] projection = new String[] { CalendarContract.Instances.TITLE, CalendarContract.Instances.ALL_DAY, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END };

        long today = Time.getJulianDay(System.currentTimeMillis(), 0);
        Uri events = Uri.parse(CalendarContract.Instances.CONTENT_BY_DAY_URI + "/" + today + "/" + (today + 1));

        Cursor q= getContentResolver().query(events, projection, null, null, CalendarContract.Instances.BEGIN + " ASC");

        PebbleDictionary event = new PebbleDictionary();

        Time now = new Time();
        now.setToNow();

        while(q.moveToNext())
        {
            Time start = new Time();
            start.set(q.getLong(2));
            Time end   = new Time();
            end.set(q.getLong(3));

            if(Time.compare(end, now) >= 0)
            {
                if(q.getInt(1) == 0)
                {
                    event.addString(0, start.format("%H:%M") + " - " + end.format("%H:%M"));
                }
                else
                {
                    event.addString(0, "All Day");
                }
                event.addString(1, q.getString(0));

                // Finish with the first matching event.
                break;
            }
        }

        PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, event);

    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.i("CalFaceConnector", "Service - onDestroy()");

        if(pebble_handler != null) unregisterReceiver(pebble_handler);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i("CalFaceConnector", "Service - onStart()");
        Toast.makeText(this, "CalFaceConnector Started", Toast.LENGTH_LONG).show();

        return START_STICKY;
    }




}
