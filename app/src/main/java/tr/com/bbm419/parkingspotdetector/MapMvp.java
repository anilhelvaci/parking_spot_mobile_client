package tr.com.bbm419.parkingspotdetector;

import tr.com.bbm419.parkingspotdetector.models.DirectionInfo;

public interface MapMvp {

    interface MapView {
        void updateDirectionInfo(DirectionInfo directionInfo);
    }

    interface MapPresenter {
        void setView(MapMvp.MapView mapView);
        void getDirectionInfo(String origin, String destination);
    }

}
