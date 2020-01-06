package tr.com.bbm419.parkingspotdetector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tr.com.bbm419.parkingspotdetector.models.DirectionInfo;
import tr.com.bbm419.parkingspotdetector.models.DirectionResponse;
import tr.com.bbm419.parkingspotdetector.models.Legs;
import tr.com.bbm419.parkingspotdetector.models.OverviewPolyline;

public class MapPresenter implements MapMvp.MapPresenter {

    private MapRepository mapRepository;
    private MapMvp.MapView mapView;

    private DirectionInfo directionInfo;

    public MapPresenter() {
        mapRepository = MapRepository.getInstance();
    }

    public void getDirectionInfo(String origin, String destination) {
        mapRepository.getDirectionInfo(origin, destination).enqueue(new Callback<DirectionResponse>() {
            @Override
            public void onResponse(Call<DirectionResponse> call,
                                   Response<DirectionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getRouteInfo().size() > 0) {
                    Legs leg = response.body().getRouteInfo().get(0).getLegs().get(0);
                    OverviewPolyline overviewPolyline = response.body().getRouteInfo().get(0).getOverviewPolyline();

                    directionInfo = new DirectionInfo(leg.getStartAddress(),
                                                      leg.getEndAddress(),
                                                      leg.getStartLocation(),
                                                      leg.getEndLocation(),
                                                      overviewPolyline.getPoints(),
                                                      leg.getDistance().getText(),
                                                      leg.getDistance().getValue(),
                                                      leg.getDuration().getText(),
                                                      leg.getDuration().getValue());
                    mapView.updateDirectionInfo(directionInfo);
                }
            }

            @Override
            public void onFailure(Call<DirectionResponse> call,
                                  Throwable t) {
                t.fillInStackTrace();
            }
        });
    }

    @Override
    public void setView(MapMvp.MapView mapView) {
        this.mapView = mapView;
    }
}
