package tr.com.bbm419.parkingspotdetector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.android.PolyUtil;
import java.util.ArrayList;
import java.util.List;
import tr.com.bbm419.parkingspotdetector.models.DirectionInfo;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                              GoogleMap.OnCameraMoveStartedListener,
                                                              View.OnClickListener,
                                                              GoogleMap.OnMarkerClickListener,
                                                              MapMvp.MapView {

    private static final String TAG = "[FIRESTORE TAG]";

    private static final float                       INITIAL_ZOOM      = 9.1f;
    private static final float                       INITIAL_BEARING   = 0f;
    private static final long                        PERIOD_MAP_UPDATE = 500;
    private static final float                       MY_LOCATION_ZOOM  = 15f;

    private LatLng home = new LatLng(39.960873, 32.867186);
    private LatLng bahceli = new LatLng(39.931609, 32.824856);
    private LatLng armada = new LatLng(39.915836, 32.803559);

    private GoogleMap                   mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest             locationRequest;
    private LocationCallback            locationCallback;

    private LatLng lastKnownLocation;
    private Marker driverMarker;
    private Bitmap driverIcon;
    private MarkerOptions markerOptions;
    private PolylineOptions polylineOptions;
    private List<Polyline> polylines = new ArrayList<>();
    private Polyline currentPolyline;

    private float   bearing = 0;
    private boolean isFollow;

    private Runnable mapRunnable;
    private Handler  handler = new Handler();

    private ImageButton myLocation;

    private SpotDetectorModel cameraHome;
    private SpotDetectorModel cameraBahceli;
    private SpotDetectorModel cameraArmada;
    private SpotDetectorModel currentDetector;

    private List<SpotDetectorModel> cameraMarkers = new ArrayList<>();

    private CardView cardInfo;
    private TextView name;
    private TextView address;
    private TextView emptySpots;
    private TextView durationDistance;
    private TextView possibility;
    private ImageButton imageButtonClearLayers;

    private MapPresenter mapPresenter;

    private DirectionInfo directionInfo;

    private FirebaseFirestore   db;
    private CollectionReference collectionReference;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("empty_spots");

        mapPresenter = new MapPresenter();
        mapPresenter.setView(this);

        myLocation = findViewById(R.id.image_button_location);
        imageButtonClearLayers = findViewById(R.id.image_button_clear_layers);
        myLocation.setOnClickListener(this);
        imageButtonClearLayers.setOnClickListener(this);

        cardInfo = findViewById(R.id.card_info);
        cardInfo.setVisibility(View.GONE);

        name = findViewById(R.id.camera_name);
        address = findViewById(R.id.camera_address);
        emptySpots = findViewById(R.id.empty_spots);
        durationDistance = findViewById(R.id.duration_distance);
        possibility  =findViewById(R.id.parking_probability);

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

        cameraBahceli = new SpotDetectorModel("Bahcelievler 7. cadde", "7. CADDE", 5, bahceli, "it6vQ8d16R9l0tra4DfD");
        cameraHome = new SpotDetectorModel("Arılık Sokak 4/5", "HOME", 0, home, "lvuoG9WWLNyfGRZeHJkO");
        cameraArmada = new SpotDetectorModel("Dumlupınar Bulvarı", "Armada", 28, armada, "x9f5k95yQRbUDKAPg77x");
        cameraMarkers.add(cameraBahceli);
        cameraMarkers.add(cameraHome);
        cameraMarkers.add(cameraArmada);
    }

    private EventListener<DocumentSnapshot> eventListener = new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                            @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                ? "Local" : "Server";

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String value = getResources().getText(R.string.empty_spots) + " " + documentSnapshot.getData().get("count");
                emptySpots.setText(value);
            } else {
                Log.d(TAG, source + " data: null");
            }

        }
    };

    @Override
    public void onMapReady(GoogleMap mMap) {
        this.mMap = mMap;

        // Add a marker in Sydney and move the camera
        //mMap.addMarker(new MarkerOptions().position(home).title("Home").snippet("This is my home!!!"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnMarkerClickListener(this);
        driverMarker = mMap.addMarker(markerOptions);
        follow();
        setCameraMarkers(cameraMarkers);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        if (cardInfo.getVisibility() == View.GONE) {
            follow();
        }
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
        listenerRegistration = null;
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        for (SpotDetectorModel detectorModel : cameraMarkers) {
            if (detectorModel.getCameraMarker().equals(marker)) {
                String origin = lastKnownLocation.latitude + "," + lastKnownLocation.longitude;
                String destination = detectorModel.getCameraLocation().latitude
                    + ","
                    + detectorModel.getCameraLocation().longitude;
                mapPresenter.getDirectionInfo(origin, destination);
                currentDetector = detectorModel;
                clearPolyline();
                imageButtonClearLayers.setVisibility(View.VISIBLE);
            }
        }
        return false;
    }

    private void removeEventListener() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    private void follow() {
        myLocation.setImageResource(R.drawable.ic_my_location_found);
        isFollow = true;
        clearPolyline();
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

    private void setCameraMarkers(List<SpotDetectorModel> detectorModels) {
        for (SpotDetectorModel detectorModel : detectorModels) {
            detectorModel.setCameraMarker(mMap.addMarker(
                new MarkerOptions().position(detectorModel.getCameraLocation())
                    .icon(BitmapDescriptorFactory.fromBitmap(buildIcon(R.drawable.ic_linked_camera_map_icon)))));
        }
    }

    private void displayCardInfo(SpotDetectorModel detectorModel, DirectionInfo directionInfo) {
        cardInfo.setVisibility(View.VISIBLE);
        name = findViewById(R.id.camera_name);
        address = findViewById(R.id.camera_address);
        emptySpots = findViewById(R.id.empty_spots);

        name.setText(detectorModel.getCameraName());
        address.setText(directionInfo.getEndName());
        String value = getResources().getText(R.string.empty_spots) + " " + detectorModel.getEmptySpots();
        emptySpots.setText(value);

        String info = directionInfo.getDurationText() + ",  " + directionInfo.getDistanceText();
        durationDistance.setText(info);
        drawPolyline(directionInfo.getOverviewPolyline());

        listenerRegistration = collectionReference.document(detectorModel.getDocumentId()).addSnapshotListener(eventListener);
    }

    private void drawPolyline(String overviewPolyline) {
        polylineOptions = new PolylineOptions();
        List<LatLng> points = PolyUtil.decode(overviewPolyline);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (LatLng latLng : points) {
            polylineOptions.add(latLng);
            builder.include(latLng);
        }

        polylineOptions.color(R.color.colorPrimaryDark);
        polylineOptions.width(15f);
        polylineOptions.geodesic(true);
        polylines.add(mMap.addPolyline(polylineOptions));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    private void clearPolyline() {
        if (polylines != null && polylines.size() > 0) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines.clear();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_button_location:
                follow();
                cardInfo.setVisibility(View.GONE);
                imageButtonClearLayers.setVisibility(View.GONE);
                removeEventListener();
                break;
            case R.id.image_button_clear_layers:
                clearPolyline();
                cardInfo.setVisibility(View.GONE);
                imageButtonClearLayers.setVisibility(View.GONE);
                removeEventListener();
                break;
            default:
        }

    }

    @Override
    public void updateDirectionInfo(DirectionInfo directionInfo) {
        displayCardInfo(currentDetector, directionInfo);
    }

}
