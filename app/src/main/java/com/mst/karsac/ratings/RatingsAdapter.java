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

import org.w3c.dom.Text;

import java.util.List;

public class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.RatingViewHolder> {
private List<MessageRatings> ratingList;
public TextView mac_address_txt, type_mac, average_mac, quality_text;
public RatingBar tags_rate, confidence_rate, quality_rate;
Button save_ratings;
Context context;



    public RatingsAdapter(List<MessageRatings> ratingList, RatingsActivity ratingsActivity) {
        this.ratingList = ratingList;
        context = ratingsActivity;
    }

    @Override
    public RatingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_ratings_row, parent, false);
        return new RatingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RatingViewHolder holder, final int position) {
        final MessageRatings messageRatings = ratingList.get(position);
        mac_address_txt.append(" " +messageRatings.intermediary);
        type_mac.append(" " + messageRatings.getInter_type());
        if(messageRatings.local_average == RatingsActivity.NOT_YET_RATED_CODE)
            average_mac.append("Not yet rated!");
        else
            average_mac.append(String.valueOf(messageRatings.local_average));
        if(!messageRatings.inter_type.contains("Source")){
            quality_rate.setVisibility(View.GONE);
            quality_text.setVisibility(View.GONE);
        }

        tags_rate.setRating(messageRatings.tag_rate);
        confidence_rate.setRating(messageRatings.confidence_rate);
        quality_rate.setRating(messageRatings.quality_rate);

        save_ratings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageRatings ratings = ratingList.get(position);
                Log.d("RatingAdapter", ratings.intermediary);
                View parent_view = (View) view.getParent();
                RatingBar local_confidence_rate = parent_view.findViewById(R.id.confidence_rate);
                RatingBar local_tags_rate = parent_view.findViewById(R.id.tags_rate);
                RatingBar local_quality_rate = parent_view.findViewById(R.id.quality_rate);
                TextView local_average = parent_view.findViewById(R.id.average_rating);
                float average;
                float confi_int = local_confidence_rate.getRating();
                float tags_int = local_tags_rate.getRating();
                float quality_int = 0.0f;
                if(messageRatings.inter_type.contains("Source")){
                    quality_int = local_quality_rate.getRating();
                    average = (float) (confi_int + tags_int + quality_int) / 3.0f;
                    Log.d("RatingAdapter", "Source:" + average);
                }
                else{
                    Log.d("Ratingadap", confi_int + " " +  tags_int );
                    average = (float) (confi_int + tags_int) / 2.0f;
                    Log.d("RatingAdapter", "Non Source:" + average);
                }
                ratings.setTag_rate(tags_int);
                ratings.setConfidence_rate(confi_int);
                ratings.setQuality_rate(quality_int);
                ratings.setLocal_average(average);
                GlobalApp.dbHelper.updateRatings(ratings);
                setDeviceRatings(ratings.intermediary, average);
                local_average.setText("Average: " + String.valueOf(average));
                Toast.makeText(context, "Ratings updated", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void setDeviceRatings(String intermediary, float average) {
        DeviceRating deviceRating = new DeviceRating(intermediary, average);
        GlobalApp.dbHelper.insertOrUpdateDeviceRatings(deviceRating);
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
                    ratingBar.setRating(v);
                }
            });

            confidence_rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    ratingBar.setRating(v);
                }
            });


            quality_rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    ratingBar.setRating(v);
                }
            });
        }
    }
}

