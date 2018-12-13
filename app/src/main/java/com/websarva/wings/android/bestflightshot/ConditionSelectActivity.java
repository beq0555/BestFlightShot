package com.websarva.wings.android.bestflightshot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionSelectActivity extends AppCompatActivity {

    private List<Map<String,String>> TimeZoneList;
    private ListView lvTimeZone=findViewById(R.id.lvTimeZone);
    private ImageView ivcheck=findViewById(R.id.ivcheck);
    private static final String[] FROM={"minTime","maxTime"};
    private static final int[] TO={R.id.tvMaxTime,R.id.tvMinTime};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition_select);

        //TimeZone（リスト画面上側）を表示させる
        lvTimeZone=findViewById(R.id.lvTimeZone);
        TimeZoneList=createTimeZoneList();
        SimpleAdapter adapter=new SimpleAdapter(ConditionSelectActivity.this,TimeZoneList,R.layout.timezone_list,FROM,TO);
        lvTimeZone.setAdapter(adapter);

        //TimeZoneリストをタップした時の処理
        lvTimeZone.setOnItemClickListener(new ListTimeZoneClickListener());
    }

    private List<Map<String,String>> createTimeZoneList(){

        List<Map<String,String>> timeZoneList=new ArrayList<>();
        //HashMapでMaxTime〜MinTimeを作成
        Map<String,String> timeZone=new HashMap<>();
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
        timeZone.put("minTime","15:31");
        timeZone.put("maxTime","16:00");
        timeZoneList.add(timeZone);

        //ArrayList(MaxTime.MinTime)を返す
        return timeZoneList;
    }

    //TimeZoneListがタップされた時の処理が記述されたメンバクラス
    private class ListTimeZoneClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view,int position,long id){
            Map<String,String> timeZone=(Map<String, String>) parent.getItemAtPosition(position);
            String minTime=timeZone.get("minTime");
            String maxTime=timeZone.get("maxTime");

            

        }
    }


}
