package com.csmarosi.wifiautoff;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WifiAutOffGui extends Activity {
    private LinearLayout apDisplayLayout;
    private DataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DataBase(this);

        // GUI: layout for preferences
        final LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(mainLayout);

        uiAppEnabledButton(mainLayout);
        uiUpdateProtectedWifiListButton(mainLayout);
        uiApDisplayLayout(mainLayout);

        updateApDisplayLayout();
    }

    private CheckBox uiCreateCheckBox(String t, boolean checked,
            LinearLayout p, int color) {
        CheckBox cb = new CheckBox(this);
        cb.setText(t);
        cb.setChecked(checked);
        if (color != 0)
            cb.setTextColor(color);
        p.addView(cb);
        return cb;
    }

    private void uiAppEnabledButton(final LinearLayout mainLayout) {
        final CheckBox cb = uiCreateCheckBox("Enable app", db.isAppEnabled(),
                mainLayout, 0);
        WifiAutOffService.triggerWakeupAlarm(this);
        cb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                db.setAppEnabled(cb.isChecked());
                WifiAutOffService.triggerWakeupAlarm(WifiAutOffGui.this);
            }
        });
    }

    private void uiAddSsidCheckBox(final String ssid, boolean isChecked,
            int color) {
        final CheckBox cb = uiCreateCheckBox(ssid, isChecked, apDisplayLayout,
                color);
        cb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cb.isChecked())
                    db.addSsid(ssid);
                else
                    db.removeKey(ssid);
            }
        });
    }

    private void uiAddBssidCheckBox(final String bssid, final String ssid,
            boolean isChecked, int color) {
        final CheckBox cb = uiCreateCheckBox(bssid + ", SSID:" + ssid,
                isChecked, apDisplayLayout, color);
        cb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cb.isChecked())
                    db.addBssid(bssid, ssid);
                else
                    db.removeKey(bssid);
            }
        });
    }

    private void updateApDisplayLayout() {
        apDisplayLayout.removeAllViews();

        Set<String> ssid = db.getSsidSet();
        for (String s : ssid)
            uiAddSsidCheckBox(s, true, Color.RED);
        Map<String, String> bssid = db.getBssidMap();
        for (Map.Entry<String, String> e : bssid.entrySet())
            uiAddBssidCheckBox(e.getKey(), e.getValue(), true, Color.RED);

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            return;

        // TODO: should be clever and not list already listed AP
        WifiInfo ci = wifiManager.getConnectionInfo();
        uiAddSsidCheckBox(ci.getSSID().replace("\"", ""), false, Color.GREEN);
        uiAddBssidCheckBox(ci.getBSSID(), ci.getSSID().replace("\"", ""),
                false, Color.GREEN);

        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult sr : scanResults) {
            uiAddSsidCheckBox(sr.SSID, false, 0);
            uiAddBssidCheckBox(sr.BSSID, sr.SSID, false, 0);
        }
    }

    private void uiUpdateProtectedWifiListButton(final LinearLayout mainLayout) {
        final Button startButton = new Button(this);
        startButton.setText("Get whitelisted and avaliable WIFIs");
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                updateApDisplayLayout();
            }
        });
        mainLayout.addView(startButton);
    }

    private void uiApDisplayLayout(final LinearLayout mainLayout) {
        final ScrollView sv = new ScrollView(this);
        mainLayout.addView(sv);
        apDisplayLayout = new LinearLayout(this);
        apDisplayLayout.setOrientation(LinearLayout.VERTICAL);
        sv.addView(apDisplayLayout);
    }
}
