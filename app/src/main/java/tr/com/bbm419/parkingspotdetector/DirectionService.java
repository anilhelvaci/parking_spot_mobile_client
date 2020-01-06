package tr.com.bbm419.parkingspotdetector;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import tr.com.bbm419.parkingspotdetector.models.DirectionResponse;

public interface DirectionService {
    @GET("directions/json")
    Call<DirectionResponse> requestDirection(@Query("origin") String origin,
                                             @Query("destination") String destination,
                                             @Query("key") String key);
}
