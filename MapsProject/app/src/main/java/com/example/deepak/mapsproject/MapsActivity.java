package com.example.deepak.mapsproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    
    ListviewAdapter listViewAdapter;
    ListView listView;
    Double latitude, longitude;
    LocationManager locationManager;
    MyLocationListener gpsLocationListener;
    MyLocationListener networkLocationListener;
    Button stopBtn;
    Button trackBtn;
    Button syncBtn;
    boolean requestLocation;
    int counter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //ListView
        listView = (ListView) findViewById(R.id.listMaps);
        listViewAdapter = new ListviewAdapter(this, new ArrayList());
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (i==0) {
                    Toast.makeText(getApplicationContext(), "Calculating distance atleast need 2 locations", Toast.LENGTH_SHORT).show();
                    return;
                }

                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

                if(activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    try {
                        if (listViewAdapter.arrayList.get(i).optBoolean("Stop")) {
                            Intent intent = new Intent(getApplicationContext(), Maps2Activity.class);
                            ArrayList<LatLng> latLngs = new ArrayList<>();
                            for (int k = 0; k < i; k++) {
                                Double lat = listViewAdapter.arrayList.get(k).optDouble("Latitude");
                                Double lon = listViewAdapter.arrayList.get(k).optDouble("Longitude");
                                LatLng latLng = new LatLng(lat, lon);
                                latLngs.add(latLng);
                            }
                            intent.putExtra("listLatLng", latLngs);
                            intent.putExtra("stopClicked", true);
                            startActivity(intent);
                        } else {

                            Intent intent = new Intent(getApplicationContext(), Maps2Activity.class);
                            latitude = listViewAdapter.arrayList.get(i).optDouble("Latitude");
                            longitude = listViewAdapter.arrayList.get(i).optDouble("Longitude");
                            intent.putExtra("latitude", latitude);
                            intent.putExtra("longitude", longitude);
                            ArrayList<LatLng> latLngs = new ArrayList<>();
                            for (int k = 0; k <= i; k++) {
                                if (!listViewAdapter.arrayList.get(k).optBoolean("Stop")) {
                                    Double lat = listViewAdapter.arrayList.get(k).optDouble("Latitude");
                                    Double lon = listViewAdapter.arrayList.get(k).optDouble("Longitude");
                                    LatLng latLng = new LatLng(lat, lon);
                                    latLngs.add(latLng);
                                }
                            }
                            intent.putExtra("listLatLng", latLngs);
                            intent.putExtra("stopBtnClicked", false);
                            startActivity(intent);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Internet conncetion problem, Try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        syncBtn = (Button) findViewById(R.id.sync);
        stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setEnabled(false);
        trackBtn = (Button) findViewById(R.id.searchBtn);
        trackBtn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                trackBtn.setClickable(false);
                stopBtn.setClickable(false);
                syncBtn.setClickable(false);
                listView.setClickable(false);
                if (listViewAdapter.arrayList.size() > 0) {
                    if (listViewAdapter.arrayList.get(listViewAdapter.arrayList.size() - 1).optBoolean("Restart")) {
                        listViewAdapter.arrayList.clear();
                        counter = -1;
                    }
                }

                if (listViewAdapter.arrayList.size() > 0) {
                    stopBtn.setEnabled(true);
                }
                trackLocation();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float sum = 0;
                if (listViewAdapter.arrayList.size() > 0) {
                    for (int i = 0; i < listViewAdapter.arrayList.size(); i++) {
                        sum += listViewAdapter.arrayList.get(i).optDouble("Distance");
                    }
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Sum", sum);
                        jsonObject.put("Stop", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    listViewAdapter.arrayList.add(jsonObject);
                    listViewAdapter.notifyDataSetChanged();
                    stopBtn.setEnabled(false);
                }
            }
        });

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setClickable(false);
                for(int i=-1; i<listViewAdapter.arrayList.size()-1; i++){
                    if(listViewAdapter.arrayList.get(i+1).optString("Geolocation").equals("Street address not found")){
                        Location locat = new Location("");
                        locat.setLatitude(listViewAdapter.arrayList.get(i+1).optDouble("Latitude"));
                        locat.setLongitude(listViewAdapter.arrayList.get(i+1).optDouble("Longitude"));
                        try {
                            listViewAdapter.arrayList.get(i+1).put("visibility", "visible");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        listViewAdapter.notifyDataSetChanged();
                        new AsyncClass(MapsActivity.this, locat, i, "update").execute();
                    }
                }
            }
        });
    }

    public void trackLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean flag1;
            flag1 = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (flag1) {
                Log.v("function", "onClick");
                gpsLocationListener = new MyLocationListener();
                networkLocationListener = new MyLocationListener();
            } else {
                gpsDialog();
                trackBtn.setClickable(true);
                stopBtn.setClickable(true);
                syncBtn.setClickable(true);
                listView.setClickable(true);
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("visibility", "visible");
                listViewAdapter.arrayList.add(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listViewAdapter.notifyDataSetChanged();
            requestLocation = true;
            /*Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);*/
            Log.d("maps", "Requesting location updates");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, gpsLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, networkLocationListener);
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int grantResults[]) {
        switch (requestCode) {

            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    trackLocation();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if(requestLocation) {
                requestLocation = false;
                Log.d("maps", "on location changed : " + location.getLatitude() + "---" + location.getLongitude());
                AsyncClass asyncClass = new AsyncClass(MapsActivity.this, location, counter, "add");
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
                locationManager.removeUpdates(gpsLocationListener);
                locationManager.removeUpdates(networkLocationListener);
                asyncClass.execute();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("maps" , "Status Changed");
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d("maps" , "Provider Enabled");
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d("maps" , "Provider Disabled");
        }
    }

    public Boolean gpsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to start GPS?")
                .setCancelable(false)
                .setTitle("GPS DISABLED")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {}
}
