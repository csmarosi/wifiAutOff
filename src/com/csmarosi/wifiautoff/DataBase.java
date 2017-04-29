package com.csmarosi.wifiautoff;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataBase {
    private static final String PKG_NAME = "com.csmarosi.wifiautoff";
    private static final String IS_APP_ENABLED = "IS_APP_ENABLED";
    private static final long SSID_FILTER = 42L;
    private final Context mContext;

    DataBase(Context c) {
        mContext = c;
    }

    private SharedPreferences getMyPref() {
        return mContext.getSharedPreferences(PKG_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAppEnabled() {
        return getMyPref().getBoolean(IS_APP_ENABLED, false);
    }

    public void setAppEnabled(boolean newState) {
        getMyPref().edit().putBoolean(IS_APP_ENABLED, newState).commit();
    }

    public void addSsid(String ssid) {
        getMyPref().edit().putLong(ssid, SSID_FILTER).commit();
    }

    public void addBssid(String bssid, String ssid) {
        getMyPref().edit().putString(bssid, ssid).commit();
    }

    public Set<String> getSsidSet() {
        Map<String, ?> kvmap = getMyPref().getAll();
        Set<String> result = new HashSet<String>();

        for (Map.Entry<String, ?> entry : kvmap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Long
                    && ((Long) value).longValue() == SSID_FILTER)
                result.add(key);
        }
        return result;
    }

    public Map<String, String> getBssidMap() {
        Map<String, ?> kvmap = getMyPref().getAll();
        Map<String, String> result = new HashMap<String, String>();

        for (Map.Entry<String, ?> entry : kvmap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String)
                result.put(key, (String) value);
        }
        return result;
    }

    public Set<String> getBssidSet() {
        return getBssidMap().keySet();
    }

    public void removeKey(String ssid) {
        getMyPref().edit().remove(ssid).commit();
    }
}
