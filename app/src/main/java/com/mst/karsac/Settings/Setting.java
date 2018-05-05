package com.mst.karsac.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mst.karsac.R;
import com.mst.karsac.Utils.SharedPreferencesHandler;

public class Setting extends AppCompatActivity {

    public static final String TAG = "SettingActivity";
    public static final String MODE_SELECTION = "return_mode";
    public static final String PUSH = "push";
    public static final String PULL = "pull";

    public static final String TAG_KEYS = "tags";
    public static final String LAT_LON_KEY = "location";
    public static final String RADIUS = "radius";

    private RadioGroup radioGroup;
    private RadioButton pushBtn, pullBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Setting.this, PullActivity.class);
                startActivity(intent);
            }
        });

        radioGroup = findViewById(R.id.radio_group);
        pushBtn = findViewById(R.id.setting_push);
        pullBtn = findViewById(R.id.setting_pull);

        String mode = SharedPreferencesHandler.getStringPreferences(this, MODE_SELECTION);
        if (mode.equals(PUSH))
            pushBtn.setChecked(true);
        else
            pullBtn.setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.setting_push)
                    SharedPreferencesHandler.setStringPreferences(getApplicationContext(), MODE_SELECTION, PUSH);
                else if (checkedId == R.id.setting_pull) {
                    if (SharedPreferencesHandler.getStringPreferences(getApplicationContext(),
                            TAG_KEYS).trim().length() == 0 &&
                            SharedPreferencesHandler.getStringPreferences(getApplicationContext(),
                                    LAT_LON_KEY).trim().length() == 0) {
                        Log.d(TAG, "Pull has not been set at all, so firing it");
                        Intent intent = new Intent(Setting.this, PullActivity.class);
                        startActivity(intent);
                    }
                    else{
                        SharedPreferencesHandler.setStringPreferences(getApplicationContext(), MODE_SELECTION, PULL);
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Inside on resume");
        if (pullBtn.isChecked()) {
            if (SharedPreferencesHandler.getStringPreferences(getApplicationContext(),
                    TAG_KEYS).trim().length() == 0 &&
                    SharedPreferencesHandler.getStringPreferences(getApplicationContext(),
                            LAT_LON_KEY).trim().length() == 0) {
                pushBtn.setChecked(true);
            }
        }
    }
}
