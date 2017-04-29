package com.csmarosi.wifiautoff;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WifiAutOffService extends IntentService {
    private final static String TAG = "WifiAutOffService";
    private final DataBase db;

    public static void acquireStaticLock(Context context) {
        getLock(context).acquire();
    }

    public WifiAutOffService() {
        super("WifiAutOffService");
        db = new DataBase(this);
    }

    public static void triggerWakeupAlarm(Context c) {
        Intent wakeIntent = new Intent(c, WifiAutOffServiceAlarmReceiver.class);
        c.sendBroadcast(wakeIntent);
    }

    @Override
    final protected void onHandleIntent(Intent intent) {
        try {
            turnOffWifiIfNeeded(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            getLock(this).release();
        }
    }

    private static PowerManager.WakeLock lockStatic = null;

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        final String LOCK_NAME = "com.csmarosi.wifiautoff.WifiAutOffService.LOCK";

        if (lockStatic == null) {
            PowerManager mgr = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    LOCK_NAME);
            lockStatic.setReferenceCounted(true);
        }
        return (lockStatic);
    }

    private void handleNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (db.isAppEnabled()) {
            Intent appIntent = new Intent(this, WifiAutOffGui.class);
            PendingIntent in = PendingIntent.getActivity(this, 0, appIntent, 0);
            Notification n = new Notification.Builder(this)
                    .setContentTitle("WifiAutOff").setContentText("Running...")
                    .setOngoing(true).setContentIntent(in)
                    .setSmallIcon(R.drawable.wifi).build();

            nm.notify(1, n);
        } else {
            nm.cancel(1);
        }
    }

    private static String getHumanTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                .format(new Date());
    }

    synchronized private void turnOffWifiIfNeeded(Intent intent) {
        handleNotification();

        if (!db.isAppEnabled()) {
            Log.i(TAG, getHumanTime() + " turnOffWifiIfNeeded: App disabled");
            return;
        }
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Log.i(TAG, getHumanTime() + " turnOffWifiIfNeeded: WIFI disabled");
            return;
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();
        Set<String> ssidSet = db.getSsidSet();
        Set<String> bssidSet = db.getBssidSet();
        for (ScanResult sr : scanResults) {
            if (ssidSet.contains(sr.SSID) || bssidSet.contains(sr.BSSID)) {
                Log.i(TAG, getHumanTime()
                        + " turnOffWifiIfNeeded: WIFI AP whitelisted");
                return;
            }
        }

        wifiManager.setWifiEnabled(false);
        Log.i(TAG, getHumanTime() + " turnOffWifiIfNeeded: WIFI was turned off");
    }

}
