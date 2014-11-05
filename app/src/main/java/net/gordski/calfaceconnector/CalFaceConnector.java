package net.gordski.calfaceconnector;

import android.app.Service;
import android.content.BroadcastReceiver;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CalFaceConnector extends Service
{
    static final UUID PEBBLE_APP_UUID = UUID.fromString("5837b1f3-ae4c-4fbd-bafd-d90985cb0dc2");

    protected android.content.BroadcastReceiver pebble_receiver;
    protected BroadcastReceiver pebble_ack_handler;
    protected List<PebbleDictionary> events = new ArrayList<PebbleDictionary>();

    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.i("CalFaceConnector", "Service - onCreate()");

        pebble_receiver = PebbleKit.registerReceivedDataHandler(this, new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID)
        {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data)
            {
                PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);

                startService(new Intent("SEND", null, context, CalFaceConnector.class));
                Log.i("CalFaceConnector", "Message received.");
            }
        });

        pebble_ack_handler = PebbleKit.registerReceivedAckHandler(this, new PebbleKit.PebbleAckReceiver(PEBBLE_APP_UUID)
        {
            @Override
            public void receiveAck(Context context, int i)
            {
                // Message acked, signal the main app.
                Log.i("CalFaceConnector", "Acked!");
                if(events.size() > 0) sendEvent();
            }
        });

    }

    protected void buildEvents()
    {

        String[] projection = new String[] { CalendarContract.Instances.TITLE, CalendarContract.Instances.ALL_DAY, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END };

        long today = Time.getJulianDay(System.currentTimeMillis(), 0);
        Uri query_uri = Uri.parse(CalendarContract.Instances.CONTENT_BY_DAY_URI + "/" + today + "/" + (today + 1));

        Cursor q= getContentResolver().query(query_uri, projection, null, null, CalendarContract.Instances.BEGIN + " ASC");

        Time now = new Time();
        now.setToNow();

        int i = 0;

        events.clear();

        while(q.moveToNext() && i < 3)
        {
            Time start = new Time();
            start.set(q.getLong(2));
            Time end   = new Time();
            end.set(q.getLong(3));

            if(Time.compare(end, now) >= 0)
            {
                PebbleDictionary event = new PebbleDictionary();

                if (q.getInt(1) == 0)
                {
                    event.addString(2*i, start.format("%H:%M") + " - " + end.format("%H:%M"));
                }
                else
                {
                    event.addString(2*i, "All Day");
                }
                event.addString(2*i+1, q.getString(0));

                events.add(event);
                i++;
            }
        }

        if(events.size() == 0)
        {
            PebbleDictionary event = new PebbleDictionary();
            event.addString(0, "No Events");
            events.add(event);
        }

        sendEvent();
    }

    protected void sendEvent()
    {
        PebbleKit.sendDataToPebble(this, PEBBLE_APP_UUID, events.remove(0));
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.i("CalFaceConnector", "Service - onDestroy()");

        if(pebble_receiver != null) unregisterReceiver(pebble_receiver);
        if(pebble_ack_handler != null) unregisterReceiver(pebble_ack_handler);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent != null)
        {
            if (intent.getAction() != null && intent.getAction().equals("SEND"))
            {
                buildEvents();
            }
        }
        else
        {
            // Start up.
            Log.i("CalFaceConnector", "Service - onStart()");
            Toast.makeText(this, "CalFaceConnector Started", Toast.LENGTH_LONG).show();
        }

        return START_STICKY;
    }




}
