android-background-alerts
=========================

Implementation of AlertActivity that can be brought up front with AlertDialog box
from main app UI thread, background thread, in and out of process background and
foreground services. It is guaranteed to show only one AlertDialog to the user
and queue all requests that a made during AlertDialog being shown for subsequent
messaging for the user. The API in alerts.java is intentionally simplistic but
can be easily extended to much bigger variety of convenience helper methods. 

![alt tag](https://raw.githubusercontent.com/leok7v/android-background-alerts/master/res/drawable-xxhdpi/alert.png)

