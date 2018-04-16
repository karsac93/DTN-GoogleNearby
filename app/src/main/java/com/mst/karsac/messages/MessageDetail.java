package com.mst.karsac.messages;

import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mst.karsac.DbHelper.DbHelper;
import com.mst.karsac.GlobalApp;
import com.mst.karsac.MainActivity;
import com.mst.karsac.R;

import java.io.File;

public class MessageDetail extends AppCompatActivity {
    ImageView msgImg;
    Messages msg;
    TextView file_name, source_mac, latitude, longitude, timestamp_created, incentive_paid,
            incentive_promised, incentive_received;
    EditText tags;
    Button save_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
            msg = bundle.getParcelable(MsgAdapter.MSG_KEY);
            if(msg == null)
                finish();
        }
        initCollapsingToolbar(msg);

        msgImg = findViewById(R.id.msgImg);
        Log.d("MessageDetail", msg.imgPath);
        Glide.with(this).load(msg.imgPath).into(msgImg);

        file_name = findViewById(R.id.file_name);
        source_mac = findViewById(R.id.source_mac);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        timestamp_created = findViewById(R.id.timestamp_created);
        incentive_paid = findViewById(R.id.incentive_paid);
        incentive_promised = findViewById(R.id.incentive_promised);
        incentive_received = findViewById(R.id.incentive_received);
        tags = findViewById(R.id.insert_tags);
        save_btn = findViewById(R.id.save_btn);

        file_name.append(msg.fileName);
        source_mac.append(msg.sourceMac);
        latitude.append(String.valueOf(msg.lat));
        longitude.append(String.valueOf(msg.lon));
        timestamp_created.append(msg.timestamp);
        incentive_received.append(String.valueOf(msg.incentive_received));
        incentive_paid.append(String.valueOf(msg.incentive_paid));
        incentive_promised.append(String.valueOf(msg.incentive_promised));
        tags.setText(msg.tagsForCurrentImg);

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tagsObtained = tags.getText().toString();
                Log.d("TAGS", tagsObtained);
                DbHelper dbHelper = GlobalApp.dbHelper;
                msg.tagsForCurrentImg = tagsObtained;
                dbHelper.updateMsg(msg);
                String[] interests = tagsObtained.split(",");
                for (String intrst : interests)
                    dbHelper.insertInterest(intrst.trim(), 0, 0.5f);
            }
        });
    }

    public void initCollapsingToolbar(Messages msg) {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_detail);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.title_activity_message_detail));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }
}
