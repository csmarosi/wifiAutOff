package com.csmarosi.wifiautoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WifiAutOffServiceAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WifiAutOffService.acquireStaticLock(context);
        context.startService(new Intent(context, WifiAutOffService.class));
    }
}
