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

    RecyclerView recyclerView;
    RatingsAdapter ratingsAdapter;

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
        List<RatingPOJ> ratingsObjects = new ArrayList<>();
        if (getIntent().hasExtra("Message")) {
            Messages msg = (Messages) getIntent().getSerializableExtra("Message");
            Log.d(TAG, "Intermediaries:" + msg.destAddr);
            String[] msg_array = msg.destAddr.split("\\|");

            for (String mac_addresses : msg_array) {
                if (!mac_addresses.contains(GlobalApp.source_mac) && !msg.sourceMac.contains(mac_addresses)) {
                    RatingPOJ ratingPOJ = new RatingPOJ();
                    ratingPOJ.average = 0.0f;
                    ratingPOJ.mac_address = mac_addresses;
                    ratingPOJ.type = "Intermediary";
                    ratingsObjects.add(ratingPOJ);
                }
                else{
                    break;
                }
            }
            RatingPOJ ratingPOJ = new RatingPOJ();
            ratingPOJ.mac_address = msg.sourceMac;
            ratingPOJ.average = 0.0f;
            ratingPOJ.type = "Source";
            ratingsObjects.add(ratingPOJ);
        }

        List<RatingPOJ> rating_fromSql = GlobalApp.dbHelper.getRatings();
        for (RatingPOJ from_table : rating_fromSql) {
            for (RatingPOJ from_msg : ratingsObjects) {
                if (from_msg.mac_address.contains(from_table.mac_address)) {
                    from_msg.average = from_table.average;
                }
            }
        }

        for (RatingPOJ ratingPOJs : ratingsObjects) {
            GlobalApp.dbHelper.insertOrUpdateRating(ratingPOJs);
        }

        recyclerView = findViewById(R.id.recycler_rating);
        ratingsAdapter = new RatingsAdapter(ratingsObjects, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(ratingsAdapter);
        ratingsAdapter.notifyDataSetChanged();
    }

}

