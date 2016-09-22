package com.example.deepak.mapsproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Maps2Activity extends FragmentActivity implements OnMapReadyCallback {
    int RESPONSE_CODE = 10;
    private GoogleMap mMap;
    Double latitude = 0.0, longitude = 0.0;
    ArrayList<LatLng> latLngs;
    String v = "satellite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        latLngs = new ArrayList<>();

        if(savedInstanceState == null){
            Bundle bundle = getIntent().getExtras();
            if(bundle.getBoolean("stopClicked")) {
                latLngs = bundle.getParcelableArrayList("listLatLng");
            }
            else{
                latitude = bundle.getDouble("latitude");
                longitude = bundle.getDouble("longitude");
                latLngs = bundle.getParcelableArrayList("listLatLng");
            }
        }
    }

    //Searching a location provided by AutoCompleteTextView
    public void searchLocation(List<Address> addresses){
        Address address = addresses.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        String addressText = String.format("%s, %s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "", address.getCountryName());

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(latLng);
        markerOptions.title(addressText);

        mMap.clear();
        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //locationTv.setText("Latitude:" + address.getLatitude() + ", Longitude:" + address.getLongitude());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //PolylineOptions polylineOptions = new PolylineOptions();
        //polylineOptions.addAll(latLngs);
        //polylineOptions.add(new LatLng(12.928411, 77.671790));
        //polylineOptions.width(5).color(Color.CYAN).geodesic(true);
        //mMap.addPolyline(polylineOptions);

        //OnMapClickListener
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getApplicationContext(), latLng.latitude+""+latLng.longitude, Toast.LENGTH_SHORT).show();
            }
        });

        //Satellite/Terrain map switch Button
        final Button button = (Button) findViewById(R.id.changeViewBtn);
        button.setText(Constants.satellite);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(v.equals("terrain")){
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    v = "satellite";
                    button.setText(Constants.satellite);
                }else{
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    v = "terrain";
                    button.setText(Constants.terrain);
                }
            }
        });

        //AutoCompleteTextView
        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.searchTxt);
        autoCompleteTextView.setAdapter(new GooglePlaceAutoCompleteAdapter(this, R.layout.row_search_view));
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String s = adapterView.getItemAtPosition(i).toString();
                String search = autoCompleteTextView.getText().toString();
                if(!search.equals("")){
                    Geocoder geocoder = new Geocoder(getBaseContext());
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocationName(search, 3);
                        if(addresses!=null)
                            searchLocation(addresses);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        LatLng me;
        if(latitude!=0.0) {
            me = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
            mMap.addMarker(new MarkerOptions().position(me).title("You're here"));
            if(latLngs.size()>0)
            {

                for(int i=0; i<latLngs.size()-1; i++) {
                    mMap.addMarker(new MarkerOptions().position(latLngs.get(i)).title("Place Visited").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.7f));
                }
            }
        }
        else{
            me = new LatLng(latLngs.get(latLngs.size()-1).latitude, latLngs.get(latLngs.size()-1).longitude);
            if(latLngs.size()>0)
            {
                for(int i=0; i<latLngs.size(); i++) {
                    mMap.addMarker(new MarkerOptions().position(latLngs.get(i)).title("Place Visited").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.7f));
                }
            }
        }
        String url = getMapsApiDirectionsUrl(latLngs); //URL for getting transit route
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);//Downloading route data from url
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(me)             // Sets the center of the map to Mountain View
                .zoom(15)               // Sets the zoom
                .bearing(0)             // Sets the orientation of the camera to east
                .tilt(30)               // Sets the tilt of the camera to 30 degrees
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        else {
            Toast.makeText(getApplicationContext(), "Click ok to grant permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RESPONSE_CODE);
        }
    }

    private String getMapsApiDirectionsUrl(ArrayList<LatLng> latLngs) {
        int i=0, n = latLngs.size()-1;
        LatLng latLng1 = latLngs.get(i);
        LatLng latLng2 = latLngs.get(n);
        String waypoints = "origin="+latLng1.latitude + "," +latLng1.longitude + "&destination=" + latLng2.latitude + "," + latLng2.longitude;
        if(latLngs.size()>2) {
            waypoints += "&waypoints=optimize:true|";
            for (i = 1; i < n-1; i++) {
                LatLng latLng = latLngs.get(i);
                waypoints += latLng.latitude;
                waypoints += ",";
                waypoints += latLng.longitude;
                waypoints += "|";
            }
            LatLng latLng = latLngs.get(n-1);
            waypoints += latLng.latitude;
            waypoints += ",";
            waypoints += latLng.longitude;
        }

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + params + "&key=AIzaSyBD0tNTn0bAzINoAeRti5vwONY9fyar5yE";
    }

    class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(strings[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);  //Parsing the result
        }
    }

    class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            super.onPostExecute(routes);
            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(10);
                polyLineOptions.color(Color.BLUE);
            }

            mMap.addPolyline(polyLineOptions);
        }
    }

}