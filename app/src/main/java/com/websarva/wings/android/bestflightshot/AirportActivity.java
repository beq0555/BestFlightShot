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
        setTitle("撮影する空港を選択してください");
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
