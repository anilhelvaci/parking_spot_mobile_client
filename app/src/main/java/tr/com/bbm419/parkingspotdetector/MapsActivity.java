package tr.com.bbm419.parkingspotdetector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.DrawableRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                              GoogleMap.OnCameraMoveStartedListener,
                                                              View.OnClickListener{

    private static final float                       INITIAL_ZOOM      = 9.1f;
    private static final float                       INITIAL_BEARING   = 0f;
    private static final long                        PERIOD_MAP_UPDATE = 500;
    private static final float                       MY_LOCATION_ZOOM  = 15f;

    LatLng home = new LatLng(39.960873, 32.867186);

    private GoogleMap                   mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest             locationRequest;
    private LocationCallback            locationCallback;

    private LatLng lastKnownLocation;
    private Marker driverMarker;
    private Bitmap driverIcon;
    private MarkerOptions markerOptions;

    private float   bearing = 0;
    private boolean isFollow;

    private Runnable mapRunnable;
    private Handler  handler = new Handler();

    private ImageButton myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        myLocation = findViewById(R.id.image_button_location);
        myLocation.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            MapsActivity.this);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(
            new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(android.location.Location location) {
                    if (location != null) {
                        lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
            });

        setMarkerOptions(lastKnownLocation);

        startLocationServices();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(
            R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {

                    lastKnownLocation = new LatLng(locationResult.getLastLocation().getLatitude(),
                                                   locationResult.getLastLocation().getLongitude());
                    if (driverMarker != null) {
                        driverMarker.setPosition(lastKnownLocation);
                    }
                    if (locationResult.getLastLocation().hasBearing()) {
                        bearing = locationResult.getLastLocation().getBearing();
                        driverMarker.setRotation(bearing);
                    }
                }
            }
        };

        mapRunnable = new Runnable() {
            @Override
            public void run() {
                if (isFollow && mMap != null) {

                    if (lastKnownLocation != null) {
                        CameraPosition position = new CameraPosition.Builder().bearing(
                            INITIAL_BEARING)
                            .target(lastKnownLocation)
                            .zoom(MY_LOCATION_ZOOM)
                            .build();

                        mMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(position), 500, null);
                    } else {
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(home, MY_LOCATION_ZOOM));
                    }
                }

                handler.postDelayed(this, PERIOD_MAP_UPDATE);
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        this.mMap = mMap;

        // Add a marker in Sydney and move the camera
        //mMap.addMarker(new MarkerOptions().position(home).title("Home").snippet("This is my home!!!"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnCameraMoveStartedListener(this);
        driverMarker = mMap.addMarker(markerOptions);
        follow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        follow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        handler.removeCallbacks(mapRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMap != null) {
            mMap.setOnCameraMoveStartedListener(null);
            mMap.clear();
        }

        if (mapRunnable != null) {
            mapRunnable = null;
        }
        if (handler != null) {
            handler = null;
        }
        locationCallback = null;
        mMap = null;
    }

    private void startLocationServices() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (i == REASON_GESTURE) {
            unfollow();
        }
    }

    private void follow() {
        myLocation.setImageResource(R.drawable.ic_my_location_found);
        isFollow = true;
        restartMapRunnableImmediate();
    }

    private void unfollow() {
        myLocation.setImageResource(R.drawable.ic_my_location_lost);
        isFollow = false;
        handler.removeCallbacks(mapRunnable);
    }

    private void restartMapRunnableImmediate() {
        handler.removeCallbacks(mapRunnable);
        handler.post(mapRunnable);
    }

    public Bitmap buildIcon(@DrawableRes int drawableId) {
        Drawable normalDrawable = this.getResources().getDrawable(drawableId);
        Drawable drawable = DrawableCompat.wrap(normalDrawable);

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(
            drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void setMarkerOptions(LatLng lastKnownLocation) {
        driverIcon = buildIcon(R.drawable.ic_navigation_24px);
        Drawable drawable = new BitmapDrawable(this.getResources(), driverIcon);
        driverIcon = Bitmap.createBitmap(driverIcon, 0, 0, drawable.getIntrinsicWidth(),
                                         drawable.getIntrinsicHeight());

        markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(driverIcon))
            .anchor(0.5f, 0.5f);

        if (lastKnownLocation == null) {
            markerOptions.position(home);
        } else {
            markerOptions.position(lastKnownLocation);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_button_location:
                follow();
                break;
            default:
        }

    }
}
