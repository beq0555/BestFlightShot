package com.websarva.wings.android.bestflightshot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class ImageSpinnerAdapter extends SimpleAdapter {

    private Context context;
    private LayoutInflater mInflater;
    private List<? extends Map<String, Object>> list_data;

    // 初期化
    public ImageSpinnerAdapter(Context context, List<? extends Map<String, Object>> list_data, int resource, String[] from, int[] to) {
        super(context, list_data, resource, from, to);

        this.context = context;
        this.list_data = list_data;

        // リストの動的な描画のためにインフレータを生成
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        //一行づつ生成
        View view=convertView;
        //aircraft_list.xmlとの紐付け
        if(view == null){
            view = mInflater.inflate(R.layout.aircraft_list, null);
        }
        Map<String,Object> aircraft_line=list_data.get(position);

        //aircraftの読みこみ
        String aircraft=(String) aircraft_line.get("aircraft");
        TextView tvAircraft=(TextView) view.findViewById(R.id.aircraft);
        tvAircraft.setText(aircraft);


        //aircraft_imageの読み込み
        Integer aircraft_image=(Integer) aircraft_line.get("aircraft_image");
        ImageView ivAircraft=(ImageView) view.findViewById(R.id.aircraft_image);
        ivAircraft.setImageResource(aircraft_image);

        return view;
    }
}