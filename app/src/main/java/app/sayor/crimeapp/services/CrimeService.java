package app.sayor.crimeapp.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import app.sayor.crimeapp.models.Crime;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// service created for using retrofit
public class CrimeService {

    private static final String WEB_SERVICE_BASE_URL = "https://api1.chicagopolice.org";
    private static CrimeAPI mWebService;

    public static CrimeAPI getClient() {
        if (mWebService == null) {

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss zzz")
                    .create();

            final OkHttpClient lOkhttpClient = new OkHttpClient.Builder()
                    .connectTimeout(50, TimeUnit.SECONDS)
                    .readTimeout(50,TimeUnit.SECONDS)
                    .build();

            Retrofit client = new Retrofit.Builder()
                    .baseUrl(WEB_SERVICE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(lOkhttpClient)
                    .build();

            mWebService = client.create(CrimeAPI.class);
        }
        return mWebService ;
    }

    // REST Methods
    public interface CrimeAPI {
        @GET("/clearpath/api/1.0/crimes/major")
        Call<List<Crime>> getMajorCrimes(
                @QueryMap Map<String, String> options
        );

    }
}
