package com.websarva.wings.android.bestflightshot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionSelectActivity extends AppCompatActivity {

    //Intentの情報を受け取る、送る
    private String airport;
    Intent intent;

    private Spinner timezone_spinner;
    private Spinner aircraftManufacturer_spinner;
    private Spinner aircraft_spinner;
    private Spinner airline_spinner;

    private TextView tvAircraft;

    private List<Map<String,String>> timezonList;
    private List<Map<String,String>> aircraftManufacturerList;
    private List<String> aircraftList;
    private List<String> airlineList;

    private static final String[] timezone_FROM={"minTime","maxTime"};
    private static final int[] timezone_TO={R.id.minTime,R.id.maxTime};

    private static final String[] aircraftmanufacturer_FROM={"manufacturer","country"};
    private static final int[] aircraftmanufacturer_TO={R.id.manufacturer,R.id.country};

    private static final String[] aircraft_FROM={"aircraft_image","aircraft"};
    private static final int[] aircraft_TO={R.id.aircraft_image,R.id.aircraft};

    private SimpleAdapter adapter;
    private SimpleAdapter  aircraftManufacturer_adapter;
    private ArrayAdapter aircraft_adapter;
    private ArrayAdapter airline_adapter;

    //Spinnerで選択された値を入れる変数群
    private String minTime;
    private String maxTime;

    private String manufacturer;
    private String country;

    private String aircraft;

    private String airline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition_select);
        setTitle("航空機の条件をしてください");

        Intent _intent=getIntent();
        airport=_intent.getStringExtra("airport");
        intent=new Intent(ConditionSelectActivity.this,ConditionFlightActivity.class);
        intent.putExtra("airport",airport);

        //各リストを作成します
        timezonList=createTimeZoneList();
        aircraftManufacturerList=createAircraftManufacturerList();
        aircraftList=createAircraft_boeing();
        airlineList=createAirlineList();

        //timezoneのアダプタを設定
        timezone_spinner=(Spinner) findViewById(R.id.timezone_spinner);
        adapter=new SimpleAdapter(ConditionSelectActivity.this,timezonList,R.layout.timezone_list,timezone_FROM,timezone_TO);
        adapter.setDropDownViewResource(R.layout.timezone_dropdown_list);
        timezone_spinner.setAdapter(adapter);
        timezone_spinner.setPrompt("時間帯を選択してください");
        timezone_spinner.setOnItemSelectedListener(new spinnerTimezoneListener());

        //aircraftManufacturerのアダプタを設定
        aircraftManufacturer_spinner=(Spinner) findViewById(R.id.aircraftManufacturer_spinner);
        aircraftManufacturer_adapter=new SimpleAdapter(ConditionSelectActivity.this,aircraftManufacturerList,R.layout.aircraftmanufacturer_list,aircraftmanufacturer_FROM,aircraftmanufacturer_TO);
        aircraftManufacturer_adapter.setDropDownViewResource(R.layout.aircraftmanufacturer_dropdown_list);
        aircraftManufacturer_spinner.setAdapter(aircraftManufacturer_adapter);
        aircraftManufacturer_spinner.setPrompt("航空機メーカーを選択してください");
        aircraftManufacturer_spinner.setOnItemSelectedListener(new spinnerAircraftManufacturerListener());

        //aircraftのアダプタの設定
        aircraft_spinner=(Spinner) findViewById(R.id.aircraft_spinner);
        tvAircraft=(TextView) findViewById(R.id.tvAircraft);
        aircraft_adapter=new ArrayAdapter(ConditionSelectActivity.this,android.R.layout.simple_list_item_1,aircraftList);
        aircraft_spinner.setAdapter(aircraft_adapter);
        aircraft_spinner.setPrompt("航空機を選択してください");
        aircraft_spinner.setOnItemSelectedListener(new spinnerAircraftListener());

        //airlineのアダプタ設定
        airline_spinner=(Spinner) findViewById(R.id.airline_spinner);
        airline_adapter=new ArrayAdapter(ConditionSelectActivity.this,android.R.layout.simple_list_item_1,airlineList);
        airline_spinner.setAdapter(airline_adapter);
        airline_spinner.setPrompt("運行会社を選択してください");
        airline_spinner.setOnItemSelectedListener(new spinnerAirlineListener());
    }

    private List<Map<String,String>> createTimeZoneList(){

        List<Map<String,String>> timeZoneList=new ArrayList<>();
        //HashMapでMaxTime〜MinTimeを作成
        Map<String,String> timeZone=new HashMap<>();
        timeZone.put("minTime","時間帯指定なし");
        timeZone.put("maxTime","");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","12:00");
        timeZone.put("maxTime","12:30");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","12:31");
        timeZone.put("maxTime","13:00");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","13:01");
        timeZone.put("maxTime","13:30");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","13:31");
        timeZone.put("maxTime","14:00");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","14:01");
        timeZone.put("maxTime","14:30");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","14:31");
        timeZone.put("maxTime","15:00");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","15:01");
        timeZone.put("maxTime","15:30");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","16:00");
        timeZone.put("maxTime","16:30");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","16:31");
        timeZone.put("maxTime","17:00");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","17:00");
        timeZone.put("maxTime","17:30");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","17:31");
        timeZone.put("maxTime","18:00");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","18:01");
        timeZone.put("maxTime","18:30");
        timeZoneList.add(timeZone);

        timeZone=new HashMap<>();
        timeZone.put("minTime","23:30");
        timeZone.put("maxTime","23:59");
        timeZoneList.add(timeZone);

        //ArrayList(MaxTime.MinTime)を返す
        return timeZoneList;
    }

    private List<Map<String,String>> createAircraftManufacturerList(){

        List<Map<String,String>> aircraftManufacturerList=new ArrayList<>();
        //HashMapで航空機メーカーリストを作成
        Map<String,String> aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","航空機メーカー指定なし");
        aircraftManufacturer.put("country","");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","ボーイング社");
        aircraftManufacturer.put("country","アメリカ");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","ロッキード・マーティン社");
        aircraftManufacturer.put("country","アメリカ");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","セスナ社");
        aircraftManufacturer.put("country","アメリカ");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","ボンバルディア社");
        aircraftManufacturer.put("country","カナダ");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","ダグラス社");
        aircraftManufacturer.put("country","カナダ");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","エンブラレル社");
        aircraftManufacturer.put("country","ブラジル");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","ツポレフ社");
        aircraftManufacturer.put("country","ロシア");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","イリューシン社");
        aircraftManufacturer.put("country","ロシア");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","アントノフ社");
        aircraftManufacturer.put("country","ウクライナ");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","エアバス社");
        aircraftManufacturer.put("country","欧州共同");
        aircraftManufacturerList.add(aircraftManufacturer);

        aircraftManufacturer=new HashMap<>();
        aircraftManufacturer.put("manufacturer","ブリティッシュ社");
        aircraftManufacturer.put("country","イギリス");
        aircraftManufacturerList.add(aircraftManufacturer);

        return aircraftManufacturerList;

    }

    private List<String> createAircraft(){

        //既存の航空機メーカーのリストを削除する

        //航空機メーカーによって航空機の選択リストを変える
        switch (manufacturer){
            case "ボーイング社":
                aircraftList=createAircraft_boeing();
                break;
            case "エアバス社":
                aircraftList=createAircraft_airbus();
                break;
            //航空機メーカーを選択していない時
            case "航空機メーカー指定なし":
                aircraftList=createAircraft_boeing();
                break;
            default:
                aircraftList=createAircraft_boeing();
                break;
        }

        return aircraftList;

    }

    //ボーイング一覧リスト
    private List<String> createAircraft_boeing(){

        //航空機リストを作成
        List<String> aircraftList=new ArrayList<>();
        //ArrayListで航空機を作成！

        aircraftList.add("787");
        aircraftList.add("777");
        aircraftList.add("767");
        aircraftList.add("757");
        aircraftList.add("747");
        aircraftList.add("739");
        aircraftList.add("738");
        aircraftList.add("735");

        return aircraftList;

    }

    //エアバス一覧リスト
    private List<String> createAircraft_airbus(){

        //航空機リストを作成
        List<String> aircraftList=new ArrayList<>();
        //ArrayListで航空機を作成！
        aircraftList.add("300");
        aircraftList.add("310");
        aircraftList.add("319");
        aircraftList.add("320");
        aircraftList.add("330");
        aircraftList.add("350");
        aircraftList.add("380");

        return aircraftList;

    }

    private List<String> createAirlineList(){

        List<String> airlineList=new ArrayList<>();
        airlineList.add("JAL");
        airlineList.add("ANA");

        return airlineList;
    }

    //時間帯Spinnerのリスナ設定
    private class spinnerTimezoneListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,int position,long id){
            Map<String,String> item=(Map<String, String>) parent.getItemAtPosition(position);
            minTime=(String) item.get("minTime");
            maxTime=(String) item.get("maxTime");

            intent.putExtra("minTime",minTime);
            intent.putExtra("maxTime",maxTime);
        }

        @Override
        public  void  onNothingSelected(AdapterView<?> parent){

        }


    }

    //航空機メーカーSpinnerのリスナ設定
    private class spinnerAircraftManufacturerListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
            Map<String,String> item=(Map<String, String>) parent.getItemAtPosition(position);
            manufacturer=(String) item.get("manufacturer");
            country=(String) item.get("country");


            //航空機メーカーを選択するとaircraft_spinnerを表示させる
            if (!(manufacturer.equals("航空機メーカー指定なし"))){
                aircraft_spinner.setVisibility(View.VISIBLE);
                tvAircraft.setVisibility(View.VISIBLE);
            }else if (manufacturer.equals("航空機メーカー指定なし")){
                aircraft_spinner.setVisibility(View.GONE);
                tvAircraft.setVisibility(View.GONE);
            }

            //航空機メーカーによって航空機のリスト、アダプタを更新する
            aircraftList=createAircraft();
            aircraft_adapter=new ArrayAdapter(ConditionSelectActivity.this,android.R.layout.simple_list_item_1,aircraftList);
            aircraft_spinner.setAdapter(aircraft_adapter);
            aircraft_spinner.setOnItemSelectedListener(new spinnerAircraftListener());

            //intent.putExtra("manufacturer",manufacturer);

        }

        @Override
        public  void  onNothingSelected(AdapterView<?> parent){
        }

    }

    //航空機Spinnerのリスナ設定
    private class spinnerAircraftListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,int position,long id){
            aircraft=(String) parent.getItemAtPosition(position);

            intent.putExtra("aircraft",aircraft);
        }

        @Override
        public  void  onNothingSelected(AdapterView<?> parent){
        }

    }

    //運行会社Spinnerのリスナ設定
    private class spinnerAirlineListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
            airline=(String) parent.getItemAtPosition(position);

            intent.putExtra("airline",airline);
        }

        @Override
        public  void  onNothingSelected(AdapterView<?> parent){
        }

    }

    public void onConditionCheckButtonClick(View view){
        startActivity(intent);
    }


}
