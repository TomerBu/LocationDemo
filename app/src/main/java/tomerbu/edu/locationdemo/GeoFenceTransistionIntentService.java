package tomerbu.edu.locationdemo;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class GeoFenceTransistionIntentService extends IntentService {

    private static final String TAG = "TomerBu";

    public GeoFenceTransistionIntentService() {
        super("sugar");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {

            Log.e(TAG, geofencingEvent.getErrorCode() + "Error code");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();


            // Send notification and log the transition details.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle("Tomer").setContentText(triggeringGeofences.get(0).toString()).setSmallIcon(R.mipmap.ic_launcher);

            NotificationManagerCompat.from(this).notify(0, builder.build());

            Log.i(TAG, triggeringGeofences.get(0).toString());
        } else {
            // Log the error.
            Log.e(TAG, "geofence_transition_invalid_type");
        }

    }


}


class Entry {
    String key;
    LatLng value;

    public Entry(String key, LatLng value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LatLng getValue() {
        return value;
    }

    public void setValue(LatLng value) {
        this.value = value;
    }
}