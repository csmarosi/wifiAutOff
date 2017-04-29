User notes
---

Accidentally enabled WIFI eats battery. It also facilitates tracking.
This app helps to reduce this by turning off WIFI.

By default the app only listen to WIFI changes.
You need to enable the app (checkbox) to activate the turn off WIFI feature.
If want to connect to a new (not whitelisted) network, you need to disable the
app, scan or connect to whitelist the SSID/BSSID, then enable the app again.
(Otherwise it would just instantly turn off the WIFI.)

The app only cares whether the SSID/BSSID is among the WIFI scan results, so
connection errors does not turn off the WIFI.

The whitelist, currently connected networks, available networks are
color coded (red, green, and gray respectively).

A notification is shown if the app is enabled.


Architecture overview
---

It subscribes to `android.net.wifi.SCAN_RESULTS`, etc receivers,
and checks the WIFI connection and WIFI scan result.
It never uses another data source (i.e., cell or GPS), and never starts scanning.
The only setting change is when it turns the WIFI off.

The build process and repo structure is opinionated:
I want to keep the source files and resulting apk minimal. See Makefile


Permissions used
---

* `CHANGE_WIFI_STATE` and `ACCESS_WIFI_STATE` should be obvious
* `ACCESS_FINE_LOCATION` WIFI scan result usually pinpoints your location, see
[in the api as well](https://developer.android.com/reference/android/net/wifi/WifiManager.html#getScanResults())
* `WAKE_LOCK` not sure if needed, but should not hurt; the service holds it


License
---

Code is under MIT license.
Icon is adapted from [this](https://www.iconfinder.com/icons/352130/off_signal_wifi_icon#size=32)
