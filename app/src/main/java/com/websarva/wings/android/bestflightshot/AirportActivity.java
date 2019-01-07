package com.websarva.wings.android.bestflightshot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AirportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport);
        Intent intent = getIntent();
        setTitle("撮影を行う空港を選択してください");
    }

    public void onHanedaClick(View view){
        Intent intent=new Intent(AirportActivity.this,SelectListActivity.class);
        intent.putExtra("airport","HND");
        startActivity(intent);
    }

    public void onNaritaClick(View view){
        Intent intent=new Intent(AirportActivity.this,SelectListActivity.class);
        intent.putExtra("airport","NRT");
        startActivity(intent);
    }
}
//3D:B6:E0:9C:F0:89:B9:0A:BD:87:AC:EA:39:19:CD:AB:3C:BE:B5:B1