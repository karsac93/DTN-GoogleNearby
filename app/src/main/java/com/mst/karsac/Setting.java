package com.mst.karsac;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mst.karsac.Utils.SharedPreferencesHandler;

public class Setting extends AppCompatActivity {

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        radioGroup = findViewById(R.id.radio_group);
        pushBtn = findViewById(R.id.setting_push);
        pullBtn = findViewById(R.id.setting_pull);

        String mode = SharedPreferencesHandler.getSelectedMode(this, MODE_SELECTION);
        if (mode.equals(PUSH))
            pushBtn.setChecked(true);
        else
            pullBtn.setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if(checkedId == R.id.setting_push)
                    SharedPreferencesHandler.setStringPreferences(getApplicationContext(), MODE_SELECTION, PUSH);
                else if(checkedId == R.id.setting_pull){
                    SharedPreferencesHandler.setStringPreferences(getApplicationContext(), MODE_SELECTION, PULL);

                }
            }
        });

    }

}
