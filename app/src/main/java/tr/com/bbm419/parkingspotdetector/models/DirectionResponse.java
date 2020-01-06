package tr.com.bbm419.parkingspotdetector.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DirectionResponse {
    @SerializedName("routes")
    @Expose
    private List<Routes> routeInfo;

    public List<Routes> getRouteInfo() {
        return routeInfo;
    }

    public void setRouteInfo(List<Routes> routeInfo) {
        this.routeInfo = routeInfo;
    }
}
