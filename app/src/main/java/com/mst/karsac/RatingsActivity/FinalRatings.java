package com.mst.karsac.RatingsActivity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;
import com.mst.karsac.ratings.RatingPOJ;

import java.util.List;

public class FinalRatings extends AppCompatActivity {

    RecyclerView final_rating_view;
    FinalRatingsAdapter finalRatingsAdapter;
    TextView default_msg_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_ratings);
        final_rating_view = findViewById(R.id.final_ratings);
        default_msg_txt = findViewById(R.id.default_msg);
        List<RatingPOJ> ratingPOJList = GlobalApp.dbHelper.getRatings();
        if (ratingPOJList.size() == 0) {
            default_msg_txt.setVisibility(View.VISIBLE);
            final_rating_view.setVisibility(View.GONE);
        } else {
            default_msg_txt.setVisibility(View.GONE);
            final_rating_view.setVisibility(View.VISIBLE);
        }
        finalRatingsAdapter = new FinalRatingsAdapter(ratingPOJList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        final_rating_view.setLayoutManager(layoutManager);
        final_rating_view.setItemAnimator(new DefaultItemAnimator());
        final_rating_view.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        final_rating_view.setAdapter(finalRatingsAdapter);
        finalRatingsAdapter.notifyDataSetChanged();
    }
}
