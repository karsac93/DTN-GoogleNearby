package com.mst.karsac.ratings;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;
import com.mst.karsac.messages.Messages;

import java.util.ArrayList;
import java.util.List;


public class RatingsActivity extends AppCompatActivity {

    public static final String TAG = RatingsActivity.class.getSimpleName();
    public static final float NOT_YET_RATED_CODE = 555.55f;
    RecyclerView recyclerView;
    RatingsAdapter ratingsAdapter;
    Messages msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        if(Build.VERSION.SDK_INT >= 21){
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        List<MessageRatings> messageRatingsList = new ArrayList<>();

        if (getIntent().hasExtra("Message")) {
            msg = (Messages) getIntent().getSerializableExtra("Message");
            Log.d(TAG, "Intermediaries:" + msg.destAddr);
            String[] msg_array = msg.destAddr.split("\\|");

            MessageRatings messageRatings;

            for (String mac_addresses : msg_array) {
                if (!mac_addresses.contains(GlobalApp.source_mac) && !msg.sourceMac.contains(mac_addresses)) {
                    messageRatings = new MessageRatings();
                    messageRatings.setMessage_unique_id(msg.uuid);
                    messageRatings.setIntermediary(mac_addresses);
                    messageRatings.setInter_type("Intermediary");
                    messageRatings.setConfidence_rate(0.0f);
                    messageRatings.setQuality_rate(0.0f);
                    messageRatings.setTag_rate(0.0f);
                    messageRatings.setLocal_average(NOT_YET_RATED_CODE);
                    messageRatingsList.add(messageRatings);
                }
                else{
                    break;
                }
            }
            messageRatings = new MessageRatings();
            messageRatings.setMessage_unique_id(msg.uuid);
            messageRatings.setIntermediary(msg.sourceMac);
            messageRatings.setInter_type("Source");
            messageRatings.setConfidence_rate(0.0f);
            messageRatings.setQuality_rate(0.0f);
            messageRatings.setTag_rate(0.0f);
            messageRatings.setLocal_average(NOT_YET_RATED_CODE);
            messageRatingsList.add(messageRatings);
        }

        List<MessageRatings> rating_fromSql = GlobalApp.dbHelper.getRatingsMessage(msg.uuid, null);
        for (MessageRatings from_table : rating_fromSql) {
            for (MessageRatings from_msg : messageRatingsList) {
                if (from_msg.intermediary.contains(from_table.intermediary)) {
                    from_msg.local_average = from_table.local_average;
                    from_msg.tag_rate = from_table.tag_rate;
                    from_msg.confidence_rate = from_table.confidence_rate;
                    from_msg.quality_rate = from_table.quality_rate;
                }
            }
        }

        for (MessageRatings messageRatings : messageRatingsList) {
            if(messageRatings.local_average == NOT_YET_RATED_CODE) {
                GlobalApp.dbHelper.insertMessageRating(messageRatings);
            }
        }

        recyclerView = findViewById(R.id.recycler_rating);
        ratingsAdapter = new RatingsAdapter(messageRatingsList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(ratingsAdapter);
        ratingsAdapter.notifyDataSetChanged();
    }
}

