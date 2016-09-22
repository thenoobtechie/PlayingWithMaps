package com.example.deepak.mapsproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AsyncClass extends AsyncTask<String, String, JSONObject> {
    List<Address> addresses;
    MapsActivity activity;
    Double latitude, longitude;
    float distance;
    Location location;
    int counter;
    String mode;
    JSONObject jsonObject;

    public AsyncClass(MapsActivity activity, Location location, int counter, String mode) {
        super();
        this.activity = activity;
        this.location = location;
        this.counter = counter;
        this.mode = mode;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        if(mode.equals("add")) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                float[] results = new float[1];
                if (activity.listViewAdapter.arrayList.size() > 1) {
                    Double oldLatitude = activity.listViewAdapter.arrayList.get(activity.listViewAdapter.arrayList.size() - 2).optDouble("Latitude");
                    Double oldLongitude = activity.listViewAdapter.arrayList.get(activity.listViewAdapter.arrayList.size() - 2).optDouble("Longitude");
                    Location.distanceBetween(oldLatitude, oldLongitude, latitude, longitude, results);
                    distance = results[0];
                } else
                    distance = 0;

                Log.d("maps", "LatLon ->" + latitude + "---" + longitude);

                Geocoder geocoder = new Geocoder(activity.getApplicationContext(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                    jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Latitude", latitude);
                        jsonObject.put("Longitude", longitude);
                        jsonObject.put("Geolocation", "Street address not found");
                        jsonObject.put("Distance", distance);
                        jsonObject.put("Stop", false);
                    } catch (JSONException jE) {
                        jE.printStackTrace();
                    }
                    return jsonObject;
                }
                Address address;
                String street = "";
                List<String> add;
                try {
                    address = addresses.get(0);
                    add = new ArrayList<>();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        add.add(address.getAddressLine(i));
                    }
                    for (int i = 0; i < add.size(); i++) {
                        street = street.concat(add.get(i)).concat(", ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                jsonObject = new JSONObject();
                try {
                    jsonObject.put("Latitude", latitude);
                    jsonObject.put("Longitude", longitude);
                    jsonObject.put("Geolocation", street);
                    jsonObject.put("Distance", distance);
                    jsonObject.put("Stop", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Geolocation", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }else if(mode.equals("update")){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
                /*float[] results = new float[1];
                old.setLatitude(activity.listViewAdapter.arrayList.get(counter - 1).optDouble("Latitude"));
                old.setLongitude(activity.listViewAdapter.arrayList.get(counter - 1).optDouble("Longitude"));
                Location.distanceBetween(old.getLatitude(), old.getLongitude(), latitude, longitude, results);
                distance = results[0];*/
                try {
                    distance = (float) activity.listViewAdapter.arrayList.get(counter+1).getDouble("Distance");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Geocoder geocoder = new Geocoder(activity.getApplicationContext(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException | IllegalArgumentException e) {
                    e.printStackTrace();
                    jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Latitude", latitude);
                        jsonObject.put("Longitude", longitude);
                        jsonObject.put("Geolocation", "Street address not found");
                        jsonObject.put("Distance", distance);
                        jsonObject.put("Stop", false);
                    } catch (JSONException jE) {
                        jE.printStackTrace();
                    }
                    return jsonObject;
                }
                Address address;
                String street = "";
                List<String> add;
                try {
                    address = addresses.get(0);
                    add = new ArrayList<>();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        add.add(address.getAddressLine(i));
                    }
                    for (int i = 0; i < add.size(); i++) {
                        street = street.concat(add.get(i)).concat(", ");
                    }
                } catch (Exception e) {
                    street = "Street address not found";
                    e.printStackTrace();
                }
                jsonObject = new JSONObject();
                try {
                    jsonObject.put("Latitude", latitude);
                    jsonObject.put("Longitude", longitude);
                    jsonObject.put("Geolocation", street);
                    jsonObject.put("Distance", distance);
                    jsonObject.put("Stop", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        return jsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        activity.counter++;
        counter++;
        try {
            jsonObject.put("visibility", "gone");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        activity.listViewAdapter.arrayList.remove(counter);
        activity.listViewAdapter.arrayList.add(counter, jsonObject);
        try {
            activity.listViewAdapter.arrayList.get(counter).put("visibility", "gone");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        activity.listViewAdapter.notifyDataSetChanged();
        activity.trackBtn.setClickable(true);
        activity.stopBtn.setClickable(true);
        activity.syncBtn.setClickable(true);
        activity.listView.setClickable(true);
    }
}
