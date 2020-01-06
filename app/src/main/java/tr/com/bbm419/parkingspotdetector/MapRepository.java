package tr.com.bbm419.parkingspotdetector;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tr.com.bbm419.parkingspotdetector.models.DirectionResponse;

public class MapRepository {
   private DirectionService directionService;
   private static final String DIRECTIONS_API_KEY = "AIzaSyBWuZVnnI3qNNfUOqf2bWUQ5aVJjWL_kGQ";
   private String   BASE_URL = "https://maps.googleapis.com/maps/api/";

   private static MapRepository mapRepository;

    private MapRepository() {

        Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

        directionService = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(new OkHttpClient())
            .build().create(DirectionService.class);
    }

    public static MapRepository getInstance(){
        if (mapRepository == null) {
            mapRepository = new MapRepository();
        }
        return mapRepository;
    }

    Call<DirectionResponse> getDirectionInfo(String origin, String destination) {
        return directionService.requestDirection(origin, destination, DIRECTIONS_API_KEY);
    }
}
