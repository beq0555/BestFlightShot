package com.websarva.wings.android.bestflightshot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class OpeningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);
        setTitle("Shot your flight of imagination!");
    }
    public void onOpeningClick(View view) {
        Intent intent=new Intent(OpeningActivity.this,AirportActivity.class);
        startActivity(intent);
    }
    public void onManualClick() {
        Intent intent = new Intent(OpeningActivity.this,AirportActivity.class);
        startActivity(intent);
    }
}
