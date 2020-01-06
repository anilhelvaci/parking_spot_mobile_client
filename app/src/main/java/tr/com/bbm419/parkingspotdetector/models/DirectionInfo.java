package tr.com.bbm419.parkingspotdetector.models;

import com.google.android.gms.maps.model.LatLng;

public class DirectionInfo {
    private String startName;
    private String endName;
    private LatLng startLocation;
    private LatLng endLocation;
    private String overviewPolyline;
    private String distanceText;
    private int distanceInt;
    private String durationText;
    private int durationInt;

    public DirectionInfo(String startName,
                         String endName,
                         LatLng startLocation,
                         LatLng endLocation,
                         String overviewPolyline,
                         String distanceText,
                         int distanceInt,
                         String durationText,
                         int durationInt) {
        this.startName = startName;
        this.endName = endName;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.overviewPolyline = overviewPolyline;
        this.distanceText = distanceText;
        this.distanceInt = distanceInt;
        this.durationText = durationText;
        this.durationInt = durationInt;
    }

    public String getStartName() {
        return startName;
    }

    public String getEndName() {
        return endName;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public String getOverviewPolyline() {
        return overviewPolyline;
    }

    public void setStartName(String startName) {
        this.startName = startName;
    }

    public void setEndName(String endName) {
        this.endName = endName;
    }

    public void setOverviewPolyline(String overviewPolyline) {
        this.overviewPolyline = overviewPolyline;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

    public int getDistanceInt() {
        return distanceInt;
    }

    public void setDistanceInt(int distanceInt) {
        this.distanceInt = distanceInt;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public int getDurationInt() {
        return durationInt;
    }

    public void setDurationInt(int durationInt) {
        this.durationInt = durationInt;
    }
}
