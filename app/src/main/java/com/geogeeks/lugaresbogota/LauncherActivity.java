package com.geogeeks.lugaresbogota;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu_laucher);



    }

    public void onClickRank(View v)
    {
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();

    }



    public void onClickMap(View v)
    {
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();

        Intent myIntent = new Intent(LauncherActivity.this, MainActivity.class);
        LauncherActivity.this.startActivity(myIntent);
    }
}