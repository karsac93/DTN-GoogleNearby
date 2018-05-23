package com.mst.karsac.RatingsActivity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mst.karsac.ratings.RatingPOJ;
import com.mst.karsac.R;
import java.util.List;

public class FinalRatingsAdapter extends RecyclerView.Adapter<FinalRatingsAdapter.RatingsViewHolder> {

    private List<RatingPOJ> finalRatingsList;

    public FinalRatingsAdapter(List<RatingPOJ> finalRatingsList) {
        this.finalRatingsList = finalRatingsList;
    }

    @Override
    public RatingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.final_rating_row, parent, false);
        return new RatingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RatingsViewHolder holder, int position) {
        RatingPOJ ratingPOJ = finalRatingsList.get(position);
        holder.unique_txt.setText("Unique_id:" + ratingPOJ.mac_address);
        holder.avg_rating_txt.setText("Average rating:" + String.valueOf(ratingPOJ.average));

    }

    @Override
    public int getItemCount() {
        return finalRatingsList.size();
    }

    public class RatingsViewHolder extends RecyclerView.ViewHolder{
        public TextView unique_txt, avg_rating_txt;

        public RatingsViewHolder(View itemView) {
            super(itemView);
            unique_txt = itemView.findViewById(R.id.unique_id);
            avg_rating_txt = itemView.findViewById(R.id.avg_ratings);
        }
    }
}
