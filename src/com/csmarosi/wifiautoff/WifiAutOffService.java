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
import java.util.Date;
import java.util.List;
import java.util.Set;

public class WifiAutOffService extends IntentService {
    private final static String TAG = "WifiAutOffService";
    /* On some devices the scan result after turning on WIFI is empty. */
    private static int EMPTY_SCAN_GRACE_COUNT = 1;
    private static long EMPTY_SCAN_GRACE_TIME = 7000; // in milisec

    private final DataBase db;
    private static int remainingGraceCount;
    private static long lastDecisionTime;

    public static void acquireStaticLock(Context context) {
        getLock(context).acquire();
    }

    public WifiAutOffService() {
        super("WifiAutOffService");
        db = new DataBase(this);
        remainingGraceCount = EMPTY_SCAN_GRACE_COUNT;
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

    synchronized private void turnOffWifiIfNeeded(Intent intent) {
        long currentTime = new Date().getTime();
        handleNotification();

        if (!db.isAppEnabled()) {
            remainingGraceCount = EMPTY_SCAN_GRACE_COUNT;
            Log.d(TAG, "turnOffWifiIfNeeded: App disabled");
            return;
        }
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            remainingGraceCount = EMPTY_SCAN_GRACE_COUNT;
            Log.d(TAG, "turnOffWifiIfNeeded: WIFI is already disabled");
            return;
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();
        Set<String> ssidSet = db.getSsidSet();
        Set<String> bssidSet = db.getBssidSet();
        for (ScanResult sr : scanResults) {
            if (ssidSet.contains(sr.SSID) || bssidSet.contains(sr.BSSID)) {
                Log.d(TAG, "turnOffWifiIfNeeded: WIFI AP whitelisted");
                return;
            }
        }

        if ((remainingGraceCount > 0) && (scanResults.size() == 0)) {
            if (currentTime < lastDecisionTime + EMPTY_SCAN_GRACE_TIME) {
                Log.d(TAG, "turnOffWifiIfNeeded: too quick update");
                return;
            }
            lastDecisionTime = currentTime;
            remainingGraceCount--;
            Log.d(TAG, "turnOffWifiIfNeeded: GraceCount>0");
            return;
        }

        wifiManager.setWifiEnabled(false);
        remainingGraceCount = EMPTY_SCAN_GRACE_COUNT;
        Log.i(TAG, "turnOffWifiIfNeeded: WIFI was turned off");
        Log.d(TAG, "scanResults=" + scanResults);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent appIntent = new Intent(this, WifiAutOffGui.class);
        PendingIntent in = PendingIntent.getActivity(this, 0, appIntent, 0);
        Notification n = new Notification.Builder(this)
                .setContentTitle("WIFI was turned off")
                .setContentText("For whitelisting, uncheck 'Enable app'")
                .setOngoing(false).setContentIntent(in)
                .setSmallIcon(R.drawable.wifi).build();
        nm.notify(2, n);
    }

}
