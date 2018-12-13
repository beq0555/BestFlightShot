package com.websarva.wings.android.bestflightshot;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if(convertView != null) {
            view = convertView;
        } else {
            view = this.inflater.inflate(this.resourceId, null);
        }

        SpecialListItem item = this.items.get(position);

        //機種名をセット
        TextView aircraftName = (TextView)view.findViewById(R.id.tvAircraft);
        aircraftName.setText(item.getCraftType());

        //出発時刻をセット
        TextView departureTime = (TextView)view.findViewById(R.id.tvDeparture);
        departureTime.setText(item.getDepartureTime());

        //特別情報をセット
        TextView specialInfo = (TextView)view.findViewById(R.id.tvSpecialInfo);
        specialInfo.setText(item.getSpecialInfo());

        //機種画像をセット
        ImageView aircraftImage = (ImageView)view.findViewById(R.id.aircraftImage);
        aircraftImage.setImageResource(item.getImageId());

        return view;

    }

}
