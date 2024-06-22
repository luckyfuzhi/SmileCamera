package com.example.smilecamera.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.smilecamera.R;
import com.example.smilecamera.util.ConstantUtil;


public class BridgeActivity extends AppCompatActivity {

    private CardView mostPeople;
    private CardView nearestPeople;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bridge);
        ConstantUtil.setStatusLine(getWindow(), this);

        Toolbar toolbar = findViewById(R.id.child_toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });
        TextView title = findViewById(R.id.toolbar_title);
        title.setText("请选择笑脸相机模式");

        mostPeople = findViewById(R.id.cv_most_people);
        nearestPeople = findViewById(R.id.cv_nearest_people);

        mostPeople.setOnClickListener(view -> {
            Intent intent = new Intent(this, SmileCameraActivity.class);
            intent.putExtra(ConstantUtil.DETECT_MODE, ConstantUtil.MOST_PEOPLE);
            startActivity(intent);
        });

        nearestPeople.setOnClickListener(view -> {
            Intent intent = new Intent(this, SmileCameraActivity.class);
            intent.putExtra(ConstantUtil.DETECT_MODE, ConstantUtil.NEAREST_PEOPLE);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mostPeople.setOnClickListener(null);
        nearestPeople.setOnClickListener(null);
    }
}