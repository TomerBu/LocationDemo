package tomerbu.edu.locationdemo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private static final int REQUEST_LOCATION = 0;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest mRequest;
    private Geocoder mCoder;


    private PendingIntent mGeofencePendingIntent;

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeoFenceTransistionIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }


    ArrayList<Geofence> mGeofenceList = new ArrayList<>();


    void addGeoFence(Entry entry) {

        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(entry.getKey())

                .setCircularRegion(
                        entry.getValue().latitude,
                        entry.getValue().longitude,
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        addGeoFence(new Entry("Home", new LatLng(31.2454786,34.7889343)));
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this, this, this);
        builder.addApi(LocationServices.API);

        client = builder.build();
        client.connect();
        System.out.println("Connecting");


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                System.out.println(latLng);
                infereAddress(latLng);
            }
        });
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                client,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private void requestLocationUpdates() {
        System.out.println("requestLocationUpdates ");

        mRequest = new LocationRequest();
        mRequest.setInterval(100).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//.setNumUpdates(100);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("No permissions ");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        System.out.println("Has permissions ");
        LocationServices.FusedLocationApi.requestLocationUpdates(client, mRequest, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        mMap.addMarker(new MarkerOptions().position(from(location)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).snippet("Snipper").draggable(false).flat(true).title("Title").alpha(0.5f));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(from(location), 14));
        System.out.println("onLocationChanged ");
        infereAddress(location);
        infereAddress("באר שבע");
    }

    private void infereAddress(Location location) {
        if (mCoder == null) {
            mCoder = new Geocoder(this, Locale.getDefault());
        }
        try {
            List<Address> addresses = mCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            for (Address address : addresses) {
                System.out.println(address.getCountryName());
                int max = address.getMaxAddressLineIndex();
                for (int i = 0; i < max; i++) {
                    String line = address.getAddressLine(i);
                    System.out.println(line);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void infereAddress(LatLng latLng) {
        if (mCoder == null) {
            mCoder = new Geocoder(this, Locale.getDefault());
        }
        try {
            List<Address> addresses = mCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            for (Address address : addresses) {
                System.out.println(address.getCountryName());
                int max = address.getMaxAddressLineIndex();
                for (int i = 0; i < max; i++) {
                    String line = address.getAddressLine(i);
                    System.out.println(line);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void infereAddress(String name) {
        try {
            List<Address> addresses = mCoder.getFromLocationName(name, 1);
            for (Address address : addresses) {
                System.out.println(address.getCountryName());
                System.out.println(address.getLatitude());
                System.out.println(address.getLongitude());
                int max = address.getMaxAddressLineIndex();
                for (int i = 0; i < max; i++) {
                    String line = address.getAddressLine(i);
                    System.out.println(line);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    LatLng from(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onResult(@NonNull Status status) {
        System.out.println("Status" + status);
    }
}
