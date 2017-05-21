package app.sayor.crimeapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.kml.KmlLayer;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import app.sayor.crimeapp.KMPStringSearch;
import app.sayor.crimeapp.R;
import app.sayor.crimeapp.ValueComparator;
import app.sayor.crimeapp.mapproj.com.jhlabs.map.proj.Projection;
import app.sayor.crimeapp.mapproj.com.jhlabs.map.proj.ProjectionFactory;
import app.sayor.crimeapp.models.Crime;
import app.sayor.crimeapp.models.CrimeList;
import app.sayor.crimeapp.services.CrimeService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrimeMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final double FEET_TO_METER = .3048;
    private static final String ILLINOIS_STATE_PLANE = "nad83:1201";
    // modified color for since the given colors don't have hue value
    private String[] clrArr = new String[]{"red", "green", "blue", "green", "gray",
            "cyan", "magenta", "yellow", "aqua", "lime",
            "black", "black", "black", "black", "black",
            "black", "black", "black", "black", "black",
            "black", "black", "black", "black", "black"};
/*  String[] clrArr = new String[]{"#ff0000", "#e50000", "#cc0000", "#b20000", "#990000",
            "#7f0000", "#660000", "#4c0000", "#330000", "#190000",
            "#000000", "#000000", "#000000", "#000000", "#000000",
            "#000000", "#000000", "#000000", "#000000", "#000000",
            "#000000", "#000000", "#000000", "#000000", "#000000"}; */
    private GoogleMap mMap;
    CrimeService.CrimeAPI crimeService;
    ProgressDialog pd;
    HashMap<String, List<Crime>> responseMap;
    HashMap<String, Integer> countMap;
    HashMap<String, String> colorMap;
    TreeMap<String, Integer> sortedCountMap;
    String[] sortedDistrict;
    String[] arr;
    ValueComparator bvc;
    int lastVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        responseMap = null;
        countMap = null;
        colorMap = null;
        sortedCountMap = null;
        sortedDistrict = null;

        pd = new ProgressDialog(this);
        pd.setTitle("Loading...");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
        pd.show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            KmlLayer layer = new KmlLayer(mMap, getAssets().open("chicago_bound.kml"), getApplicationContext());
            layer.addLayerToMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getIncident();
    }

    private void getIncident() {
        if (crimeService == null)
            crimeService = CrimeService.getClient();

        Calendar theEnd = Calendar.getInstance();
        Calendar theStart = (Calendar) theEnd.clone();
        theStart.add(Calendar.DAY_OF_MONTH, -30);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String start = dateFormat.format(theStart.getTime());
        String end = dateFormat.format(theEnd.getTime());
        Map<String, String> query = new HashMap<>();
        query.put("dateOccurredStart", start);
        query.put("dateOccurredEnd", end);
        query.put("sort", "dateOccurred");
        query.put("max", "100");
        Call<List<Crime>> getCall = crimeService.getMajorCrimes(query);

        getCall.enqueue(new Callback<List<Crime>>() {
            @Override
            public void onResponse(Call<List<Crime>> call, Response<List<Crime>> response) {
                Toast.makeText(getBaseContext(), "response received", Toast.LENGTH_SHORT).show();
                CrimeList appCrimeList = CrimeList.get(getApplicationContext());
                appCrimeList.addAllCrimes(response.body());

                responseMap = new HashMap<>();
                countMap = new HashMap<>();
                colorMap = new HashMap<>();

                // district<->list<crime> name value pair
                for (int i = 0; i < appCrimeList.getCrimes().size(); i++) {
                    if (responseMap.get(appCrimeList.getCrimes(i).getCpdDistrict()) == null)
                        responseMap.put(appCrimeList.getCrimes(i).getCpdDistrict(), new ArrayList<Crime>());
                    responseMap.get(appCrimeList.getCrimes(i).getCpdDistrict()).add(appCrimeList.getCrimes(i));
                }

                if(responseMap!=null)
                    arr = responseMap.keySet().toArray(new String[responseMap.size()]);

                for (int i = 0; i < arr.length; i++) {
                    countMap.put(arr[i], responseMap.get(arr[i]).size());
                }

                bvc = new ValueComparator(countMap);
               sortedCountMap = new TreeMap<>(bvc);
                sortedCountMap.putAll(countMap);

                if(sortedCountMap!=null)
                    sortedDistrict = sortedCountMap.keySet().toArray(new String[sortedCountMap.size()]);

                lastVal = countMap.get(sortedDistrict[0]);

                for (int i = 0, j = 0; i < sortedDistrict.length; i++) {
                    // adding same incident count edge case
                    if (countMap.get(sortedDistrict[i]) != lastVal)
                        j++;
                    colorMap.put(sortedDistrict[i], clrArr[j]);
                    lastVal = countMap.get(sortedDistrict[i]);
                }

                // add colored markers based on district incident count
                if(responseMap!=null && sortedDistrict!=null && colorMap!=null) {
                    for (int i = 0; i < sortedDistrict.length; i++) {
                        List<Crime> testList = responseMap.get(sortedDistrict[i]);
                        String testColor = colorMap.get(sortedDistrict[i]);
                        for (int j = 0; j < testList.size(); j++) {
                            String xPoint = testList.get(j).getxCoordinate();
                            String yPoint = testList.get(j).getyCoordinate();
                            if(xPoint!=null && yPoint!=null) {
                            float xCoOrd = Float.parseFloat(xPoint);
                            float yCoOrd = Float.parseFloat(yPoint);
                            LatLng testLatLong = getLatLng(getBaseContext(), xCoOrd, yCoOrd);
                            mMap.addMarker(new MarkerOptions().position(testLatLong)
                                        .title(testList.get(j).getBlock())
                                        .snippet(testList.get(j).getIucrDescription())
                                        .icon(getMarkerIcon(testColor)));
                            }
                        }
                    }
                }

                // animate and move camera to chicago
                LatLng chicago = new LatLng(41.87, -87.70);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(chicago).zoom(10).build();
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                pd.dismiss();
            }

            @Override public void onFailure(Call<List<Crime>> call, Throwable t) {
                Log.e("error response", t.getMessage());
                Toast.makeText(getBaseContext(), "error response", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

    private static Projection getProjection(Context context, String projection) {
        return ProjectionFactory.getNamedPROJ4CoordinateSystem(context, projection);
    }

    // using port of map4proj library
    public static LatLng getLatLng(Context context, float xCoordinate, float yCoordinate) {
        xCoordinate *= FEET_TO_METER;
        yCoordinate *= FEET_TO_METER;

        PointF statPlanePoint = new PointF(xCoordinate, yCoordinate);
        PointF origin = new PointF(0, 0);
        getProjection(context, ILLINOIS_STATE_PLANE).inverseTransform(statPlanePoint, origin);
        return new LatLng(origin.y+36.660, origin.x+0.180);
    }

    // to convert color to hue
    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        int colour = Color.parseColor(color);
        Color.colorToHSV(colour, hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    // search button onClick
    public void onSearch(View v) {
        Toast.makeText(getBaseContext(), "search clicked", Toast.LENGTH_SHORT).show();
        EditText tvSearch = (EditText) findViewById(R.id.tvSearch);
        String pattern = tvSearch.getText().toString();
        // clearoff all the marker and overlays
        mMap.clear();
        // adding police boundaries again since we cleared all
        try {
            KmlLayer layer = new KmlLayer(mMap, getAssets().open("chicago_bound.kml"), getApplicationContext());
            layer.addLayerToMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // using implementation of Knuth-Morris-Prath alogrithm for string search. Time complexity is O(m+n)
        KMPStringSearch kSearch = new KMPStringSearch();
        if (responseMap != null && sortedDistrict != null && colorMap != null && !pattern.isEmpty()) {
            for (int i = 0; i < sortedDistrict.length; i++) {
                List<Crime> testList = responseMap.get(sortedDistrict[i]);
                String testColor = colorMap.get(sortedDistrict[i]);
                for (int j = 0; j < testList.size(); j++) {
                    String xPoint = testList.get(j).getxCoordinate();
                    String yPoint = testList.get(j).getyCoordinate();
                    if(xPoint!=null && yPoint!=null) {
                        if (kSearch.KMPSearch(pattern, testList.get(j).toString())) {
                            float xCoOrd = Float.parseFloat(xPoint);
                            float yCoOrd = Float.parseFloat(yPoint);
                            LatLng testLatLong = getLatLng(getBaseContext(), xCoOrd, yCoOrd);

                            Marker marker = mMap.addMarker(new MarkerOptions().position(testLatLong)
                                    .title(testList.get(j).getBlock())
                                    .snippet(testList.get(j).getIucrDescription())
                                    .icon(getMarkerIcon(testColor)));
                            dropPinEffect(marker);
                        }
                    }
                }
            }
        }
    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                }
            }
        });
    }

}
