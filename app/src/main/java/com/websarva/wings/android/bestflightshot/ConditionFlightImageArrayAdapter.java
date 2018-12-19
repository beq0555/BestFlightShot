package com.websarva.wings.android.bestflightshot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ConditionFlightImageArrayAdapter extends ArrayAdapter<ConditionListItem> {
    private int resourceId;
    private List<ConditionListItem> items;
    private LayoutInflater inflater;

    public ConditionFlightImageArrayAdapter(Context context, int resourceId, List<ConditionListItem> items) {
        super(context, resourceId, items);

        this.resourceId = resourceId;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if(convertView != null) {
            view = convertView;
        } else {
            view = this.inflater.inflate(this.resourceId, null);
        }

        ConditionListItem item = this.items.get(position);

        //機種名をセット
        TextView aircraftName = (TextView)view.findViewById(R.id.tvAircraft);
        aircraftName.setText(item.getCraftType());

        //出発時刻をセット
        TextView departureTime = (TextView)view.findViewById(R.id.tvDeparture);
        departureTime.setText(item.getDepartureTime());

        TextView airline = (TextView)view.findViewById(R.id.tvAirline);
        airline.setText(item.getAirline());

        //機種画像をセット
        ImageView aircraftImage = (ImageView)view.findViewById(R.id.aircraftImage);
        aircraftImage.setImageResource(item.getImageId());


        return view;

    }
}
