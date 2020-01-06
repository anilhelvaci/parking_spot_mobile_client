package tr.com.bbm419.parkingspotdetector.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Routes {
    @SerializedName("legs")
    @Expose
    private List<Legs> legs;

    @SerializedName("overview_polyline")
    @Expose
    private OverviewPolyline overviewPolyline;

    public List<Legs> getLegs() {
        return legs;
    }

    public void setLegs(List<Legs> legs) {
        this.legs = legs;
    }

    public OverviewPolyline getOverviewPolyline() {
        return overviewPolyline;
    }

    public void setOverviewPolyline(OverviewPolyline overviewPolyline) {
        this.overviewPolyline = overviewPolyline;
    }
}
