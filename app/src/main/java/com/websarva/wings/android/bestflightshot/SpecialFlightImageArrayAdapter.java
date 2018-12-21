package com.websarva.wings.android.bestflightshot;

import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SpecialFlightImageArrayAdapter extends ArrayAdapter<SpecialListItem> {

    private int resourceId;
    private List<SpecialListItem> items;
    private LayoutInflater inflater;

    public SpecialFlightImageArrayAdapter(Context context, int resourceId, List<SpecialListItem> items) {
        super(context, resourceId, items);

        this.resourceId = resourceId;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static class ViewHolder {

        TextView aircraftName;
        TextView airline;
        TextView departureTime;
        TextView specialInfo;
        ImageView aircraftImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if(convertView == null) {
            convertView = inflater.inflate(resourceId,parent,false);

            holder = new ViewHolder();
            holder.aircraftName = convertView.findViewById(R.id.tvAircraft);
            holder.airline = convertView.findViewById(R.id.tvAirline);
            holder.departureTime = convertView.findViewById(R.id.tvDeparture);
            holder.specialInfo = convertView.findViewById(R.id.tvSpecialInfo);
            holder.aircraftImage = convertView.findViewById(R.id.aircraftImage);
            convertView.setTag(holder);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 500);

            convertView.setLayoutParams(params);

        }

        else {
            holder = (ViewHolder) convertView.getTag();
        }

        SpecialListItem item = this.items.get(position);
        holder.aircraftName.setText(item.getCraftType());
        holder.airline.setText(item.getAirline());
        holder.departureTime.setText(item.getDepartureTime());
        holder.specialInfo.setText(item.getSpecialInfo());
        holder.aircraftImage.setImageResource(item.getImageId());

        return convertView;

    }

}
