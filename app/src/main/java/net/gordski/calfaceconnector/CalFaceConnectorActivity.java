package net.gordski.calfaceconnector;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class CalFaceConnectorActivity extends Activity
{

    static final UUID PEBBLE_APP_UUID = UUID.fromString("5837b1f3-ae4c-4fbd-bafd-d90985cb0dc2");

    protected android.content.BroadcastReceiver pebble_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal_face_connector);

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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(pebble_handler != null) unregisterReceiver(pebble_handler);
    }

    protected void sendEvents() 
    {

        String[] projection = new String[] { CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.END };

        long today = Time.getJulianDay(System.currentTimeMillis(), 0);
        Uri events = Uri.parse(CalendarContract.Instances.CONTENT_BY_DAY_URI + "/" + today + "/" + (today + 1));

        Cursor q= getContentResolver().query(events, projection, null, null, null);

        TextView t= (TextView)findViewById(R.id.textView2);
        t.setText("Events:\n");

        PebbleDictionary event = new PebbleDictionary();

        Time now = new Time();
        now.setToNow();

        while(q.moveToNext())
        {
            Time start = new Time();
            start.set(q.getLong(1));
            Time end   = new Time();
            end.set(q.getLong(2));

            if(Time.compare(end, now) >= 0)
            {
                event.addString(0, start.format("%H:%M") + " - " + end.format("%H:%M"));
                event.addString(1, q.getString(0));
            }
        }

        PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, event);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cal_face_connector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
