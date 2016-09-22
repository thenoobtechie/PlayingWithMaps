package com.example.deepak.mapsproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class ListviewAdapter extends BaseAdapter {
    ArrayList<JSONObject> arrayList;
    Context context;

    public ListviewAdapter(Context context, ArrayList arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.row, null);

        TextView loc = (TextView) view.findViewById(R.id.location);
        TextView street = (TextView) view.findViewById(R.id.street);
        TextView distance = (TextView) view.findViewById(R.id.distance);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBarInListItem);

        if (arrayList.get(i).optString("visibility").equals("visible")) {
            progressBar.setVisibility(View.VISIBLE);
            street.setText("");
            distance.setText("");
            loc.setText("");
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            if (!arrayList.get(i).optBoolean("Stop")) {
                loc.setText(String.format(Locale.getDefault(), "%1$f, %2$f", arrayList.get(i).optDouble("Latitude"), arrayList.get(i).optDouble("Longitude")));
                street.setText(String.format(Locale.getDefault(), "%1$s", arrayList.get(i).optString("Geolocation")));
                distance.setText(String.format(Locale.getDefault(), "%1$f m", arrayList.get(i).optDouble("Distance")));
            } else {
                loc.setText(String.format(Locale.getDefault(), "Total distance travelled is : %1$f m", arrayList.get(i).optDouble("Sum")));
                street.setText("");
                distance.setText("");
                try {
                    arrayList.get(i).put("Restart", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return view;
    }
}
