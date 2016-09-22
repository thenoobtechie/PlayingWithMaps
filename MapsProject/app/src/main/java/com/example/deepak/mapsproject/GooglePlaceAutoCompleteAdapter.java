package com.example.deepak.mapsproject;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class GooglePlaceAutoCompleteAdapter extends ArrayAdapter implements Filterable {
    private List resultList = null;
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String API_KEY = "AIzaSyBOFYdK1lnqHYRcxnLoSks_257kI-ctYhs";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    public GooglePlaceAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }


    public static ArrayList autocomplete(String input) {
        ArrayList<String> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            String sb = PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON + "?key=" + API_KEY + "&input=" + URLEncoder.encode(input, "utf8");
            //sb.append("&components=country:in");

            URL url = new URL(sb);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("Error!!", "Error processing Places API URL", e);
        } catch (IOException e) {
            Log.e("Error!!", "Error connecting to Places API", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
         try {
             // Create a JSON object hierarchy from the results
             JSONObject jsonObj = new JSONObject(jsonResults.toString());
             JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

             // Extract the Place descriptions from the results
             resultList = new ArrayList<>(predsJsonArray.length());
             for (int i = 0; i < predsJsonArray.length(); i++) {
                 /*System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                 System.out.println("============================================================");*/
                 resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
             }
         } catch (JSONException e) {
             Log.e("Error!!", "Cannot process JSON results", e);
         }

        return resultList;
    }
}
