package tr.com.bbm419.parkingspotdetector;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class SpotDetectorModel {
    private String address;
    private String cameraName;
    private int emptySpots;
    private LatLng cameraLocation;
    private Marker cameraMarker;

    public SpotDetectorModel(String address,
                             String cameraName,
                             int emptySpots,
                             LatLng cameraLocation) {
        this.address = address;
        this.cameraName = cameraName;
        this.emptySpots = emptySpots;
        this.cameraLocation = cameraLocation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public int getEmptySpots() {
        return emptySpots;
    }

    public void setEmptySpots(int emptySpots) {
        this.emptySpots = emptySpots;
    }

    public LatLng getCameraLocation() {
        return cameraLocation;
    }

    public void setCameraLocation(LatLng cameraLocation) {
        this.cameraLocation = cameraLocation;
    }

    public Marker getCameraMarker() {
        return cameraMarker;
    }

    public void setCameraMarker(Marker cameraMarker) {
        this.cameraMarker = cameraMarker;
    }
}
