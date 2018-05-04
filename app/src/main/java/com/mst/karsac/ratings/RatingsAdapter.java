package com.mst.karsac.ratings;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mst.karsac.GlobalApp;
import com.mst.karsac.R;

import java.util.List;

public class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.RatingViewHolder> {
private List<RatingPOJ> ratingList;
public TextView mac_address_txt, type_mac, average_mac, quality_text;
public RatingBar tags_rate, confidence_rate, quality_rate;
Button save_ratings;
Context context;


    public RatingsAdapter(List<RatingPOJ> ratingList, RatingsActivity ratingsActivity) {
        this.ratingList = ratingList;
        context = ratingsActivity;
    }

    @Override
    public RatingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_ratings_row, parent, false);
        return new RatingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RatingViewHolder holder, int position) {
        final RatingPOJ ratingPOJ = ratingList.get(position);
        mac_address_txt.append(ratingPOJ.mac_address);
        type_mac.append(ratingPOJ.type);
        average_mac.append(String.valueOf(ratingPOJ.average));
        if(!ratingPOJ.type.contains("Source")){
            quality_rate.setVisibility(View.GONE);
            quality_text.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return ratingList.size();
    }

    public class RatingViewHolder extends RecyclerView.ViewHolder{

        public RatingViewHolder(final View itemView) {
            super(itemView);

            mac_address_txt = itemView.findViewById(R.id.mac_address);
            type_mac = itemView.findViewById(R.id.type_mac);
            average_mac = itemView.findViewById(R.id.average_rating);

            tags_rate = itemView.findViewById(R.id.tags_rate);
            confidence_rate = itemView.findViewById(R.id.confidence_rate);
            quality_rate = itemView.findViewById(R.id.quality_rate);

            save_ratings = itemView.findViewById(R.id.save_ratings);
            quality_text = itemView.findViewById(R.id.quality_text);

            tags_rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                    tags_rate.setRating(v);
                }
            });

            confidence_rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    confidence_rate.setRating(v);
                }
            });


            quality_rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    quality_rate.setRating(v);
                }
            });

            save_ratings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RatingPOJ ratingPOJ = ratingList.get(getAdapterPosition());
                    Log.d("RatingAdapter", ratingPOJ.mac_address);
                    float average;
                    float confi_int = confidence_rate.getRating();
                    float tags_int = tags_rate.getRating();
                    if(ratingPOJ.type.contains("Source")){
                        float quality_int = quality_rate.getRating();
                        average = (float) (confi_int + tags_int + quality_int) / 3.0f;
                        Log.d("RatingAdapter", "Source:" + average);
                    }
                    else{
                        Log.d("Ratingadap", confi_int + " " +  tags_int );
                        average = (float) (confi_int + tags_int) / 2.0f;
                        Log.d("RatingAdapter", "Non Source:" + average);
                    }
                    if(ratingPOJ.average == 0.0f){
                        ratingPOJ.average = average;
                        Log.d("RatingAdapter", "Inside 0.0f:" + average);
                    }
                    else{
                        Log.d("RatingAdapter", "Not inside 0.0f:" + average);
                        ratingPOJ.average = (float) (ratingPOJ.average + average) / 2.0f;
                        Log.d("RatingAdapter", "Source:" + ratingPOJ.average);
                    }
                    Log.d("RatingAdapter", ratingPOJ.average + " --");
                    GlobalApp.dbHelper.insertOrUpdateRating(ratingPOJ);
                    Toast.makeText(context, "Ratings updated", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
}

