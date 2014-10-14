package net.gordski.calfaceconnector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by gordski on 15/09/2014.
 */
public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent calFaceConnectorIntent = new Intent(context, CalFaceConnector.class);
        context.startService(calFaceConnectorIntent);
    }
}
